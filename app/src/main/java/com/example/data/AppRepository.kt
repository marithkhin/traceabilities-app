package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {
    // --- Farmers ---
    val allFarmers: Flow<List<Farmer>> = appDao.getAllFarmers()
    
    suspend fun getFarmerById(id: String): Farmer? = appDao.getFarmerById(id)
    suspend fun getFarmerByPhone(phone: String): Farmer? = appDao.getFarmerByPhone(phone)
    suspend fun insertFarmer(farmer: Farmer) = appDao.insertFarmer(farmer)
    suspend fun updateFarmer(farmer: Farmer) = appDao.insertFarmer(farmer)

    // --- Plots ---
    fun getPlotsForFarmer(farmerId: String): Flow<List<Plot>> = appDao.getPlotsForFarmer(farmerId)
    suspend fun getPlotsForFarmerSync(farmerId: String): List<Plot> = appDao.getPlotsForFarmerSync(farmerId)
    suspend fun insertPlot(plot: Plot) = appDao.insertPlot(plot)

    // --- Diagnoses ---
    val allDiagnoses: Flow<List<Diagnosis>> = appDao.getAllDiagnoses()
    fun getDiagnosesForFarmer(farmerId: String): Flow<List<Diagnosis>> = appDao.getDiagnosesForFarmer(farmerId)
    suspend fun insertDiagnosis(diagnosis: Diagnosis) = appDao.insertDiagnosis(diagnosis)

    // --- Products ---
    val allProducts: Flow<List<Product>> = appDao.getAllProducts()
    suspend fun getProductBySku(sku: String): Product? = appDao.getProductBySku(sku)

    // --- Input Orders ---
    val allInputOrders: Flow<List<InputOrder>> = appDao.getAllInputOrders()
    fun getInputOrdersForFarmer(farmerId: String): Flow<List<InputOrder>> = appDao.getInputOrdersForFarmer(farmerId)
    suspend fun insertInputOrder(order: InputOrder) = appDao.insertInputOrder(order)
    suspend fun updateInputOrder(order: InputOrder) = appDao.updateInputOrder(order)

    // --- Sell Offers ---
    val allSellOffers: Flow<List<SellOffer>> = appDao.getAllSellOffers()
    fun getSellOffersForFarmer(farmerId: String): Flow<List<SellOffer>> = appDao.getSellOffersForFarmer(farmerId)
    suspend fun getSellOfferById(id: String): SellOffer? = appDao.getSellOfferById(id)
    suspend fun insertSellOffer(offer: SellOffer) = appDao.insertSellOffer(offer)
    suspend fun updateSellOffer(offer: SellOffer) = appDao.updateSellOffer(offer)

    // --- Intake Records ---
    val allIntakeRecords: Flow<List<IntakeRecord>> = appDao.getAllIntakeRecords()
    fun getIntakeRecordsForAggregator(aggregatorId: String): Flow<List<IntakeRecord>> = appDao.getIntakeRecordsForAggregator(aggregatorId)
    suspend fun getIntakeRecordById(id: String): IntakeRecord? = appDao.getIntakeRecordById(id)
    suspend fun insertIntakeRecord(record: IntakeRecord) = appDao.insertIntakeRecord(record)

    // --- Batches ---
    val allBatches: Flow<List<Batch>> = appDao.getAllBatches()
    fun getBatchesForAggregator(aggregatorId: String): Flow<List<Batch>> = appDao.getBatchesForAggregator(aggregatorId)
    suspend fun insertBatch(batch: Batch) = appDao.insertBatch(batch)

    // --- Lots ---
    val allLots: Flow<List<Lot>> = appDao.getAllLots()
    suspend fun getLotById(id: String): Lot? = appDao.getLotById(id)
    suspend fun insertLot(lot: Lot) = appDao.insertLot(lot)

    // --- Buyer Orders ---
    val allBuyerOrders: Flow<List<BuyerOrder>> = appDao.getAllBuyerOrders()
    fun getBuyerOrders(buyerId: String): Flow<List<BuyerOrder>> = appDao.getBuyerOrders(buyerId)
    suspend fun insertBuyerOrder(order: BuyerOrder) = appDao.insertBuyerOrder(order)
    suspend fun updateBuyerOrder(order: BuyerOrder) = appDao.updateBuyerOrder(order)

    // --- Traceability Records ---
    suspend fun getTraceabilityForLot(lotId: String): TraceabilityRecord? = appDao.getTraceabilityForLot(lotId)
    suspend fun insertTraceabilityRecord(record: TraceabilityRecord) = appDao.insertTraceabilityRecord(record)

    // --- Aggregators ---
    val allAggregators: Flow<List<Aggregator>> = appDao.getAllAggregators()
    suspend fun insertAggregator(aggregator: Aggregator) = appDao.insertAggregator(aggregator)

    // --- Buyers ---
    val allBuyers: Flow<List<Buyer>> = appDao.getAllBuyers()
    suspend fun insertBuyer(buyer: Buyer) = appDao.insertBuyer(buyer)
}
