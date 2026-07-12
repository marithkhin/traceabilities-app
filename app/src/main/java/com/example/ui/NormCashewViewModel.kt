package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.ActionRecommend
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class NormCashewViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(database.appDao())
    private val geminiClient = GeminiClient()

    // --- State and UI Toggles ---
    private val _currentLanguage = MutableStateFlow("KM") // Default to Khmer ("KM" / "EN")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _currentUserRole = MutableStateFlow("Farmer") // Farmer, FieldAgent, Aggregator, Buyer, Admin
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    private val _selectedCrop = MutableStateFlow("cashew") // cashew, cacao, coffee, rubber, pepper
    val selectedCrop: StateFlow<String> = _selectedCrop.asStateFlow()

    private val _selectedFarmerId = MutableStateFlow("FARM-100") // Session farmer
    val selectedFarmerId: StateFlow<String> = _selectedFarmerId.asStateFlow()

    private val _selectedAggregatorId = MutableStateFlow("AGGR-01") // Session aggregator
    val selectedAggregatorId: StateFlow<String> = _selectedAggregatorId.asStateFlow()

    private val _selectedBuyerId = MutableStateFlow("BUY-01") // Session buyer
    val selectedBuyerId: StateFlow<String> = _selectedBuyerId.asStateFlow()

    private val _isUserSetupCompleted = MutableStateFlow(false)
    val isUserSetupCompleted: StateFlow<Boolean> = _isUserSetupCompleted.asStateFlow()

    // --- Dynamic Crop Module ---
    val currentCropModule: StateFlow<CropModule> = _selectedCrop
        .map { CropRegistry.getModule(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CropRegistry.getModule("cashew"))

    // --- Cart State ---
    private val _cart = MutableStateFlow<Map<String, Int>>(emptyMap()) // SKU -> Quantity
    val cart: StateFlow<Map<String, Int>> = _cart.asStateFlow()

    // --- Active Vision Diagnosis ---
    private val _isAnalyzingPhoto = MutableStateFlow(false)
    val isAnalyzingPhoto: StateFlow<Boolean> = _isAnalyzingPhoto.asStateFlow()

    private val _activeDiagnosisResult = MutableStateFlow<com.example.api.DiagnosisResult?>(null)
    val activeDiagnosisResult: StateFlow<com.example.api.DiagnosisResult?> = _activeDiagnosisResult.asStateFlow()

    // --- Hot Flows from Repository ---
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredProducts: StateFlow<List<Product>> = combine(allProducts, currentCropModule) { products, cropModule ->
        products.filter { cropModule.inputCatalogIds.contains(it.sku) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFarmers: StateFlow<List<Farmer>> = repository.allFarmers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAggregators: StateFlow<List<Aggregator>> = repository.allAggregators
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBuyers: StateFlow<List<Buyer>> = repository.allBuyers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLots: StateFlow<List<Lot>> = repository.allLots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredLots: StateFlow<List<Lot>> = combine(allLots, _selectedCrop) { lots, crop ->
        lots.filter { it.cropType == crop }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Context-dependent Reactive Flows ---
    val currentFarmer: StateFlow<Farmer?> = _selectedFarmerId
        .flatMapLatest { id -> flow { emit(repository.getFarmerById(id)) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentFarmerPlots: StateFlow<List<Plot>> = combine(_selectedFarmerId, _selectedCrop) { id, crop ->
        id to crop
    }.flatMapLatest { (id, crop) ->
        repository.getPlotsForFarmer(id).map { list -> list.filter { it.cropType == crop } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentFarmerDiagnoses: StateFlow<List<Diagnosis>> = combine(_selectedFarmerId, _selectedCrop) { id, crop ->
        id to crop
    }.flatMapLatest { (id, crop) ->
        repository.getDiagnosesForFarmer(id).map { list -> list.filter { it.cropType == crop } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentFarmerOrders: StateFlow<List<InputOrder>> = _selectedFarmerId
        .flatMapLatest { id -> repository.getInputOrdersForFarmer(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentFarmerSellOffers: StateFlow<List<SellOffer>> = combine(_selectedFarmerId, _selectedCrop) { id, crop ->
        id to crop
    }.flatMapLatest { (id, crop) ->
        repository.getSellOffersForFarmer(id).map { list -> list.filter { it.cropType == crop } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSellOffers: StateFlow<List<SellOffer>> = repository.allSellOffers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSellOffers: StateFlow<List<SellOffer>> = combine(allSellOffers, _selectedCrop) { offers, crop ->
        offers.filter { it.cropType == crop }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allIntakeRecords: StateFlow<List<IntakeRecord>> = repository.allIntakeRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredIntakeRecords: StateFlow<List<IntakeRecord>> = combine(allIntakeRecords, _selectedCrop) { records, crop ->
        records.filter { it.cropType == crop }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBatches: StateFlow<List<Batch>> = repository.allBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredBatches: StateFlow<List<Batch>> = combine(allBatches, _selectedCrop) { batches, crop ->
        batches.filter { it.cropType == crop }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBuyerOrders: StateFlow<List<BuyerOrder>> = repository.allBuyerOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInputOrders: StateFlow<List<InputOrder>> = repository.allInputOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Enforce seeding on launch in case onCreate was not triggered due to cache
        viewModelScope.launch(Dispatchers.IO) {
            val count = database.appDao().getProductBySku("FERT-CACAO-POD")
            if (count == null) {
                Log.d("NormCashewViewModel", "No seed data found, manual seed triggered")
                AppDatabase.seedData(database.appDao())
            }
        }
    }

    // --- Preferences/Routing actions ---
    fun toggleLanguage() {
        _currentLanguage.update { if (it == "KM") "EN" else "KM" }
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    fun setRole(role: String) {
        _currentUserRole.value = role
    }

    fun setSelectedFarmer(id: String) {
        _selectedFarmerId.value = id
    }

    fun setSelectedAggregator(id: String) {
        _selectedAggregatorId.value = id
    }

    fun setSelectedBuyer(id: String) {
        _selectedBuyerId.value = id
    }

    fun setSelectedCrop(cropId: String) {
        _selectedCrop.value = cropId
    }

    // --- Cart Actions ---
    fun addToCart(product: Product) {
        _cart.update { currentCart ->
            val count = currentCart[product.sku] ?: 0
            currentCart + (product.sku to count + 1)
        }
    }

    fun removeFromCart(product: Product) {
        _cart.update { currentCart ->
            val count = currentCart[product.sku] ?: 0
            if (count <= 1) {
                currentCart - product.sku
            } else {
                currentCart + (product.sku to count - 1)
            }
        }
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    // --- Order Checkout ---
    fun checkoutCart(paymentMethod: String, onComplete: () -> Unit) {
        val currentCartMap = _cart.value
        val farmerId = _selectedFarmerId.value
        if (currentCartMap.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            var totalKhr = 0L
            var totalUsd = 0.0
            val lineItemsList = mutableListOf<String>()

            currentCartMap.forEach { (sku, qty) ->
                val prod = repository.getProductBySku(sku)
                if (prod != null) {
                    totalKhr += prod.priceKhr * qty
                    totalUsd += prod.priceUsd * qty
                    lineItemsList.add("""{"sku":"$sku","nameEn":"${prod.nameEn}","nameKh":"${prod.nameKh}","quantity":$qty,"priceKhr":${prod.priceKhr},"priceUsd":${prod.priceUsd}}""")
                }
            }

            val orderId = "ORD-${System.currentTimeMillis().toString().takeLast(6)}"
            val lineItemsJson = "[" + lineItemsList.joinToString(",") + "]"

            val order = InputOrder(
                id = orderId,
                farmerId = farmerId,
                lineItemsJson = lineItemsJson,
                totalKhr = totalKhr,
                totalUsd = totalUsd,
                paymentMethod = paymentMethod,
                status = if (paymentMethod == "Input Credit") "Approved" else "Pending", // credit instant approved in prototype
                deliveryAgentId = "AGENT-01",
                timestamp = System.currentTimeMillis()
            )

            repository.insertInputOrder(order)
            _cart.value = emptyMap() // empty cart
            
            // Increment farmer trust score on inputs purchase
            val farmerObj = repository.getFarmerById(farmerId)
            if (farmerObj != null) {
                repository.insertFarmer(farmerObj.copy(trustScore = (farmerObj.trustScore + 2).coerceAtMost(100)))
            }

            launch(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    // --- AI Photo Diagnosis Flow ---
    fun runPhotoDiagnosis(bitmap: Bitmap) {
        _isAnalyzingPhoto.value = true
        _activeDiagnosisResult.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cropId = _selectedCrop.value
                val cropModule = CropRegistry.getModule(cropId)
                val result = geminiClient.analyzeCropPhoto(bitmap, cropId, cropModule.geminiSystemPrompt)
                _activeDiagnosisResult.value = result

                // Store in database
                val diagnosisId = "DIAG-${System.currentTimeMillis().toString().takeLast(6)}"
                val farmerId = _selectedFarmerId.value
                val plots = repository.getPlotsForFarmerSync(farmerId)
                val plotId = plots.firstOrNull { it.cropType == cropId }?.id ?: "PLOT-NONE"

                val recommendedActionsMapped = result.recommendedActions.map { act ->
                    """{"action":"${act.action}","urgency":"${act.urgency}","product_sku":${if (act.productSku != null) "\"${act.productSku}\"" else "null"}}"""
                }
                val actionsJson = "[" + recommendedActionsMapped.joinToString(",") + "]"

                val dbDiagnosis = Diagnosis(
                    id = diagnosisId,
                    farmerId = farmerId,
                    plotId = plotId,
                    cropType = cropId,
                    photoUrl = "https://example.com/leaf_photo_${diagnosisId}.jpg",
                    gps = "104.85,12.55", // Mock current GPS
                    timestamp = System.currentTimeMillis(),
                    diagnosisText = result.diagnosis,
                    severity = result.severity,
                    rootCause = result.rootCause,
                    recommendedActionsJson = actionsJson,
                    khmerSummary = result.khmerSummary
                )

                repository.insertDiagnosis(dbDiagnosis)

                // Improve farmer trust score due to data completeness/proactivity
                val farmerObj = repository.getFarmerById(farmerId)
                if (farmerObj != null) {
                    repository.insertFarmer(farmerObj.copy(trustScore = (farmerObj.trustScore + 3).coerceAtMost(100)))
                }

            } catch (e: Exception) {
                Log.e("NormCashewViewModel", "AI Diagnosis failed", e)
            } finally {
                _isAnalyzingPhoto.value = false
            }
        }
    }

    fun clearActiveDiagnosis() {
        _activeDiagnosisResult.value = null
    }

    // --- Sell Offer Creation ---
    fun submitSellOffer(estimatedKg: Double, readyDate: String, aggregationPointId: String, onComplete: () -> Unit) {
        val farmerId = _selectedFarmerId.value
        val cropId = _selectedCrop.value
        viewModelScope.launch(Dispatchers.IO) {
            val offerId = "OFF-${System.currentTimeMillis().toString().takeLast(6)}"
            val offer = SellOffer(
                id = offerId,
                farmerId = farmerId,
                cropType = cropId,
                estimatedKg = estimatedKg,
                readyDate = readyDate,
                aggregationPointId = aggregationPointId,
                status = "Pending",
                timestamp = System.currentTimeMillis()
            )
            repository.insertSellOffer(offer)
            launch(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    // --- Onboard Farmer ---
    fun onboardFarmer(
        name: String,
        phone: String,
        village: String,
        province: String,
        areaHa: Double,
        treeCount: Int,
        variety: String,
        onComplete: () -> Unit
    ) {
        val cropId = _selectedCrop.value
        viewModelScope.launch(Dispatchers.IO) {
            val farmerId = "FARM-${System.currentTimeMillis().toString().takeLast(4)}"
            val farmer = Farmer(
                id = farmerId,
                name = name,
                phone = phone,
                village = village,
                province = province,
                joinDate = "2026-07-12",
                trustScore = 70 // Baseline score
            )
            repository.insertFarmer(farmer)

            val plotId = "PLOT-${System.currentTimeMillis().toString().takeLast(4)}"
            // Draw visual circular-polygon near province center
            val lat = 12.5
            val lng = 104.8
            val polygon = """
                {"type":"Polygon","coordinates":[[[$lng,$lat],[$lng+0.015,$lat],[$lng+0.015,$lat+0.015],[$lng,$lat+0.015],[$lng,$lat]]]}
            """.trimIndent()

            val plot = Plot(
                id = plotId,
                farmerId = farmerId,
                cropType = cropId,
                polygonGeojson = polygon,
                areaHa = areaHa,
                treeCount = treeCount,
                variety = variety,
                plantingYear = 2020
            )
            repository.insertPlot(plot)

            launch(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    // --- Aggregator Intake Operations ---
    fun submitIntakeRecord(
        sellOfferId: String,
        farmerId: String,
        grossKg: Double,
        moisturePct: Double,
        defectPct: Double,
        grade: String,
        pricePaidKhr: Long,
        onComplete: (String) -> Unit
    ) {
        val aggregatorId = _selectedAggregatorId.value
        val cropId = _selectedCrop.value
        viewModelScope.launch(Dispatchers.IO) {
            val intakeId = "INT-${System.currentTimeMillis().toString().takeLast(6)}"
            val txnId = "TXN-WING-${(100000..999999).random()}"

            val record = IntakeRecord(
                id = intakeId,
                sellOfferId = sellOfferId,
                farmerId = farmerId,
                aggregatorId = aggregatorId,
                cropType = cropId,
                grossKg = grossKg,
                moisturePct = moisturePct,
                defectPct = defectPct,
                grade = grade,
                pricePaidKhr = pricePaidKhr,
                transactionId = txnId,
                timestamp = System.currentTimeMillis()
            )

            repository.insertIntakeRecord(record)

            // Update Sell Offer status to Completed
            if (sellOfferId != "OFF-NONE") {
                val offer = repository.getSellOfferById(sellOfferId)
                if (offer != null) {
                    repository.updateSellOffer(offer.copy(status = "Completed"))
                }
            }

            // Reward farmer trust score for selling quality yield (low moisture + low defects)
            val farmerObj = repository.getFarmerById(farmerId)
            if (farmerObj != null) {
                val scoreBump = if (moisturePct <= 9.0 && defectPct <= 3.0) 5 else 2
                repository.insertFarmer(farmerObj.copy(trustScore = (farmerObj.trustScore + scoreBump).coerceAtMost(100)))
            }

            launch(Dispatchers.Main) {
                onComplete(intakeId)
            }
        }
    }

    // --- Aggregator Batch Creation ---
    fun createTraceableBatch(selectedIntakeIds: List<String>, onComplete: () -> Unit) {
        val aggregatorId = _selectedAggregatorId.value
        val cropId = _selectedCrop.value
        if (selectedIntakeIds.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val batchId = "BATCH-${System.currentTimeMillis().toString().takeLast(4)}"
            var totalKg = 0.0
            val grades = mutableListOf<String>()

            selectedIntakeIds.forEach { id ->
                val rec = repository.getIntakeRecordById(id)
                if (rec != null) {
                    totalKg += rec.grossKg
                    grades.add(rec.grade)
                }
            }

            // Blended grade is most common grade in batch
            val defaultBlended = if (cropId == "cacao") "Fine Flavor" else "W240"
            val blendedGrade = grades.groupBy { it }.maxByOrNull { it.value.size }?.key ?: defaultBlended

            val batch = Batch(
                id = batchId,
                aggregatorId = aggregatorId,
                cropType = cropId,
                intakeRecordIdsJson = "[\"" + selectedIntakeIds.joinToString("\",\"") + "\"]",
                totalKg = totalKg,
                blendedGrade = blendedGrade,
                createdAt = System.currentTimeMillis()
            )
            repository.insertBatch(batch)

            // In prototype, batches automatically roll into a new Buyer Lot with complete traceable parameters!
            val lotId = "LOT-${System.currentTimeMillis().toString().takeLast(4)}"
            val farmerIds = mutableSetOf<String>()
            selectedIntakeIds.forEach { id ->
                val rec = repository.getIntakeRecordById(id)
                if (rec != null) farmerIds.add(rec.farmerId)
            }

            val qualReportJson = if (cropId == "cacao") {
                """
                {"bean_count":95,"moisture_pct":7.2,"fermentation_index":3,"mold_pct":0.1,"insect_damage_pct":0.0}
                """.trimIndent()
            } else {
                """
                {"moisture_pct":8.5,"defect_pct":1.8,"outturn_lbs":49,"immature_pct":1.0,"mold_pct":0.0}
                """.trimIndent()
            }

            val susAttrsJson = if (cropId == "cacao") {
                """
                {"agroforestry":true,"shade_grown":true,"intercropped_with":"Bananas & Coconut","soil_type":"Volcanic clay","carbon_index":2.4}
                """.trimIndent()
            } else {
                """
                {"agroforestry":true,"shade_grown":true,"intercropped_with":"Soybeans & Pineapples","soil_type":"Red clay basaltic","carbon_index":2.1}
                """.trimIndent()
            }

            val newLot = Lot(
                id = lotId,
                cropType = cropId,
                batchIdsJson = "[\"$batchId\"]",
                totalKg = totalKg,
                grade = blendedGrade,
                qualityReportJson = qualReportJson,
                sustainabilityAttrsJson = susAttrsJson,
                sourceFarmerIdsJson = "[\"" + farmerIds.joinToString("\",\"") + "\"]",
                aggregatedPolygonGeojson = """{"type":"MultiPolygon","coordinates":[[[104.8,12.5],[104.85,12.5],[104.85,12.55],[104.8,12.55],[104.8]]]]}""",
                province = "Kampong Thom",
                harvestDate = "2026-07-12",
                certification = if (cropId == "cacao") "Organic (USDA) & Shade Grown" else "USDA Organic & EUDR Compliant"
            )
            repository.insertLot(newLot)

            launch(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    // --- Buyer Purchase Flow ---
    fun requestBuyLot(lotId: String, onComplete: () -> Unit) {
        val buyerId = _selectedBuyerId.value
        viewModelScope.launch(Dispatchers.IO) {
            val lot = repository.getLotById(lotId) ?: return@launch
            val orderId = "BUYORD-${System.currentTimeMillis().toString().takeLast(6)}"
            val invoiceId = "INV-NC-${(1000..9999).random()}"

            val order = BuyerOrder(
                id = orderId,
                buyerId = buyerId,
                lotIdsJson = "[\"$lotId\"]",
                totalKg = lot.totalKg,
                pricePerKgUsd = 1.35, // Mock baseline rate
                status = "Completed",
                invoiceId = invoiceId,
                timestamp = System.currentTimeMillis()
            )
            repository.insertBuyerOrder(order)

            val cropId = lot.cropType
            val cropModule = CropRegistry.getModule(cropId)

            // Generate Traceability Record (EUDR DDS-compatible JSON structure)
            val traceId = "TRC-${System.currentTimeMillis().toString().takeLast(6)}"
            val ddsJson = """
                {
                  "eudr_compliance": "PASS",
                  "reference_number": "$invoiceId",
                  "commodity": "${cropModule.displayNameEn}",
                  "origin": "Cambodia",
                  "source_farm_count": 5,
                  "total_weight_kg": ${lot.totalKg},
                  "geolocations": [
                    {"latitude": 12.5, "longitude": 104.8, "polygon_points": 4}
                  ],
                  "harvest_period": "March-July 2026",
                  "deforestation_free": true,
                  "legally_produced": true
                }
            """.trimIndent()

            val traceRecord = TraceabilityRecord(
                id = traceId,
                lotId = lotId,
                buyerOrderId = orderId,
                generatedAt = System.currentTimeMillis(),
                ddsJson = ddsJson,
                pdfUrl = "https://normcashew.com/reports/traceability_${lotId}.pdf"
            )
            repository.insertTraceabilityRecord(traceRecord)

            launch(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    // --- Direct Sourcing Marketplace Logic ---
    private val _directSourcingRequests = MutableStateFlow<List<DirectSourcingRequest>>(
        listOf(
            DirectSourcingRequest(
                id = "REQ-101",
                buyerName = "Kampong Thom Cashew Processors Ltd",
                cropType = "cashew",
                targetVolumeKg = 15000.0,
                offerPriceKhr = 5450,
                province = "Kampong Thom",
                notes = "Looking for premium quality W210 raw cashew nuts. Moisture under 8%. Prompt payment upon delivery.",
                datePosted = "2026-07-10"
            ),
            DirectSourcingRequest(
                id = "REQ-102",
                buyerName = "Amru Agri Cambodia",
                cropType = "cashew",
                targetVolumeKg = 25000.0,
                offerPriceKhr = 5600,
                province = "Preah Vihear",
                notes = "Urgently sourcing USDA Organic-certified raw cashews. Willing to pay premium. Traceability polygons required.",
                datePosted = "2026-07-11"
            ),
            DirectSourcingRequest(
                id = "REQ-103",
                buyerName = "Siam Nut Traders",
                cropType = "cashew",
                targetVolumeKg = 10000.0,
                offerPriceKhr = 5300,
                province = "Kompong Cham",
                notes = "Sourcing well-dried cashew nuts. Minimum outturn 48 lbs.",
                datePosted = "2026-07-11"
            ),
            DirectSourcingRequest(
                id = "REQ-104",
                buyerName = "Mondulkiri Cacao Co-op",
                cropType = "cacao",
                targetVolumeKg = 8000.0,
                offerPriceKhr = 7400,
                province = "Mondulkiri",
                notes = "Sourcing shade-grown fermented cacao beans. High fat content desired. Organic-preferred.",
                datePosted = "2026-07-12"
            )
        )
    )
    val directSourcingRequests: StateFlow<List<DirectSourcingRequest>> = _directSourcingRequests.asStateFlow()

    fun postSourcingRequest(
        buyerName: String,
        cropType: String,
        targetVolumeKg: Double,
        offerPriceKhr: Long,
        province: String,
        notes: String
    ) {
        val newRequest = DirectSourcingRequest(
            id = "REQ-${System.currentTimeMillis().toString().takeLast(4)}",
            buyerName = buyerName,
            cropType = cropType,
            targetVolumeKg = targetVolumeKg,
            offerPriceKhr = offerPriceKhr,
            province = province,
            notes = notes,
            datePosted = "2026-07-12"
        )
        _directSourcingRequests.value = listOf(newRequest) + _directSourcingRequests.value
    }

    fun fulfillSourcingRequest(requestId: String, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentFarmerId = _selectedFarmerId.value
            val farmer = repository.getFarmerById(currentFarmerId)
            if (farmer != null) {
                val updatedFarmer = farmer.copy(trustScore = (farmer.trustScore + 5).coerceAtMost(100))
                repository.updateFarmer(updatedFarmer)
            }
            _directSourcingRequests.value = _directSourcingRequests.value.filter { it.id != requestId }
            launch(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    // --- New User Selection & Registration Logic ---
    fun registerFarmer(
        name: String,
        phone: String,
        village: String,
        province: String,
        areaHa: Double,
        treeCount: Int,
        variety: String,
        plantingYear: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = "FARM-${System.currentTimeMillis().toString().takeLast(4)}"
            val newFarmer = com.example.data.Farmer(
                id = newId,
                name = name,
                phone = phone,
                village = village,
                province = province,
                joinDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                trustScore = 80
            )
            repository.insertFarmer(newFarmer)

            // Insert a default plot for them
            val newPlot = com.example.data.Plot(
                id = "PLOT-${System.currentTimeMillis().toString().takeLast(4)}",
                farmerId = newId,
                cropType = _selectedCrop.value,
                polygonGeojson = """{"type":"Polygon","coordinates":[[[104.8,12.5],[104.85,12.5],[104.85,12.55],[104.8,12.55],[104.8,12.5]]]}""",
                areaHa = areaHa,
                treeCount = treeCount,
                variety = variety,
                plantingYear = plantingYear
            )
            repository.insertPlot(newPlot)

            _selectedFarmerId.value = newId
            _currentUserRole.value = "Farmer"
            _isUserSetupCompleted.value = true
        }
    }

    fun registerAggregator(
        nameEn: String,
        nameKh: String,
        village: String,
        province: String,
        buyPriceKhr: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = "AGGR-${System.currentTimeMillis().toString().takeLast(3)}"
            val newAggregator = com.example.data.Aggregator(
                id = newId,
                nameEn = nameEn,
                nameKh = nameKh,
                village = village,
                province = province,
                currentBuyPriceKhr = buyPriceKhr
            )
            repository.insertAggregator(newAggregator)

            _selectedAggregatorId.value = newId
            _currentUserRole.value = "Aggregator"
            _isUserSetupCompleted.value = true
        }
    }

    fun registerBuyer(
        name: String,
        country: String,
        verifiedTonnes: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = "BUY-${System.currentTimeMillis().toString().takeLast(3)}"
            val newBuyer = com.example.data.Buyer(
                id = newId,
                name = name,
                country = country,
                verifiedTonnes = verifiedTonnes
            )
            repository.insertBuyer(newBuyer)

            _selectedBuyerId.value = newId
            _currentUserRole.value = "Buyer"
            _isUserSetupCompleted.value = true
        }
    }

    fun bypassSetup() {
        _isUserSetupCompleted.value = true
    }

    fun resetSetup() {
        _isUserSetupCompleted.value = false
    }
}

data class DirectSourcingRequest(
    val id: String,
    val buyerName: String,
    val cropType: String,
    val targetVolumeKg: Double,
    val offerPriceKhr: Long,
    val province: String,
    val notes: String,
    val datePosted: String
)
