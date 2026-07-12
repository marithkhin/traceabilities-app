package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        Farmer::class,
        Plot::class,
        Diagnosis::class,
        Product::class,
        InputOrder::class,
        SellOffer::class,
        IntakeRecord::class,
        Batch::class,
        Lot::class,
        BuyerOrder::class,
        TraceabilityRecord::class,
        Aggregator::class,
        Buyer::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "normcashew_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed database in background thread
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                seedData(database.appDao())
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedData(dao: AppDao) {
            // 1. Seed Products (catalog items)
            val products = listOf(
                // Cacao Products
                Product(
                    sku = "FERT-CACAO-POD",
                    nameEn = "Cocoa-Boost Organic Fertilizer (12-11-18)",
                    nameKh = "ជីសរីរាង្គកាកាវ Cocoa-Boost (១២-១១-១៨)",
                    category = "Fertilizers",
                    priceUsd = 22.00,
                    priceKhr = 88000,
                    descriptionEn = "Optimizes pod count and bean weight per pod. Rich in organic matter.",
                    descriptionKh = "ជួយបង្កើនចំនួនផ្លែ និងទម្ងន់គ្រាប់កាកាវក្នុងមួយផ្លែ។ សម្បូរទៅដោយសារធាតុសរីរាង្គ។",
                    imageResName = "cacao_fert"
                ),
                Product(
                    sku = "FUNG-CACAO-BLACK",
                    nameEn = "Ridomil Gold Black Pod Protection",
                    nameKh = "ថ្នាំការពារជំងឺផ្លែខ្មៅ Ridomil Gold",
                    category = "Crop protection",
                    priceUsd = 14.00,
                    priceKhr = 56000,
                    descriptionEn = "Copper-mancozeb fungicide targeting Phytophthora pod rot.",
                    descriptionKh = "ថ្នាំបាញ់ការពារ និងកម្ចាត់ជំងឺផ្សិតរលួយផ្លែខ្មៅកាកាវ។",
                    imageResName = "cacao_fung"
                ),
                Product(
                    sku = "POST-CACAO-YEAST",
                    nameEn = "ChocoFerment Starter Culture Yeast",
                    nameKh = "មេផ្សិតផ្អាប់ ChocoFerment សម្រាប់កាកាវ",
                    category = "Biofertilizers",
                    priceUsd = 6.50,
                    priceKhr = 26000,
                    descriptionEn = "Premium yeast strain for sweatbox fermentation to enhance chocolate aroma.",
                    descriptionKh = "មេផ្សិតគុណភាពខ្ពស់សម្រាប់ដាក់ផ្អាប់ក្នុងប្រអប់ ដើម្បីបង្កើនក្លិនឈ្ងុយសូកូឡា។",
                    imageResName = "cacao_yeast"
                ),
                Product(
                    sku = "TOOL-CACAO-CUTTER",
                    nameEn = "Serrated Cocoa Pod Harvesting Hook",
                    nameKh = "កាំបិតកោងសម្រាប់បេះផ្លែកាកាវ",
                    category = "Tools",
                    priceUsd = 10.00,
                    priceKhr = 40000,
                    descriptionEn = "Curved blade for clean cuts without damaging flower cushions.",
                    descriptionKh = "កាំបិតកោងសម្រាប់បេះផ្លែទុំកាកាវបានស្អាតល្អ ដោយមិនប៉ះពាល់ដល់ភ្នែកផ្កាថ្មី។",
                    imageResName = "cacao_cutter"
                ),
                Product(
                    sku = "BIO-CACAO-COMPOST",
                    nameEn = "Trichoderma Bio-Compost Activator",
                    nameKh = "ថ្នាំលាយជីកំប៉ុស្ត Trichoderma សម្រាប់ដី",
                    category = "Biofertilizers",
                    priceUsd = 12.50,
                    priceKhr = 50000,
                    descriptionEn = "Promotes rapid leaf litter breakdown to prevent black pod spores.",
                    descriptionKh = "ជួយរំលាយស្លឹករលួយក្នុងចម្ការបានលឿន និងទប់ស្កាត់ការរីករាលដាលនៃមេរោគផ្សិតផ្លែខ្មៅ។",
                    imageResName = "cacao_compost"
                ),
                // Cashew Products
                Product(
                    sku = "FERT-NPK-50KG",
                    nameEn = "NPK Cashew Special (15-15-15 + S + Zn)",
                    nameKh = "ជីគីមីចម្រុះ NPK ពិសេសសម្រាប់ស្វាយចន្ទី (១៥-១៥-១៥)",
                    category = "Fertilizers",
                    priceUsd = 28.50,
                    priceKhr = 114000,
                    descriptionEn = "Balanced pre-monsoon and fruit set fertilizer with added sulfur and zinc to boost nut size.",
                    descriptionKh = "ជីគីមីតុល្យភាពសម្រាប់បាញ់មុនរដូវវស្សា និងពេលផ្លែ ដើម្បីបង្កើនទំហំគ្រាប់។",
                    imageResName = "fertilizer"
                ),
                Product(
                    sku = "FERT-DOLOMITE-40KG",
                    nameEn = "Premium Dolomite Soil Amendment",
                    nameKh = "កំបោរកសិកម្មដូឡូមីត ប្រណិត",
                    category = "Fertilizers",
                    priceUsd = 9.00,
                    priceKhr = 36000,
                    descriptionEn = "Reduces soil acidity, improves calcium and magnesium absorption in cashew plantations.",
                    descriptionKh = "កាត់បន្ថយអាស៊ីតដី ជួយដល់ការបឺតស្រូបជាតិកាល់ស្យូម និងម៉ាញេស្យូម។",
                    imageResName = "dolomite"
                ),
                Product(
                    sku = "BIO-KEN-KOJI-20KG",
                    nameEn = "Kenkoshoku Koji Organic Amendment",
                    nameKh = "ជីសរីរាង្គកូជី Kenkoshoku បច្ចេកវិទ្យាជប៉ុន",
                    category = "Biofertilizers",
                    priceUsd = 16.00,
                    priceKhr = 64000,
                    descriptionEn = "Japan-tech koji-fermented organic fertilizer to activate soil biology and build humic content.",
                    descriptionKh = "ជីសរីរាង្គបច្ចេកវិទ្យាជប៉ុន បង្កើនជីវជាតិដី និងសកម្មភាពមីក្រូសារពាង្គកាយដី។",
                    imageResName = "koji"
                ),
                Product(
                    sku = "BIO-MYCO-1KG",
                    nameEn = "Bio-Consortium Mycorrhizal Inoculant",
                    nameKh = "ថ្នាំផ្សំមីកូរីហ្សាជីវសាស្ត្រ",
                    category = "Biofertilizers",
                    priceUsd = 14.50,
                    priceKhr = 58000,
                    descriptionEn = "Azospirillum + PSB + AMF consortium to improve root development and drought resilience.",
                    descriptionKh = "ជួយបណ្តុះប្រព័ន្ធឫស និងបង្កើនភាពធន់នឹងការរាំងស្ងួត។",
                    imageResName = "myco"
                ),
                Product(
                    sku = "KEN-MICRO-1L",
                    nameEn = "Kenkoshoku Micronutrient Spray (Zn + B + Mg)",
                    nameKh = "ទឹកបាញ់បំប៉នមីក្រូសារធាតុ Kenkoshoku (Zn + B + Mg)",
                    category = "Micronutrients",
                    priceUsd = 12.00,
                    priceKhr = 48000,
                    descriptionEn = "Foliar spray rich in zinc and boron. Critical for flower retention, nut setting, and preventing blank nuts.",
                    descriptionKh = "ជីបាញ់ស្លឹកសម្បូរទៅដោយស័ង្កសី និងបូរ៉ុង ជំនួយដល់ការចេញផ្កា និងទប់ទល់ការជ្រុះក្តឹប។",
                    imageResName = "micronutrient"
                ),
                Product(
                    sku = "FUNG-COPPER-1KG",
                    nameEn = "Kocide 3000 Copper Hydroxide Fungicide",
                    nameKh = "ថ្នាំកម្ចាត់ផ្សិតស្ពាន់ Kocide 3000",
                    category = "Crop protection",
                    priceUsd = 8.00,
                    priceKhr = 32000,
                    descriptionEn = "Highly effective protective fungicide for prevention of Anthracnose disease and pink disease.",
                    descriptionKh = "ថ្នាំការពារ និងកម្ចាត់ជំងឺផ្សិតអង់ត្រាក់ណូស និងផ្សិតពណ៌ស៊ីជម្ពូ។",
                    imageResName = "fungicide"
                ),
                Product(
                    sku = "PROT-NEEM-1L",
                    nameEn = "Pure Organic Cold-Pressed Neem Oil",
                    nameKh = "ប្រេងគ្រាប់ស្តៅធម្មជាតិត្រជាក់",
                    category = "Crop protection",
                    priceUsd = 11.00,
                    priceKhr = 44000,
                    descriptionEn = "Organic insect repellent targeting cashew stem borer and thrips without chemical residues.",
                    descriptionKh = "ថ្នាំបណ្តេញសត្វល្អិតធម្មជាតិ ការពារដង្កូវស៊ីធ្លុះដើម និងតឹកស៊ីផ្កា។",
                    imageResName = "neem"
                ),
                Product(
                    sku = "TOOL-SPRAYER-16L",
                    nameEn = "Knapsack Battery-Powered Sprayer (16L)",
                    nameKh = "ធុងបាញ់ថ្នាំប្រើអាគុយ ១៦លីត្រ",
                    category = "Tools",
                    priceUsd = 25.00,
                    priceKhr = 100000,
                    descriptionEn = "Rechargeable high-pressure battery sprayer for uniform canopy application. 6-hour battery life.",
                    descriptionKh = "ធុងបាញ់សម្ពាធខ្ពស់ប្រើអាគុយ កម្លាំងបាញ់ស្មើគ្នាបានល្អ ថ្មកាន់បាន ៦ម៉ោង។",
                    imageResName = "sprayer"
                ),
                Product(
                    sku = "TOOL-PRUNER-LONG",
                    nameEn = "Telescopic Long-Reach Canopy Pruner",
                    nameKh = "កន្ត្រៃកាត់មែកកម្ពស់ខ្ពស់ កែវពន្លូតបាន",
                    category = "Tools",
                    priceUsd = 18.00,
                    priceKhr = 72000,
                    descriptionEn = "Extendable pruner to maintain open canopy, improve air circulation, and prune deadwood.",
                    descriptionKh = "កន្ត្រៃពន្លូតសម្រាប់កាត់មែកចាស់ៗ បង្កើនការចរន្តខ្យល់ និងកាត់បន្ថយជំងឺផ្សិត។",
                    imageResName = "pruner"
                )
            )
            dao.insertProducts(products)

            // 2. Seed Farmers (20 Farmers)
            val provinces = listOf("Kampong Thom", "Preah Vihear", "Ratanakiri", "Mondulkiri", "Kratie")
            val firstNamesEn = listOf("Sok", "Chann", "Vibol", "Dara", "Veasna", "Sophea", "Bory", "Phearith", "Narin", "Piseth", "Sina", "Theara", "Rithy", "Sophal", "Sothy", "Kosal", "Vanna", "Sarath", "Moni", "Srey")
            val lastNamesEn = listOf("Seng", "Keo", "Chan", "Lim", "Meng", "Nguon", "Ouk", "Prum", "Rath", "Sam", "Te", "Chea", "Duong", "Hak", "In", "Khim", "Long", "Mao", "Phon", "Sok")

            val firstNamesKh = listOf("សុខ", "ចាន់", "វិបុល", "ដារ៉ា", "វាសនា", "សុភា", "បូរី", "ភារិទ្ធ", "ណារិន", "ពិសិដ្ឋ", "ស៊ីណា", "ធារ៉ា", "រិទ្ធី", "សុផល", "សុធី", "កុសល", "វណ្ណា", "សារ៉ាត់", "មុន្នី", "ស្រី")
            val lastNamesKh = listOf("សេង", "កែវ", "ចាន់", "លីម", "ម៉េង", "ងួន", "អ៊ុក", "ព្រហ្ម", "រ័ត្ន", "សាម", "តេ", "ជា", "ដួង", "ហាក់", "អ៊ិន", "ឃីម", "ឡុង", "ម៉ៅ", "ផុន", "សុខ")

            val farmers = mutableListOf<Farmer>()
            val plots = mutableListOf<Plot>()

            for (i in 0 until 20) {
                val id = "FARM-${100 + i}"
                val firstNameEn = firstNamesEn[i]
                val lastNameEn = lastNamesEn[i]
                val nameEn = "$firstNameEn $lastNameEn"
                
                val firstNameKh = firstNamesKh[i]
                val lastNameKh = lastNamesKh[i]
                val nameKh = "$lastNameKh $firstNameKh" // Khmer style: Last first

                val phone = "855${70000000 + i * 12345}"
                val province = provinces[i % provinces.size]
                val village = "Village ${i + 1}"
                val trustScore = 75 + (i * 37) % 25

                farmers.add(
                    Farmer(
                        id = id,
                        name = "$nameKh ($nameEn)",
                        phone = phone,
                        village = village,
                        province = province,
                        joinDate = "2025-05-10",
                        trustScore = trustScore
                    )
                )

                // Add Plot for each farmer
                val lat = 12.5 + (i * 0.15)
                val lng = 104.8 + (i * 0.12)
                val size = 1.5 + (i * 0.4) % 4.0
                val treeCount = (size * 180).toInt()
                val polygon = """
                    {"type":"Polygon","coordinates":[[[$lng,$lat],[$lng+0.01,$lat],[$lng+0.01,$lat+0.01],[$lng,$lat+0.01],[$lng,$lat]]]}
                """.trimIndent().replace("+", "")

                val isCacaoFarmer = (i % 3 == 1) && (province == "Mondulkiri" || province == "Ratanakiri")
                plots.add(
                    Plot(
                        id = "PLOT-${500 + i}",
                        farmerId = id,
                        cropType = if (isCacaoFarmer) "cacao" else "cashew",
                        polygonGeojson = polygon,
                        areaHa = Math.round(size * 10) / 10.0,
                        treeCount = treeCount,
                        variety = if (isCacaoFarmer) "Trinitario" else (if (i % 3 == 0) "M23" else "M20"),
                        plantingYear = 2018 + (i % 6)
                    )
                )
            }
            dao.insertFarmers(farmers)
            dao.insertPlots(plots)

            // 3. Seed Aggregators (10 aggregators)
            val aggregators = listOf(
                Aggregator("AGGR-01", "Kampong Thom Central collection", "មជ្ឈមណ្ឌលប្រមូលទិញកំពង់ធំ", "Chhouk", "Kampong Thom", 4600),
                Aggregator("AGGR-02", "Sandan Depot", "ដេប៉ូសណ្តាន់", "Sandan", "Kampong Thom", 4550),
                Aggregator("AGGR-03", "Preah Vihear Trade Center", "មជ្ឈមណ្ឌលពាណិជ្ជកម្មព្រះវិហារ", "Kulend", "Preah Vihear", 4500),
                Aggregator("AGGR-04", "Choam Ksant Depot", "ដេប៉ូជាំក្សាន្ត", "Choam Ksant", "Preah Vihear", 4450),
                Aggregator("AGGR-05", "Banlung Cashew Hub", "មជ្ឈមណ្ឌលស្វាយចន្ទីបានលុង", "Banlung", "Ratanakiri", 4700),
                Aggregator("AGGR-06", "O'Chum Collection Station", "ស្ថានីយប្រមូលទិញអូរជុំ", "O'Chum", "Ratanakiri", 4650),
                Aggregator("AGGR-07", "Senmonorom Warehouse", "ឃ្លាំងសែនមនោរម្យ", "Senmonorom", "Mondulkiri", 4680),
                Aggregator("AGGR-08", "Koh Nhek Depot", "ដេប៉ូកោះញែក", "Koh Nhek", "Mondulkiri", 4600),
                Aggregator("AGGR-09", "Kratie North Depot", "ដេប៉ូក្រចេះភាគខាងជើង", "Sambo", "Kratie", 4580),
                Aggregator("AGGR-10", "Chhlong Collection Point", "ចំណុចប្រមូលទិញឆ្លូង", "Chhlong", "Kratie", 4520)
            )
            dao.insertAggregators(aggregators)

            // 4. Seed Buyers (5 buyers)
            val buyers = listOf(
                Buyer("BUY-01", "Phnom Penh Cashew Processing JSC", "Cambodia", 1250.0),
                DoubleM_Buyer("BUY-02", "Vietnam Cashew Export Corp", "Vietnam", 4800.0),
                DoubleM_Buyer("BUY-03", "Kirirom Organic Importers", "Japan", 750.0),
                DoubleM_Buyer("BUY-04", "EuroCashew Logistics", "Netherlands", 1100.0),
                DoubleM_Buyer("BUY-05", "Cambodia Agri-Trade Co., Ltd", "Cambodia", 2100.0)
            )
            dao.insertBuyers(buyers)

            // 5. Seed 30 Lots (preloaded historical batch trace data)
            val lots = mutableListOf<Lot>()
            val certs = listOf("Organic (USDA)", "Fairtrade Certified", "EUDR Compliant Baseline", "Standard Grade")
            for (i in 1..30) {
                val lotId = "LOT-${1000 + i}"
                val volume = 2500.0 + (i * 350) % 8000
                val prov = provinces[i % provinces.size]
                val cert = certs[i % certs.size]
                val isCacao = i % 4 == 1 && (prov == "Mondulkiri" || prov == "Ratanakiri")

                val grade = if (isCacao) {
                    if (i % 2 == 0) "Fine Flavor" else "Bulk"
                } else {
                    when (i % 5) {
                        0 -> "W180"
                        1 -> "W210"
                        2 -> "W240"
                        3 -> "W320"
                        else -> "W240"
                    }
                }
                
                val qualReport = if (isCacao) {
                    """
                    {"bean_count":${92 + (i % 15)},"moisture_pct":${6.5 + (i % 3) * 0.5},"fermentation_index":3,"mold_pct":0.2,"insect_damage_pct":0.0}
                    """.trimIndent()
                } else {
                    """
                    {"moisture_pct":${8.2 + (i % 3) * 0.4},"defect_pct":${2.1 + (i % 4) * 0.3},"outturn_lbs":${48 + (i % 5)},"immature_pct":${1.2 + (i % 3) * 0.2},"mold_pct":0.0}
                    """.trimIndent()
                }

                val susAttrs = if (isCacao) {
                    """
                    {"agroforestry":true,"shade_grown":true,"intercropped_with":"Bananas & Coconut","soil_type":"Volcanic clay","carbon_index":${2.5 + i * 0.04}}
                    """.trimIndent()
                } else {
                    """
                    {"agroforestry":${i % 2 == 0},"shade_grown":${i % 3 != 0},"intercropped_with":"${if(i % 2 == 0) "Cassava & Bananas" else "None"}","soil_type":"Red basaltic soil","carbon_index":${1.8 + i * 0.05}}
                    """.trimIndent()
                }

                val sourceFarmers = (1..4).map { "FARM-${100 + (i + it) % 20}" }
                val sourceFarmerIdsJson = "[\"" + sourceFarmers.joinToString("\",\"") + "\"]"

                val centerLng = 104.8 + (i * 0.05)
                val centerLat = 12.5 + (i * 0.03)
                val polyGeo = """
                    {"type":"MultiPolygon","coordinates":[[[[$centerLng,$centerLat],[$centerLng+0.05,$centerLat],[$centerLng+0.05,$centerLat+0.05],[$centerLng,$centerLat+0.05],[$centerLng,$centerLat]]]]}
                """.trimIndent().replace("+", "")

                lots.add(
                    Lot(
                        id = lotId,
                        cropType = if (isCacao) "cacao" else "cashew",
                        batchIdsJson = "[\"BATCH-${200 + i}\", \"BATCH-${201 + i}\"]",
                        totalKg = volume,
                        grade = grade,
                        qualityReportJson = qualReport,
                        sustainabilityAttrsJson = susAttrs,
                        sourceFarmerIdsJson = sourceFarmerIdsJson,
                        aggregatedPolygonGeojson = polyGeo,
                        province = prov,
                        harvestDate = "2026-03-${10 + (i % 15)}",
                        certification = cert
                    )
                )
            }
            dao.insertLots(lots)

            // 6. Seed a few starter Sell Offers, Intake Records, and Diagnoses to show in dashboards
            dao.insertDiagnosis(
                Diagnosis(
                    id = "DIAG-START",
                    farmerId = "FARM-100",
                    plotId = "PLOT-500",
                    photoUrl = "https://example.com/cashew_leaves.jpg",
                    gps = "104.8,12.5",
                    timestamp = System.currentTimeMillis() - 86400000 * 2,
                    diagnosisText = "Anthracnose fungal infection",
                    severity = "moderate",
                    rootCause = "High humidity from dense canopy",
                    recommendedActionsJson = """
                        [{"action":"Prune affected branches immediately","urgency":"within_3_days","product":null},{"action":"Apply copper-based fungicide (Kocide 3000)","urgency":"within_7_days","product":{"sku":"FUNG-COPPER-1KG","name":"Kocide 3000 copper hydroxide","price_usd":8.00,"price_khr":32000}}]
                    """.trimIndent(),
                    khmerSummary = "ដើមស្វាយចន្ទីរបស់អ្នកមានជំងឺផ្សិត។ សូមកាត់មែកឆាប់ៗ និងបាញ់ថ្នាំសំលាប់ផ្សិត។"
                )
            )

            dao.insertSellOffer(
                SellOffer(
                    id = "OFF-START-1",
                    farmerId = "FARM-100",
                    estimatedKg = 850.0,
                    readyDate = "2026-07-15",
                    aggregationPointId = "AGGR-01",
                    status = "Pending",
                    timestamp = System.currentTimeMillis() - 3600000 * 4
                )
            )

            dao.insertIntakeRecord(
                IntakeRecord(
                    id = "INT-START-1",
                    sellOfferId = "OFF-PREV",
                    farmerId = "FARM-101",
                    aggregatorId = "AGGR-01",
                    grossKg = 1200.0,
                    moisturePct = 9.5,
                    defectPct = 3.2,
                    grade = "W210",
                    pricePaidKhr = 5460000,
                    transactionId = "TXN-WING-992384",
                    timestamp = System.currentTimeMillis() - 86400000
                )
            )
        }

        // Helper to bypass duplicate JVM signatures if compiling double-typed lists or properties
        private fun DoubleM_Buyer(id: String, name: String, country: String, verifiedTonnes: Double): Buyer {
            return Buyer(id, name, country, verifiedTonnes)
        }
    }
}
