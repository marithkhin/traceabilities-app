package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Farmers ---
    @Query("SELECT * FROM farmers ORDER BY name ASC")
    fun getAllFarmers(): Flow<List<Farmer>>

    @Query("SELECT * FROM farmers WHERE id = :id LIMIT 1")
    suspend fun getFarmerById(id: String): Farmer?

    @Query("SELECT * FROM farmers WHERE phone = :phone LIMIT 1")
    suspend fun getFarmerByPhone(phone: String): Farmer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmers(farmers: List<Farmer>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmer(farmer: Farmer)

    // --- Plots ---
    @Query("SELECT * FROM plots WHERE farmerId = :farmerId")
    fun getPlotsForFarmer(farmerId: String): Flow<List<Plot>>

    @Query("SELECT * FROM plots WHERE farmerId = :farmerId")
    suspend fun getPlotsForFarmerSync(farmerId: String): List<Plot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlots(plots: List<Plot>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlot(plot: Plot)

    // --- Diagnoses ---
    @Query("SELECT * FROM diagnoses WHERE farmerId = :farmerId ORDER BY timestamp DESC")
    fun getDiagnosesForFarmer(farmerId: String): Flow<List<Diagnosis>>

    @Query("SELECT * FROM diagnoses ORDER BY timestamp DESC")
    fun getAllDiagnoses(): Flow<List<Diagnosis>>

    @Query("SELECT * FROM diagnoses WHERE id = :id LIMIT 1")
    suspend fun getDiagnosisById(id: String): Diagnosis?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnosis(diagnosis: Diagnosis)

    // --- Products ---
    @Query("SELECT * FROM products ORDER BY category ASC, nameEn ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    // --- Input Orders ---
    @Query("SELECT * FROM input_orders WHERE farmerId = :farmerId ORDER BY timestamp DESC")
    fun getInputOrdersForFarmer(farmerId: String): Flow<List<InputOrder>>

    @Query("SELECT * FROM input_orders ORDER BY timestamp DESC")
    fun getAllInputOrders(): Flow<List<InputOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInputOrder(order: InputOrder)

    @Update
    suspend fun updateInputOrder(order: InputOrder)

    // --- Sell Offers ---
    @Query("SELECT * FROM sell_offers WHERE farmerId = :farmerId ORDER BY timestamp DESC")
    fun getSellOffersForFarmer(farmerId: String): Flow<List<SellOffer>>

    @Query("SELECT * FROM sell_offers ORDER BY timestamp DESC")
    fun getAllSellOffers(): Flow<List<SellOffer>>

    @Query("SELECT * FROM sell_offers WHERE id = :id LIMIT 1")
    suspend fun getSellOfferById(id: String): SellOffer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSellOffer(offer: SellOffer)

    @Update
    suspend fun updateSellOffer(offer: SellOffer)

    // --- Intake Records ---
    @Query("SELECT * FROM intake_records ORDER BY timestamp DESC")
    fun getAllIntakeRecords(): Flow<List<IntakeRecord>>

    @Query("SELECT * FROM intake_records WHERE aggregatorId = :aggregatorId ORDER BY timestamp DESC")
    fun getIntakeRecordsForAggregator(aggregatorId: String): Flow<List<IntakeRecord>>

    @Query("SELECT * FROM intake_records WHERE id = :id LIMIT 1")
    suspend fun getIntakeRecordById(id: String): IntakeRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntakeRecord(record: IntakeRecord)

    // --- Batches ---
    @Query("SELECT * FROM batches ORDER BY createdAt DESC")
    fun getAllBatches(): Flow<List<Batch>>

    @Query("SELECT * FROM batches WHERE aggregatorId = :aggregatorId ORDER BY createdAt DESC")
    fun getBatchesForAggregator(aggregatorId: String): Flow<List<Batch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: Batch)

    // --- Lots ---
    @Query("SELECT * FROM lots ORDER BY harvestDate DESC")
    fun getAllLots(): Flow<List<Lot>>

    @Query("SELECT * FROM lots WHERE id = :id LIMIT 1")
    suspend fun getLotById(id: String): Lot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLots(lots: List<Lot>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLot(lot: Lot)

    // --- Buyer Orders ---
    @Query("SELECT * FROM buyer_orders ORDER BY timestamp DESC")
    fun getAllBuyerOrders(): Flow<List<BuyerOrder>>

    @Query("SELECT * FROM buyer_orders WHERE buyerId = :buyerId ORDER BY timestamp DESC")
    fun getBuyerOrders(buyerId: String): Flow<List<BuyerOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuyerOrder(order: BuyerOrder)

    @Update
    suspend fun updateBuyerOrder(order: BuyerOrder)

    // --- Traceability Records ---
    @Query("SELECT * FROM traceability_records WHERE lotId = :lotId LIMIT 1")
    suspend fun getTraceabilityForLot(lotId: String): TraceabilityRecord?

    @Query("SELECT * FROM traceability_records WHERE id = :id LIMIT 1")
    suspend fun getTraceabilityRecordById(id: String): TraceabilityRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraceabilityRecord(record: TraceabilityRecord)

    // --- Aggregators ---
    @Query("SELECT * FROM aggregators ORDER BY nameEn ASC")
    fun getAllAggregators(): Flow<List<Aggregator>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAggregators(aggregators: List<Aggregator>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAggregator(aggregator: Aggregator)

    // --- Buyers ---
    @Query("SELECT * FROM buyers ORDER BY name ASC")
    fun getAllBuyers(): Flow<List<Buyer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuyers(buyers: List<Buyer>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuyer(buyer: Buyer)
}
