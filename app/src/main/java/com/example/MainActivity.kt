package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.window.DialogProperties
import com.example.ui.screens.NfcScanHistoryScreen
import com.example.ui.components.BatteryLowAlertBanner
import com.example.ui.components.GlobalDatabaseSyncProgressBar
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.components.NetworkSyncBanner
import com.example.ui.components.TopStatusBarOfflineIndicator
import com.example.ui.components.QrCodeView
import com.example.ui.components.ThermalReceiptDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinancialsGrantScreen
import com.example.ui.screens.FarmersScreen
import com.example.ui.screens.OfflineSyncScreen
import com.example.ui.screens.ProcessingHubScreen
import com.example.ui.screens.WeighBatchScreen
import com.example.ui.screens.LoginScreen
import androidx.fragment.app.FragmentActivity
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Shield
import com.example.ui.components.QuickStartCarouselDialog
import androidx.compose.material.icons.filled.School
import com.example.ui.components.BiometricAuthModal
import com.example.ui.components.HardenedSecurityBlueprintModal
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AgriCoopViewModel
import com.example.security.GlobalSessionManager
import com.example.security.RemoteWipeManager
import com.example.ui.components.RemoteWipeLockdownOverlay
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalSessionManager.init()
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AgriCoopApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriCoopApp(
    viewModel: AgriCoopViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val batches by viewModel.batches.collectAsStateWithLifecycle()
    val farmers by viewModel.farmers.collectAsStateWithLifecycle()
    val cooperatives by viewModel.cooperatives.collectAsStateWithLifecycle()
    val hubs by viewModel.hubOperations.collectAsStateWithLifecycle()
    val momoFloats by viewModel.momoFloats.collectAsStateWithLifecycle()
    val unsyncedBatches by viewModel.unsyncedBatches.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val firebaseAuth = remember { try { FirebaseAuth.getInstance() } catch (e: Exception) { null } }
    var currentUser by remember { mutableStateOf(firebaseAuth?.currentUser) }
    var isLoggedIn by remember { mutableStateOf(true) }

    val isSessionLocked by GlobalSessionManager.isSessionLocked.collectAsStateWithLifecycle()
    val autoLockTimeoutMinutes by GlobalSessionManager.autoLockTimeoutMinutes.collectAsStateWithLifecycle()

    val isDeviceWipedState by RemoteWipeManager.wipeStateFlow.collectAsStateWithLifecycle(initialValue = RemoteWipeManager.isDeviceWiped(context))
    val wipeDetails = remember(isDeviceWipedState) { RemoteWipeManager.getWipeDetails(context) }

    var showNfcHistoryModal by remember { mutableStateOf(false) }
    var showHardenedSecurityModal by remember { mutableStateOf(false) }
    var showBiometricModal by remember { mutableStateOf(false) }
    var showQuickStartCarousel by remember { mutableStateOf(false) }

    val isAppLocked = isSessionLocked

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.refreshBatteryState()
    }

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = {
                currentUser = firebaseAuth?.currentUser
                isLoggedIn = true
                GlobalSessionManager.recordLogin()
            }
        )
    } else {
        Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFFFD54F),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Agriculture,
                                    contentDescription = null,
                                    tint = Color(0xFF0B3D2E),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = uiState.localizedText.appTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = uiState.localizedText.appSubtitle,
                                fontSize = 10.sp,
                                color = Color(0xFF80CBC4)
                            )
                        }
                    }
                },
                actions = {
                    // Quick Start Guide Staff Training Action
                    Surface(
                        color = Color(0x33FFD54F),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clickable { showQuickStartCarousel = true }
                            .testTag("top_bar_quick_start_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Quick Start Training",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Quick Start",
                                color = Color(0xFFFFECB3),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Settings & Language Action Button
                    Surface(
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clickable { viewModel.setShowLanguageDialog(true) }
                            .testTag("top_bar_settings_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings & Language Toggle",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Settings",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Language Selection Chip Action
                    Surface(
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clickable { viewModel.setShowLanguageDialog(true) }
                            .testTag("top_bar_language_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.currentLanguage.flagEmoji,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.currentLanguage.displayName,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Battery Level Status Indicator Action
                    Surface(
                        color = if (uiState.batteryLevel < 15) Color(0xFFD32F2F) else Color(0x3381C784),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clickable {
                                // Toggle between simulated low battery (12%) and normal battery (85%) for test demo
                                if (uiState.batteryLevel < 15) {
                                    viewModel.setSimulatedBatteryLevel(85)
                                } else {
                                    viewModel.setSimulatedBatteryLevel(12)
                                }
                            }
                            .testTag("top_bar_battery_indicator")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (uiState.batteryLevel < 15) Icons.Default.BatteryAlert else Icons.Default.BatteryStd,
                                contentDescription = "Battery Status Observer",
                                tint = if (uiState.batteryLevel < 15) Color.White else Color(0xFF81C784),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${uiState.batteryLevel}%",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = if (unsyncedBatches.isNotEmpty()) Color(0x33FFB300) else Color(0x3381C784),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (unsyncedBatches.isNotEmpty()) Icons.Default.SignalCellularConnectedNoInternet0Bar else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (unsyncedBatches.isNotEmpty()) Color(0xFFFFD54F) else Color(0xFF81C784),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (unsyncedBatches.isNotEmpty()) "Offline (${unsyncedBatches.size} Queued)" else "Live Synced",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // NFC Tap Scan History Action
                    Surface(
                        color = Color(0x3381C784),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clickable { showNfcHistoryModal = true }
                            .testTag("top_bar_nfc_history_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = "NFC History",
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "NFC Logs",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Biometric Authentication Action Button
                    Surface(
                        color = if (isAppLocked) Color(0x66FF5252) else Color(0x3300E676),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clickable { showBiometricModal = true }
                            .testTag("top_bar_biometric_auth_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Biometric Lock",
                                tint = if (isAppLocked) Color(0xFFFF5252) else Color(0xFF00E676),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAppLocked) "LOCKED" else "Biometric",
                                color = if (isAppLocked) Color(0xFFFF5252) else Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Zero-Trust Hardened Security Architecture Blueprint Action
                    Surface(
                        color = Color(0x3300E676),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clickable { showHardenedSecurityModal = true }
                            .testTag("top_bar_zero_trust_security_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Zero-Trust Hardened Security Architecture",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "v3.0 Security",
                                color = Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Sign Out Action Button (Clears FirebaseAuth session & redirects to LoginScreen)
                    Surface(
                        color = Color(0x33FF5252),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable {
                                firebaseAuth?.signOut()
                                currentUser = null
                                isLoggedIn = false
                                Toast.makeText(context, "Signed Out of Co-op Session", Toast.LENGTH_SHORT).show()
                            }
                            .testTag("top_bar_sign_out_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Sign Out",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sign Out",
                                color = Color(0xFFFF5252),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B3D2E))
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0B3D2E),
                contentColor = Color.White
            ) {
                val navItems = listOf(
                    Triple(uiState.localizedText.tabDashboard, Icons.Default.Dashboard, "tab_dashboard"),
                    Triple(uiState.localizedText.tabWeigh, Icons.Default.Scale, "tab_weigh_batch"),
                    Triple(uiState.localizedText.tabFarmers, Icons.Default.People, "tab_farmers"),
                    Triple(uiState.localizedText.tabProcessing, Icons.Default.SolarPower, "tab_processing"),
                    Triple(uiState.localizedText.tabSync, Icons.Default.CloudSync, "tab_sync"),
                    Triple(uiState.localizedText.tabFinancials, Icons.Default.AccountBalance, "tab_financials")
                )

                navItems.forEachIndexed { index, (label, icon, testTag) ->
                    NavigationBarItem(
                        selected = uiState.activeTab == index,
                        onClick = { viewModel.selectTab(index) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text(label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0B3D2E),
                            selectedTextColor = Color(0xFFFFD54F),
                            indicatorColor = Color(0xFFFFD54F),
                            unselectedIconColor = Color(0xFF80CBC4),
                            unselectedTextColor = Color(0xFF80CBC4)
                        ),
                        modifier = Modifier.testTag(testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopStatusBarOfflineIndicator(
                isOnline = uiState.isOnline,
                unsyncedCount = unsyncedBatches.size,
                onToggleNetwork = { viewModel.toggleNetworkStatus() }
            )

            GlobalDatabaseSyncProgressBar(
                isOnline = uiState.isOnline,
                isSyncing = uiState.isSyncing,
                syncProgress = uiState.syncProgress,
                syncCompletedCount = uiState.syncCompletedCount,
                syncTotalCount = uiState.syncTotalCount,
                unsyncedCount = unsyncedBatches.size,
                totalRecordsCount = batches.size,
                syncStatusMessage = uiState.syncStatusMessage,
                lastSyncedTimeText = uiState.lastSyncedTimeText,
                bandwidthCondition = uiState.bandwidthCondition,
                syncStrategy = uiState.syncStrategy,
                onTriggerSync = { viewModel.performLowBandwidthSync() }
            )

            BatteryLowAlertBanner(
                batteryLevel = uiState.batteryLevel,
                isLowBattery = uiState.isLowBatteryWarning,
                isCharging = uiState.isCharging,
                dismissed = uiState.dismissBatteryAlert,
                onDismiss = { viewModel.dismissBatteryAlert() },
                onSimulateToggle = { level -> viewModel.setSimulatedBatteryLevel(level) }
            )

            NetworkSyncBanner(
                isOnline = uiState.isOnline,
                isSyncing = uiState.isSyncing,
                unsyncedCount = unsyncedBatches.size,
                syncStatusMessage = uiState.syncStatusMessage,
                lastSyncedTimeText = uiState.lastSyncedTimeText,
                bandwidthCondition = uiState.bandwidthCondition,
                syncStrategy = uiState.syncStrategy,
                onToggleNetwork = { viewModel.toggleNetworkStatus() },
                onToggleBandwidthCondition = {
                    val nextCond = if (uiState.bandwidthCondition == com.example.service.BandwidthCondition.CELLULAR_LOW_BANDWIDTH) {
                        com.example.service.BandwidthCondition.HIGH_BANDWIDTH_WIFI
                    } else {
                        com.example.service.BandwidthCondition.CELLULAR_LOW_BANDWIDTH
                    }
                    viewModel.setBandwidthCondition(nextCond)
                },
                onTriggerSync = { viewModel.performLowBandwidthSync() }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
            when (uiState.activeTab) {
                0 -> DashboardScreen(
                    batches = batches,
                    farmers = farmers,
                    cooperatives = cooperatives,
                    hubs = hubs,
                    momoFloats = momoFloats,
                    unsyncedCount = unsyncedBatches.size,
                    isPredictingHarvestTrends = uiState.isPredictingHarvestTrends,
                    harvestPredictionResult = uiState.harvestPredictionResult,
                    predictionError = uiState.predictionError,
                    currentLanguage = uiState.currentLanguage,
                    onOpenLanguageDialog = { viewModel.setShowLanguageDialog(true) },
                    onRunHarvestPrediction = { viewModel.runGeminiHarvestPrediction() },
                    onNavigateTab = { viewModel.selectTab(it) },
                    onSelectBatchForQr = { viewModel.selectBatchForQr(it) },
                    onTriggerSync = { viewModel.performLowBandwidthSync() },
                    onOpenBiometricAuth = { showBiometricModal = true }
                )
                1 -> WeighBatchScreen(
                    farmers = farmers,
                    batches = batches,
                    isScaleConnected = uiState.isScaleConnected,
                    scaleWeightKg = uiState.scaleWeightKg,
                    scaleStatusText = uiState.scaleStatusText,
                    onToggleScale = { viewModel.toggleScaleConnection() },
                    onCalibrateScale = { viewModel.calibrateScaleZero() },
                    onSelectBatchForQr = { viewModel.selectBatchForQr(it) },
                    onSubmitBatch = { farmerId, crop, weight, starch, moisture, price, location ->
                        viewModel.addProduceBatch(farmerId, crop, weight, starch, moisture, price, location)
                    },
                    onUndoBatch = { batch ->
                        viewModel.deleteProduceBatch(batch)
                    }
                )
                2 -> FarmersScreen(
                    farmers = farmers,
                    cooperatives = cooperatives,
                    batches = batches,
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onOnboardFarmer = { coopId, name, phone, momo, gender, yob, cuttings ->
                        viewModel.onboardFarmer(coopId, name, phone, momo, gender, yob, cuttings)
                    }
                )
                3 -> ProcessingHubScreen(
                    hubs = hubs
                )
                4 -> OfflineSyncScreen(
                    unsyncedBatches = unsyncedBatches,
                    allBatches = batches,
                    farmers = farmers,
                    cooperatives = cooperatives,
                    hubs = hubs,
                    momoFloats = momoFloats,
                    isSyncing = uiState.isSyncing,
                    syncStatusMessage = uiState.syncStatusMessage,
                    encryptedSmsPreview = uiState.encryptedSmsPreview,
                    compressedJsonPreview = uiState.compressedJsonPreview,
                    bandwidthCondition = uiState.bandwidthCondition,
                    syncStrategy = uiState.syncStrategy,
                    syncMetrics = uiState.syncPayloadMetrics,
                    syncLogHistory = uiState.syncLogHistory,
                    onSelectBandwidthCondition = { viewModel.setBandwidthCondition(it) },
                    onSelectSyncStrategy = { viewModel.setSyncStrategy(it) },
                    onPerformSync = { viewModel.performLowBandwidthSync() },
                    onTriggerMoMo = { viewModel.triggerMoMoPayout(it) },
                    onSelectBatchForQr = { viewModel.selectBatchForQr(it) }
                )
                5 -> FinancialsGrantScreen()
            }

            // Settings & Device Health / Language Selection Modal
            if (uiState.showLanguageDialog) {
                LanguageSelectionDialog(
                    currentLanguage = uiState.currentLanguage,
                    onSelectLanguage = { viewModel.setAppLanguage(it) },
                    onDismiss = { viewModel.setShowLanguageDialog(false) },
                    simulatedBatteryLevel = uiState.batteryLevel,
                    onSimulateBatteryChange = { viewModel.setSimulatedBatteryLevel(it) }
                )
            }

            // NFC Tag Scan History Log Modal Dialog
            if (showNfcHistoryModal) {
                Dialog(
                    onDismissRequest = { showNfcHistoryModal = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    NfcScanHistoryScreen(
                        onBack = { showNfcHistoryModal = false },
                        onOpenNfcScanner = {
                            showNfcHistoryModal = false
                            viewModel.selectTab(1) // Navigate to Weigh Batch Screen
                        }
                    )
                }
            }

            // Zero-Trust Hardened Security Architecture Blueprint Modal
            if (showHardenedSecurityModal) {
                HardenedSecurityBlueprintModal(
                    onDismissRequest = { showHardenedSecurityModal = false }
                )
            }

            // Biometric Staff Authentication Modal / Overlay
            if (showBiometricModal || isAppLocked) {
                BiometricAuthModal(
                    isAppLocked = isAppLocked,
                    autoLockTimeoutMinutes = autoLockTimeoutMinutes,
                    onAutoLockTimeoutChange = { GlobalSessionManager.setAutoLockTimeout(it) },
                    onUnlockSuccess = {
                        GlobalSessionManager.unlockSession()
                        showBiometricModal = false
                    },
                    onLockApp = {
                        GlobalSessionManager.lockSession()
                    },
                    onDismissRequest = {
                        if (!isAppLocked) {
                            showBiometricModal = false
                        }
                    }
                )
            }

            // Thermal Slip Dialog
            uiState.selectedBatchForQr?.let { batch ->
                ThermalReceiptDialog(
                    batch = batch,
                    onDismiss = { viewModel.selectBatchForQr(null) },
                    onMoMoTrigger = {
                        viewModel.triggerMoMoPayout(batch.id)
                        viewModel.selectBatchForQr(null)
                    }
                )
            }

            // Batch Created Success Dialog
            if (uiState.showSuccessDialog) {
                Dialog(onDismissRequest = { viewModel.dismissSuccessDialog() }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tuber Batch Recorded & Tagged!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0B3D2E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Code: ${uiState.lastCreatedBatch?.batchCode}\nPayout: LRD $%.2f".format(uiState.lastCreatedBatch?.totalPayoutLrd ?: 0.0),
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            uiState.lastCreatedBatch?.let {
                                QrCodeView(data = it.batchCode, size = 100.dp)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.dismissSuccessDialog() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Done")
                                }
                            }
                        }
                    }
                }
            }

            // Remote Wipe Emergency Lockdown Overlay
            if (isDeviceWipedState) {
                RemoteWipeLockdownOverlay(
                    wipeDetails = wipeDetails,
                    onRestored = {
                        // When admin PIN unlocks device, refresh Viewmodel state
                        viewModel.restoreAndReSeedData()
                    }
                )
            }

            // Quick Start Interactive Training Carousel Dialog
            if (showQuickStartCarousel) {
                QuickStartCarouselDialog(
                    onDismiss = { showQuickStartCarousel = false },
                    onComplete = {
                        Toast.makeText(context, "Co-op Staff Quick Start Training Completed!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
}
}
