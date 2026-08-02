package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.entities.AuditLogEntity
import com.example.data.db.entities.CooperativeEntity
import com.example.data.db.entities.FarmerEntity
import com.example.data.db.entities.HubOperationEntity
import com.example.data.db.entities.MoMoFloatEntity
import com.example.data.db.entities.ProduceBatchEntity
import com.example.data.repository.AgriCoopRepository
import com.example.security.SecureAuditLogger
import com.example.service.AppLanguage
import com.example.service.BandwidthCondition
import com.example.service.BatteryObserver
import com.example.service.GeminiHarvestTrendService
import com.example.service.HarvestPredictionResult
import com.example.service.IntelligentSyncStrategyService
import com.example.service.LiberianLanguageLocalizer
import com.example.service.LocalizedText
import com.example.service.NetworkConnectivityMonitor
import com.example.service.PendingBatchNotifier
import com.example.service.SyncLogEntry
import com.example.service.SyncPayloadMetrics
import com.example.service.SyncStrategyType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class AgriCoopUiState(
    val activeTab: Int = 0,
    val searchQuery: String = "",
    val countyFilter: String = "ALL",
    val isScaleConnected: Boolean = true,
    val scaleWeightKg: Double = 450.0,
    val scaleStatusText: String = "Bluetooth IP67 Scale Ready (Zeroed)",
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val syncProgress: Float = 0f,
    val syncCompletedCount: Int = 0,
    val syncTotalCount: Int = 0,
    val syncStatusMessage: String = "Offline Mode - Queued locally in Room DB",
    val lastSyncedTimeText: String = "Just now",
    val encryptedSmsPreview: String = "",
    val compressedJsonPreview: String = "",
    val showSuccessDialog: Boolean = false,
    val lastCreatedBatch: ProduceBatchEntity? = null,
    val selectedBatchForQr: ProduceBatchEntity? = null,
    val selectedFarmerId: String = "",
    val batchFormError: String? = null,
    val showOverdueAlertBanner: Boolean = true,
    val batteryLevel: Int = 100,
    val isLowBatteryWarning: Boolean = false,
    val isCharging: Boolean = false,
    val dismissBatteryAlert: Boolean = false,
    val isPredictingHarvestTrends: Boolean = false,
    val harvestPredictionResult: HarvestPredictionResult? = null,
    val predictionError: String? = null,
    val bandwidthCondition: BandwidthCondition = BandwidthCondition.CELLULAR_LOW_BANDWIDTH,
    val syncStrategy: SyncStrategyType = SyncStrategyType.METADATA_PACKET_PRIORITY,
    val syncPayloadMetrics: SyncPayloadMetrics? = null,
    val syncLogHistory: List<SyncLogEntry> = emptyList(),
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val showLanguageDialog: Boolean = false
) {
    val localizedText: LocalizedText get() = LiberianLanguageLocalizer.getLocalizedText(currentLanguage)
}

class AgriCoopViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AgriCoopRepository = AgriCoopRepository(AppDatabase.getDatabase(application).agriCoopDao())
    private val _uiState = MutableStateFlow(AgriCoopUiState())
    val uiState: StateFlow<AgriCoopUiState> = _uiState.asStateFlow()

    // Master Room Database State Flows
    val cooperatives: StateFlow<List<CooperativeEntity>> = repository.allCooperatives
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val farmers: StateFlow<List<FarmerEntity>> = repository.allFarmers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val batches: StateFlow<List<ProduceBatchEntity>> = repository.allBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overdue Pending Batches (>24 hours in PENDING status)
    val overduePendingBatches: StateFlow<List<ProduceBatchEntity>> = batches.map { list ->
        val now = System.currentTimeMillis()
        val twentyFourHoursMs = 24 * 60 * 60 * 1000L
        list.filter { batch ->
            batch.payoutStatus == "PENDING" && (now - batch.timestamp) >= twentyFourHoursMs
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unsyncedBatches: StateFlow<List<ProduceBatchEntity>> = repository.unsyncedBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hubOperations: StateFlow<List<HubOperationEntity>> = repository.hubOperations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val momoFloats: StateFlow<List<MoMoFloatEntity>> = repository.momoFloats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = SecureAuditLogger.getAllAuditLogsFlow(application)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        com.example.worker.SyncWorkScheduler.scheduleAutoSync(application)
        SecureAuditLogger.seedInitialAuditLogsIfEmpty(application)

        viewModelScope.launch {
            com.example.security.DatabaseKeyRotationService.checkAndRotateIfDue(application)
        }

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        viewModelScope.launch {
            batches.collect { batchList ->
                PendingBatchNotifier.checkAndNotifyPendingBatches(getApplication(), batchList)
            }
        }

        viewModelScope.launch {
            unsyncedBatches.collect {
                recalculateSyncMetrics()
            }
        }

        val networkMonitor = NetworkConnectivityMonitor.getInstance(application)
        viewModelScope.launch {
            networkMonitor.networkState.collect { netState ->
                val prevOnline = _uiState.value.isOnline
                _uiState.value = _uiState.value.copy(
                    isOnline = netState.isConnected,
                    syncStatusMessage = if (netState.isConnected) {
                        if (netState.isLowBandwidth) "Online (${netState.connectionType}) - Prioritizing Metadata Packets"
                        else "Online (${netState.connectionType}) - Connected to PostGIS Cloud"
                    } else {
                        "Offline Mode - Room DB active (Data Queued)"
                    }
                )
                if (!prevOnline && netState.isConnected && unsyncedBatches.value.isNotEmpty() && !_uiState.value.isSyncing) {
                    performLowBandwidthSync()
                }
            }
        }
    }

    fun restoreAndReSeedData() {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // Filtered Farmers State Flow combining Farmers, Search Query, and County Filter
    val filteredFarmers: StateFlow<List<FarmerEntity>> = combine(
        farmers,
        uiState
    ) { allFarmers, state ->
        val query = state.searchQuery.trim()
        val filter = state.countyFilter
        allFarmers.filter { farmer ->
            val matchesSearch = query.isEmpty() ||
                    farmer.fullName.contains(query, ignoreCase = true) ||
                    farmer.nationalId.contains(query, ignoreCase = true) ||
                    farmer.cooperativeName.contains(query, ignoreCase = true) ||
                    farmer.phoneNumber.contains(query)

            val matchesCounty = when (filter) {
                "NIMBA" -> farmer.nationalId.contains("NIM", ignoreCase = true) ||
                        farmer.cooperativeName.contains("Ganta", ignoreCase = true) ||
                        farmer.cooperativeName.contains("Sanniquellie", ignoreCase = true)
                "LOFA" -> farmer.nationalId.contains("LOF", ignoreCase = true) ||
                        farmer.cooperativeName.contains("Voinjama", ignoreCase = true) ||
                        farmer.cooperativeName.contains("Zorzor", ignoreCase = true) ||
                        farmer.cooperativeName.contains("Foya", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesCounty
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Farmer State Management ---

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setCountyFilter(filter: String) {
        _uiState.value = _uiState.value.copy(countyFilter = filter)
    }

    fun selectFarmer(farmerId: String) {
        _uiState.value = _uiState.value.copy(selectedFarmerId = farmerId)
    }

    fun getFarmerById(farmerId: String): FarmerEntity? {
        return farmers.value.find { it.id == farmerId }
    }

    fun onboardFarmer(
        coopId: String,
        fullName: String,
        phone: String,
        momo: String,
        gender: String,
        yearOfBirth: Int,
        cuttings: Int
    ) {
        viewModelScope.launch {
            val coops = cooperatives.value
            val coop = coops.find { it.id == coopId } ?: coops.firstOrNull()
            val isYouth = (2026 - yearOfBirth) in 18..35

            val newFarmer = FarmerEntity(
                id = "farm-" + UUID.randomUUID().toString().take(6),
                cooperativeId = coop?.id ?: "coop-ganta-01",
                cooperativeName = coop?.name ?: "Ganta District Farmers Co-op",
                nationalId = "LR-" + (if (coop?.county == "Lofa") "LOF" else "NIM") + "-" + (10000..99999).random(),
                fullName = fullName,
                phoneNumber = phone,
                momoNumber = momo,
                gender = gender,
                yearOfBirth = yearOfBirth,
                isYouth = isYouth,
                seedCuttingsAllocated = cuttings,
                totalBatchesDelivered = 0,
                totalEarningsLrd = 0.0
            )

            repository.addFarmer(newFarmer)
        }
    }

    // --- Tuber Processing Batch State Management ---

    fun recordNewTuberBatch(
        farmerId: String,
        cropType: String,
        weightKg: Double,
        starchPct: Double,
        moisturePct: Double,
        pricePerKgLrd: Double,
        locationName: String
    ) {
        if (weightKg <= 0) {
            _uiState.value = _uiState.value.copy(batchFormError = "Weight must be greater than 0 kg")
            return
        }
        if (pricePerKgLrd <= 0) {
            _uiState.value = _uiState.value.copy(batchFormError = "Unit price must be greater than 0 LRD")
            return
        }

        _uiState.value = _uiState.value.copy(batchFormError = null)

        addProduceBatch(
            farmerId = farmerId,
            cropType = cropType,
            weightKg = weightKg,
            starchPct = starchPct,
            moisturePct = moisturePct,
            pricePerKgLrd = pricePerKgLrd,
            locationName = locationName
        )
    }

    fun addProduceBatch(
        farmerId: String,
        cropType: String,
        weightKg: Double,
        starchPct: Double,
        moisturePct: Double,
        pricePerKgLrd: Double,
        locationName: String
    ) {
        viewModelScope.launch {
            val currentFarmers = farmers.value
            val farmer = currentFarmers.find { it.id == farmerId } ?: currentFarmers.firstOrNull()

            val countyTag = if (locationName.contains("Lofa", ignoreCase = true) || (farmer?.cooperativeName?.contains("Voinjama", ignoreCase = true) == true)) "LOF" else "NIM"
            val randomNum = (100..999).random()
            val batchCode = "BATCH-$countyTag-2026-$randomNum"

            val totalPayout = weightKg * pricePerKgLrd

            val newBatch = ProduceBatchEntity(
                id = UUID.randomUUID().toString(),
                batchCode = batchCode,
                farmerId = farmer?.id ?: "farm-001",
                farmerName = farmer?.fullName ?: "Farmer Partner",
                cooperativeName = farmer?.cooperativeName ?: "Ganta District Co-op",
                cropType = cropType,
                weightKg = weightKg,
                starchPercentage = starchPct,
                moisturePercentage = moisturePct,
                pricePerKgLrd = pricePerKgLrd,
                totalPayoutLrd = totalPayout,
                latitude = if (countyTag == "LOF") 8.4219 else 7.3622,
                longitude = if (countyTag == "LOF") -9.7478 else -8.9811,
                locationName = locationName,
                payoutStatus = "PENDING",
                momoTransactionRef = "PENDING_DISPATCH",
                isSynced = false,
                timestamp = System.currentTimeMillis()
            )

            // Insert new batch into Room DB via repository
            repository.addProduceBatch(newBatch)

            // Update farmer delivered batches & total earnings in Room DB if farmer exists
            farmer?.let { f ->
                val updatedFarmer = f.copy(
                    totalBatchesDelivered = f.totalBatchesDelivered + 1,
                    totalEarningsLrd = f.totalEarningsLrd + totalPayout
                )
                repository.updateFarmer(updatedFarmer)
            }

            _uiState.value = _uiState.value.copy(
                showSuccessDialog = true,
                lastCreatedBatch = newBatch,
                selectedBatchForQr = newBatch
            )
        }
    }

    fun deleteProduceBatch(batch: ProduceBatchEntity) {
        viewModelScope.launch {
            repository.deleteProduceBatch(batch.id)

            SecureAuditLogger.recordAction(
                context = getApplication(),
                action = "BATCH_DELETED",
                category = "SENSITIVE_DATA_MUTATION",
                detail = "Deleted batch ${batch.batchCode} (${batch.weightKg} kg ${batch.cropType}) for ${batch.farmerName}"
            )

            val currentFarmers = farmers.value
            val farmer = currentFarmers.find { it.id == batch.farmerId }
            farmer?.let { f ->
                val updatedFarmer = f.copy(
                    totalBatchesDelivered = (f.totalBatchesDelivered - 1).coerceAtLeast(0),
                    totalEarningsLrd = (f.totalEarningsLrd - batch.totalPayoutLrd).coerceAtLeast(0.0)
                )
                repository.updateFarmer(updatedFarmer)
            }

            if (_uiState.value.lastCreatedBatch?.id == batch.id) {
                _uiState.value = _uiState.value.copy(
                    lastCreatedBatch = null,
                    selectedBatchForQr = null,
                    showSuccessDialog = false
                )
            }
        }
    }

    fun getBatchesForFarmer(farmerId: String): List<ProduceBatchEntity> {
        return batches.value.filter { it.farmerId == farmerId }
    }

    // --- Bluetooth IP67 Scale & UI Actions ---

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(activeTab = index)
    }

    fun setScaleWeight(weight: Double) {
        _uiState.value = _uiState.value.copy(scaleWeightKg = weight)
    }

    fun toggleScaleConnection() {
        val current = _uiState.value.isScaleConnected
        _uiState.value = _uiState.value.copy(
            isScaleConnected = !current,
            scaleStatusText = if (!current) "Bluetooth IP67 Scale Connected" else "Scale Disconnected (Manual Override)"
        )
    }

    fun calibrateScaleZero() {
        _uiState.value = _uiState.value.copy(
            scaleWeightKg = 0.0,
            scaleStatusText = "Scale Zeroed & Calibrated (Bi-Monthly Standard)"
        )
    }

    fun dismissSuccessDialog() {
        _uiState.value = _uiState.value.copy(showSuccessDialog = false)
    }

    fun selectBatchForQr(batch: ProduceBatchEntity?) {
        _uiState.value = _uiState.value.copy(selectedBatchForQr = batch)
    }

    fun triggerMoMoPayout(batchId: String) {
        viewModelScope.launch {
            val ref = "MOMO-" + listOf("ORG", "MTN").random() + "-" + (10000..99999).random()
            repository.updatePayoutStatus(batchId, "PAID", ref)

            SecureAuditLogger.recordAction(
                context = getApplication(),
                action = "MOMO_PAYOUT_INITIATED",
                category = "FINANCIAL_TRANSACTION",
                detail = "Dispatched MoMo transaction payout $ref for batch $batchId"
            )
        }
    }

    fun setBandwidthCondition(condition: BandwidthCondition) {
        val newStrategy = if (condition == BandwidthCondition.CELLULAR_LOW_BANDWIDTH) {
            SyncStrategyType.METADATA_PACKET_PRIORITY
        } else {
            SyncStrategyType.FULL_DATABASE_SNAPSHOT
        }
        _uiState.value = _uiState.value.copy(
            bandwidthCondition = condition,
            syncStrategy = newStrategy,
            syncStatusMessage = if (condition == BandwidthCondition.CELLULAR_LOW_BANDWIDTH) {
                "Low-Bandwidth Cellular (2G/3G) - Metadata Packet Priority Active"
            } else {
                "High-Bandwidth Wi-Fi/4G - Full DB Snapshot Active"
            }
        )
        recalculateSyncMetrics()
    }

    fun setSyncStrategy(strategy: SyncStrategyType) {
        _uiState.value = _uiState.value.copy(syncStrategy = strategy)
        recalculateSyncMetrics()
    }

    fun recalculateSyncMetrics() {
        val unsynced = unsyncedBatches.value
        val metrics = IntelligentSyncStrategyService.computeSyncPayloadMetrics(unsynced, _uiState.value.syncStrategy)
        _uiState.value = _uiState.value.copy(
            syncPayloadMetrics = metrics,
            compressedJsonPreview = metrics.compressedPayloadPreview,
            encryptedSmsPreview = metrics.smsFallbackPreview
        )
    }

    fun toggleNetworkStatus() {
        val current = _uiState.value.isOnline
        val newOnline = !current
        NetworkConnectivityMonitor.getInstance(getApplication()).toggleOfflineSimulation(!newOnline)
    }

    fun performLowBandwidthSync() {
        viewModelScope.launch {
            val strategy = _uiState.value.syncStrategy
            val unsynced = unsyncedBatches.value
            val ids = unsynced.map { it.id }

            if (ids.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncProgress = 1.0f,
                    syncStatusMessage = "All Room DB records are already synced to PostGIS Cloud."
                )
                return@launch
            }

            val metrics = IntelligentSyncStrategyService.computeSyncPayloadMetrics(unsynced, strategy)
            val total = ids.size

            _uiState.value = _uiState.value.copy(
                isSyncing = true,
                syncProgress = 0.05f,
                syncCompletedCount = 0,
                syncTotalCount = total,
                syncPayloadMetrics = metrics,
                syncStatusMessage = if (strategy == SyncStrategyType.METADATA_PACKET_PRIORITY) {
                    "Compressing & Pushing $total Light Metadata Packets (${metrics.totalTransmittedBytes} B)..."
                } else {
                    "Pushing Full Database Snapshot for $total Batches (${metrics.totalTransmittedBytes} B)..."
                },
                compressedJsonPreview = metrics.compressedPayloadPreview,
                encryptedSmsPreview = metrics.smsFallbackPreview
            )

            val stepTime = when {
                total > 10 -> 180L
                total > 5 -> 280L
                else -> 380L
            }

            for (index in 1..total) {
                delay(stepTime)
                val currentProgress = index.toFloat() / total.toFloat()
                _uiState.value = _uiState.value.copy(
                    syncProgress = currentProgress,
                    syncCompletedCount = index,
                    syncStatusMessage = "Uploading Room DB to Cloud: Record $index of $total (${(currentProgress * 100).toInt()}% synced)"
                )
            }

            repository.markBatchesSynced(ids)

            val formattedTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())

            val logEntry = SyncLogEntry(
                timestampText = formattedTime,
                strategyUsed = strategy,
                recordCount = ids.size,
                bytesSent = metrics.totalTransmittedBytes,
                bytesSaved = metrics.bytesSaved,
                statusText = if (strategy == SyncStrategyType.METADATA_PACKET_PRIORITY) {
                    "Pushed ${ids.size} Metadata Packets (${metrics.totalTransmittedBytes}B sent, ${metrics.bytesSaved}B saved)"
                } else {
                    "Pushed Full Snapshot (${metrics.totalTransmittedBytes}B sent)"
                }
            )

            val updatedHistory = listOf(logEntry) + _uiState.value.syncLogHistory.take(15)

            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                syncProgress = 1.0f,
                syncCompletedCount = total,
                lastSyncedTimeText = formattedTime,
                syncLogHistory = updatedHistory,
                syncStatusMessage = if (strategy == SyncStrategyType.METADATA_PACKET_PRIORITY) {
                    "Metadata Packet Sync Complete: ${ids.size} packets pushed (${metrics.totalTransmittedBytes}B sent, ${metrics.savingsPercentage}% data saved)."
                } else {
                    "Full Snapshot Sync Complete: ${ids.size} Room records pushed to PostGIS Cloud (${metrics.totalTransmittedBytes}B)."
                }
            )
            recalculateSyncMetrics()
        }
    }

    fun dismissOverdueAlertBanner() {
        _uiState.value = _uiState.value.copy(showOverdueAlertBanner = false)
    }

    fun refreshBatteryState() {
        val level = BatteryObserver.getCurrentBatteryLevel(getApplication())
        val charging = BatteryObserver.isCharging(getApplication())
        val isLow = level in 1..14 && !charging

        if (isLow) {
            BatteryObserver.evaluateAndNotifyLowBattery(getApplication(), level)
        }

        _uiState.value = _uiState.value.copy(
            batteryLevel = level,
            isCharging = charging,
            isLowBatteryWarning = isLow
        )
    }

    fun setSimulatedBatteryLevel(level: Int) {
        val isLow = level in 1..14
        if (isLow) {
            BatteryObserver.evaluateAndNotifyLowBattery(getApplication(), level)
        }
        _uiState.value = _uiState.value.copy(
            batteryLevel = level,
            isLowBatteryWarning = isLow,
            dismissBatteryAlert = false
        )
    }

    fun dismissBatteryAlert() {
        _uiState.value = _uiState.value.copy(dismissBatteryAlert = true)
    }

    fun setAppLanguage(language: AppLanguage) {
        _uiState.value = _uiState.value.copy(
            currentLanguage = language,
            showLanguageDialog = false
        )
    }

    fun setShowLanguageDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showLanguageDialog = show)
    }

    fun runGeminiHarvestPrediction() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isPredictingHarvestTrends = true,
                predictionError = null
            )
            try {
                val currentBatches = batches.value
                val currentFarmers = farmers.value
                val result = GeminiHarvestTrendService.predictHarvestVolumeTrends(currentBatches, currentFarmers)
                _uiState.value = _uiState.value.copy(
                    isPredictingHarvestTrends = false,
                    harvestPredictionResult = result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPredictingHarvestTrends = false,
                    predictionError = "Failed to run harvest prediction: ${e.message}"
                )
            }
        }
    }
}
