package com.example.data.repository

import com.example.data.db.dao.AgriCoopDao
import com.example.data.db.entities.CooperativeEntity
import com.example.data.db.entities.FarmerEntity
import com.example.data.db.entities.HubOperationEntity
import com.example.data.db.entities.MoMoFloatEntity
import com.example.data.db.entities.ProduceBatchEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AgriCoopRepository(private val dao: AgriCoopDao) {

    val allCooperatives: Flow<List<CooperativeEntity>> = dao.getAllCooperatives()
    val allFarmers: Flow<List<FarmerEntity>> = dao.getAllFarmers()
    val allBatches: Flow<List<ProduceBatchEntity>> = dao.getAllProduceBatches()
    val unsyncedBatches: Flow<List<ProduceBatchEntity>> = dao.getUnsyncedBatches()
    val hubOperations: Flow<List<HubOperationEntity>> = dao.getAllHubOperations()
    val momoFloats: Flow<List<MoMoFloatEntity>> = dao.getAllMoMoFloats()

    suspend fun addProduceBatch(batch: ProduceBatchEntity) {
        dao.insertProduceBatch(batch)
    }

    suspend fun deleteProduceBatch(batchId: String) {
        dao.deleteProduceBatchById(batchId)
    }

    suspend fun addFarmer(farmer: FarmerEntity) {
        dao.insertFarmer(farmer)
    }

    suspend fun updateFarmer(farmer: FarmerEntity) {
        dao.updateFarmer(farmer)
    }

    suspend fun updatePayoutStatus(batchId: String, status: String, ref: String) {
        dao.updateBatchPayoutStatus(batchId, status, ref)
    }

    suspend fun markBatchesSynced(batchIds: List<String>) {
        dao.markBatchesAsSynced(batchIds)
    }

    suspend fun seedInitialDataIfEmpty() {
        // Pre-populate realistic Lofa & Nimba data
        val coops = listOf(
            CooperativeEntity(
                id = "coop-ganta-01",
                name = "Ganta District Farmers Co-op",
                county = "Nimba",
                district = "Ganta Central",
                leadPerson = "Theresa Gbotoe",
                phone = "+231776112233",
                memberCount = 320
            ),
            CooperativeEntity(
                id = "coop-voinjama-02",
                name = "Voinjama Tuber Alliance",
                county = "Lofa",
                district = "Voinjama City",
                leadPerson = "Mondo Mawolo",
                phone = "+231888445566",
                memberCount = 280
            ),
            CooperativeEntity(
                id = "coop-zorzor-03",
                name = "Zorzor Agricultural FBO",
                county = "Lofa",
                district = "Zorzor District",
                leadPerson = "Saa Tengbeh",
                phone = "+231770998877",
                memberCount = 210
            ),
            CooperativeEntity(
                id = "coop-sanniquellie-04",
                name = "Sanniquellie Women Farmers Co-op",
                county = "Nimba",
                district = "Sanniquellie Mah",
                leadPerson = "Kula Saah",
                phone = "+231886223344",
                memberCount = 240
            ),
            CooperativeEntity(
                id = "coop-foya-05",
                name = "Foya Quality Cassava Producers",
                county = "Lofa",
                district = "Foya District",
                leadPerson = "Tarnue Weefur",
                phone = "+231775551122",
                memberCount = 150
            )
        )
        dao.insertCooperatives(coops)

        val farmers = listOf(
            FarmerEntity(
                id = "farm-001",
                cooperativeId = "coop-ganta-01",
                cooperativeName = "Ganta District Farmers Co-op",
                nationalId = "LR-NIM-88412",
                fullName = "Korpo Flomo",
                phoneNumber = "+231776991100",
                momoNumber = "+231776991100",
                gender = "FEMALE",
                yearOfBirth = 1988,
                isYouth = false,
                seedCuttingsAllocated = 450,
                totalBatchesDelivered = 12,
                totalEarningsLrd = 185000.0
            ),
            FarmerEntity(
                id = "farm-002",
                cooperativeId = "coop-ganta-01",
                cooperativeName = "Ganta District Farmers Co-op",
                nationalId = "LR-NIM-99213",
                fullName = "Emmanuel Kollie",
                phoneNumber = "+231888223311",
                momoNumber = "+231888223311",
                gender = "MALE",
                yearOfBirth = 1999,
                isYouth = true,
                seedCuttingsAllocated = 600,
                totalBatchesDelivered = 8,
                totalEarningsLrd = 124000.0
            ),
            FarmerEntity(
                id = "farm-003",
                cooperativeId = "coop-voinjama-02",
                cooperativeName = "Voinjama Tuber Alliance",
                nationalId = "LR-LOF-33104",
                fullName = "Marie Yarkpawolo",
                phoneNumber = "+231775334422",
                momoNumber = "+231775334422",
                gender = "FEMALE",
                yearOfBirth = 1995,
                isYouth = true,
                seedCuttingsAllocated = 500,
                totalBatchesDelivered = 15,
                totalEarningsLrd = 240000.0
            ),
            FarmerEntity(
                id = "farm-004",
                cooperativeId = "coop-zorzor-03",
                cooperativeName = "Zorzor Agricultural FBO",
                nationalId = "LR-LOF-11092",
                fullName = "Tarnue Weefur",
                phoneNumber = "+231886119988",
                momoNumber = "+231886119988",
                gender = "MALE",
                yearOfBirth = 1992,
                isYouth = false,
                seedCuttingsAllocated = 400,
                totalBatchesDelivered = 6,
                totalEarningsLrd = 98000.0
            ),
            FarmerEntity(
                id = "farm-005",
                cooperativeId = "coop-sanniquellie-04",
                cooperativeName = "Sanniquellie Women Farmers Co-op",
                nationalId = "LR-NIM-44519",
                fullName = "Cecelia Kamara",
                phoneNumber = "+231770441199",
                momoNumber = "+231770441199",
                gender = "FEMALE",
                yearOfBirth = 2001,
                isYouth = true,
                seedCuttingsAllocated = 750,
                totalBatchesDelivered = 10,
                totalEarningsLrd = 162000.0
            )
        )
        dao.insertFarmers(farmers)

        val now = System.currentTimeMillis()
        val batches = listOf(
            ProduceBatchEntity(
                id = "batch-001",
                batchCode = "BATCH-NIM-2026-001",
                farmerId = "farm-001",
                farmerName = "Korpo Flomo",
                cooperativeName = "Ganta District Farmers Co-op",
                cropType = "CASSAVA",
                weightKg = 420.0,
                starchPercentage = 24.5,
                moisturePercentage = 62.0,
                pricePerKgLrd = 85.0,
                totalPayoutLrd = 35700.0,
                latitude = 7.3622,
                longitude = -8.9811,
                locationName = "Ganta Collection Spoke 1",
                payoutStatus = "PAID",
                momoTransactionRef = "MOMO-ORG-99201",
                isSynced = true,
                timestamp = now - (3600000 * 4)
            ),
            ProduceBatchEntity(
                id = "batch-002",
                batchCode = "BATCH-LOF-2026-014",
                farmerId = "farm-003",
                farmerName = "Marie Yarkpawolo",
                cooperativeName = "Voinjama Tuber Alliance",
                cropType = "CASSAVA",
                weightKg = 680.0,
                starchPercentage = 26.2,
                moisturePercentage = 58.5,
                pricePerKgLrd = 90.0,
                totalPayoutLrd = 61200.0,
                latitude = 8.4219,
                longitude = -9.7478,
                locationName = "Voinjama Regional Hub",
                payoutStatus = "PAID",
                momoTransactionRef = "MOMO-MTN-44120",
                isSynced = true,
                timestamp = now - (3600000 * 2)
            ),
            ProduceBatchEntity(
                id = "batch-003",
                batchCode = "BATCH-NIM-2026-002",
                farmerId = "farm-002",
                farmerName = "Emmanuel Kollie",
                cooperativeName = "Ganta District Farmers Co-op",
                cropType = "SWEET_POTATO",
                weightKg = 310.0,
                starchPercentage = 19.8,
                moisturePercentage = 68.0,
                pricePerKgLrd = 95.0,
                totalPayoutLrd = 29450.0,
                latitude = 7.3710,
                longitude = -8.9740,
                locationName = "Ganta Collection Spoke 2",
                payoutStatus = "PENDING",
                momoTransactionRef = "PENDING_MOMO",
                isSynced = false,
                timestamp = now - 1800000
            ),
            ProduceBatchEntity(
                id = "batch-004",
                batchCode = "BATCH-LOF-2026-015",
                farmerId = "farm-004",
                farmerName = "Tarnue Weefur",
                cooperativeName = "Zorzor Agricultural FBO",
                cropType = "YAM",
                weightKg = 250.0,
                starchPercentage = 21.0,
                moisturePercentage = 65.0,
                pricePerKgLrd = 110.0,
                totalPayoutLrd = 27500.0,
                latitude = 7.7801,
                longitude = -9.4311,
                locationName = "Zorzor Feeder Depot",
                payoutStatus = "PENDING",
                momoTransactionRef = "PENDING_MOMO",
                isSynced = false,
                timestamp = now - 900000
            ),
            ProduceBatchEntity(
                id = "batch-005",
                batchCode = "BATCH-NIM-2026-009",
                farmerId = "farm-005",
                farmerName = "Cecelia Kamara",
                cooperativeName = "Sanniquellie Women Farmers Co-op",
                cropType = "CASSAVA",
                weightKg = 520.0,
                starchPercentage = 25.4,
                moisturePercentage = 60.0,
                pricePerKgLrd = 88.0,
                totalPayoutLrd = 45760.0,
                latitude = 7.3622,
                longitude = -8.9811,
                locationName = "Sanniquellie Collection Spoke",
                payoutStatus = "PENDING",
                momoTransactionRef = "PENDING_DISPATCH",
                isSynced = false,
                timestamp = now - (3600000 * 28) // 28 hours ago (overdue >24h)
            )
        )
        dao.insertProduceBatches(batches)

        val hubs = listOf(
            HubOperationEntity(
                id = "hub-ganta-01",
                hubName = "Ganta Central Processing Hub",
                county = "Nimba",
                solarCapacityKw = 15.0,
                flashDryerActive = true,
                dailyRawTons = 12.5,
                hqcfYieldTons = 3.2,
                industrialStarchTons = 1.8,
                animalFeedTons = 2.1,
                siftingMeshPassed = true,
                moistureContentPct = 8.8,
                activeCargoTrikes = 4
            ),
            HubOperationEntity(
                id = "hub-voinjama-02",
                hubName = "Voinjama Solar Flash Hub",
                county = "Lofa",
                solarCapacityKw = 12.0,
                flashDryerActive = true,
                dailyRawTons = 9.8,
                hqcfYieldTons = 2.5,
                industrialStarchTons = 1.4,
                animalFeedTons = 1.6,
                siftingMeshPassed = true,
                moistureContentPct = 9.2,
                activeCargoTrikes = 3
            )
        )
        dao.insertHubOperations(hubs)

        val floats = listOf(
            MoMoFloatEntity(
                id = "float-001",
                hubLocation = "Ganta Regional Carrier Node",
                orangeMoMoFloatLrd = 850000.0,
                mtnMoMoFloatLrd = 920000.0,
                isSufficientFloat = true,
                lastRefreshedTime = now
            ),
            MoMoFloatEntity(
                id = "float-002",
                hubLocation = "Voinjama Agent Hub Node",
                orangeMoMoFloatLrd = 620000.0,
                mtnMoMoFloatLrd = 580000.0,
                isSufficientFloat = true,
                lastRefreshedTime = now
            )
        )
        dao.insertMoMoFloats(floats)
    }
}
