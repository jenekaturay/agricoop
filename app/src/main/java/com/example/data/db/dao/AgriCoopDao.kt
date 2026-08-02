package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entities.CooperativeEntity
import com.example.data.db.entities.FarmerEntity
import com.example.data.db.entities.HubOperationEntity
import com.example.data.db.entities.MoMoFloatEntity
import com.example.data.db.entities.ProduceBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgriCoopDao {

    // Cooperatives
    @Query("SELECT * FROM cooperatives ORDER BY name ASC")
    fun getAllCooperatives(): Flow<List<CooperativeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCooperatives(cooperatives: List<CooperativeEntity>)

    // Farmers
    @Query("SELECT * FROM farmers ORDER BY fullName ASC")
    fun getAllFarmers(): Flow<List<FarmerEntity>>

    @Query("SELECT * FROM farmers WHERE cooperativeId = :coopId ORDER BY fullName ASC")
    fun getFarmersByCooperative(coopId: String): Flow<List<FarmerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmer(farmer: FarmerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmers(farmers: List<FarmerEntity>)

    @Update
    suspend fun updateFarmer(farmer: FarmerEntity)

    // Produce Batches
    @Query("SELECT * FROM produce_batches ORDER BY timestamp DESC")
    fun getAllProduceBatches(): Flow<List<ProduceBatchEntity>>

    @Query("SELECT * FROM produce_batches WHERE isSynced = 0 ORDER BY timestamp ASC")
    fun getUnsyncedBatches(): Flow<List<ProduceBatchEntity>>

    @Query("SELECT * FROM produce_batches WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedBatchesList(): List<ProduceBatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduceBatch(batch: ProduceBatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduceBatches(batches: List<ProduceBatchEntity>)

    @Query("UPDATE produce_batches SET isSynced = 1 WHERE id IN (:batchIds)")
    suspend fun markBatchesAsSynced(batchIds: List<String>)

    @Query("UPDATE produce_batches SET payoutStatus = :status, momoTransactionRef = :ref WHERE id = :batchId")
    suspend fun updateBatchPayoutStatus(batchId: String, status: String, ref: String)

    @Query("DELETE FROM produce_batches WHERE id = :batchId")
    suspend fun deleteProduceBatchById(batchId: String)

    // Hub Operations
    @Query("SELECT * FROM hub_operations")
    fun getAllHubOperations(): Flow<List<HubOperationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHubOperations(hubs: List<HubOperationEntity>)

    // MoMo Floats
    @Query("SELECT * FROM momo_floats")
    fun getAllMoMoFloats(): Flow<List<MoMoFloatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoMoFloats(floats: List<MoMoFloatEntity>)

    // Remote Data Purge
    @Query("DELETE FROM farmers")
    suspend fun clearAllFarmers()

    @Query("DELETE FROM produce_batches")
    suspend fun clearAllProduceBatches()

    @Query("DELETE FROM hub_operations")
    suspend fun clearAllHubOperations()

    @Query("DELETE FROM momo_floats")
    suspend fun clearAllMoMoFloats()

    @Query("DELETE FROM cooperatives")
    suspend fun clearAllCooperatives()
}
