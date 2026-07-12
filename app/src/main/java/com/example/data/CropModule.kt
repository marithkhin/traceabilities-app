package com.example.data

data class CalendarEvent(
    val monthRange: String, // e.g. "May-June" or "October"
    val titleEn: String,
    val titleKh: String,
    val descriptionEn: String,
    val descriptionKh: String,
    val deepLinkedSku: String? = null
)

data class QualitySchemaField(
    val key: String,
    val nameEn: String,
    val nameKh: String,
    val unit: String,
    val target: String,
    val isNumeric: Boolean = true,
    val options: List<String> = emptyList()
)

data class CropModule(
    val cropId: String, // "cashew" | "cacao" | "coffee" | "rubber" | "pepper"
    val displayNameEn: String,
    val displayNameKh: String,
    val enabled: Boolean,
    val eudrRegulated: Boolean,
    val geminiSystemPrompt: String,
    val inputCatalogIds: List<String>,
    val qualitySchemaFields: List<QualitySchemaField>,
    val seasonalCalendar: List<CalendarEvent>,
    val provinces: List<String>
)

object CropRegistry {
    val modules = listOf(
        // Cashew (v1 seed, fully active)
        CropModule(
            cropId = "cashew",
            displayNameEn = "Cashew Nut",
            displayNameKh = "ស្វាយចន្ទី",
            enabled = true,
            eudrRegulated = false,
            geminiSystemPrompt = """
                You are a cashew agronomy expert specialized in Cambodian growing conditions (Ratanakiri, Mondulkiri, Kampong Thom, Preah Vihear). Diagnose common cashew issues: anthracnose, tea mosquito bug, powdery mildew, nitrogen/zinc/boron deficiency, drought stress. Reply in structured JSON. Provide plain-language Khmer summary. Only recommend products in the NormAgri catalog. If uncertain, request a clearer photo.
            """.trimIndent(),
            inputCatalogIds = listOf(
                "FERT-NPK-50KG",
                "FERT-DOLOMITE-40KG",
                "BIO-KEN-KOJI-20KG",
                "BIO-MYCO-1KG",
                "KEN-MICRO-1L",
                "FUNG-COPPER-1KG",
                "PROT-NEEM-1L",
                "TOOL-SPRAYER-16L",
                "TOOL-PRUNER-LONG"
            ),
            qualitySchemaFields = listOf(
                QualitySchemaField("moisturePct", "Moisture %", "កម្រិតសំណើម %", "%", "target ≤ 10.0%"),
                QualitySchemaField("defectPct", "Defect %", "កម្រិតខូចគុណភាព %", "%", "target ≤ 3.0%"),
                QualitySchemaField("grade", "Grade", "កម្រិតថ្នាក់", "", "W180 / W210 / W240 / W320", isNumeric = false, options = listOf("W180", "W210", "W240", "W320", "Reject"))
            ),
            seasonalCalendar = listOf(
                CalendarEvent(
                    monthRange = "May-June",
                    titleEn = "Pre-Monsoon Soil Fertilization",
                    titleKh = "ការដាក់ជីដីមុនរដូវវស្សា",
                    descriptionEn = "Apply balanced NPK fertilizer with dolomite soil amendment to build mineral reserves.",
                    descriptionKh = "ដាក់ជីគីមីតុល្យភាពជាមួយកំបោរកសិកម្មដូឡូមីត ដើម្បីបង្កើនជីវជាតិដី។",
                    deepLinkedSku = "FERT-NPK-50KG"
                ),
                CalendarEvent(
                    monthRange = "October",
                    titleEn = "Flower Initiation Split",
                    titleKh = "ការជំរុញការចេញផ្កា",
                    descriptionEn = "Apply second NPK split and perform first protective foliar spray to maximize flower retention.",
                    descriptionKh = "ដាក់ជីគីមី NPK លើកទីពីរ និងបាញ់ទឹកបំប៉នមីក្រូសារធាតុ ដើម្បីទប់ទល់ការជ្រុះក្តឹប។",
                    deepLinkedSku = "KEN-MICRO-1L"
                ),
                CalendarEvent(
                    monthRange = "November-December",
                    titleEn = "Fruit Set Protection",
                    titleKh = "ការការពារក្តឹបផ្លែ",
                    descriptionEn = "Perform preventive copper fungicide application to protect young fruit and nuts from Anthracnose.",
                    descriptionKh = "បាញ់ថ្នាំការពារផ្សិតស្ពាន់ ដើម្បីការពារក្តឹប និងគ្រាប់ខ្ចីពីជំងឺអង់ត្រាក់ណូស។",
                    deepLinkedSku = "FUNG-COPPER-1KG"
                ),
                CalendarEvent(
                    monthRange = "January",
                    titleEn = "Nut Development Spray",
                    titleKh = "ការបាញ់ថែគ្រាប់ចន្ទី",
                    descriptionEn = "Apply foliar micronutrients to boost kernel weight and reduce drought stress symptoms.",
                    descriptionKh = "បាញ់សារធាតុបំប៉នបន្ថែម ដើម្បីបង្កើនទម្ងន់គ្រាប់ និងកាត់បន្ថយភាពស្រ្តេសពីកម្តៅរាំងស្ងួត។",
                    deepLinkedSku = "KEN-MICRO-1L"
                ),
                CalendarEvent(
                    monthRange = "February-May",
                    titleEn = "Harvest and Drying window",
                    titleKh = "រដូវប្រមូលផល និងហាលគ្រាប់",
                    descriptionEn = "Gather fallen mature cashew nuts, separate the apple, and sun-dry nuts on clean tarpaulins for 3 days.",
                    descriptionKh = "ប្រមូលផលគ្រាប់ចន្ទីទុំ ញែកផ្លែចេញ និងហាលថ្ងៃលើកម្រាលស្អាតរយៈពេល ៣ថ្ងៃ។",
                    deepLinkedSku = "TOOL-SPRAYER-16L"
                )
            ),
            provinces = listOf("Kampong Thom", "Preah Vihear", "Ratanakiri", "Mondulkiri", "Kratie")
        ),
        
        // Cacao (v2 crop, activated as Phase 5 Demonstration)
        CropModule(
            cropId = "cacao",
            displayNameEn = "Cacao (Cocoa)",
            displayNameKh = "កាកាវ",
            enabled = true,
            eudrRegulated = true,
            geminiSystemPrompt = """
                You are a cacao agronomy expert specialized in Cambodian growing conditions (Mondulkiri, Ratanakiri, Kampong Cham). Diagnose common cacao issues: black pod disease (Phytophthora), witches' broom, capsid bug, cacao pod borer, cadmium uptake concerns, shade management issues. Reply in structured JSON. Provide plain-language Khmer summary. Only recommend products in the NormAgri catalog. If uncertain, request a clearer photo.
            """.trimIndent(),
            inputCatalogIds = listOf(
                "FERT-CACAO-POD",
                "FUNG-CACAO-BLACK",
                "POST-CACAO-YEAST",
                "TOOL-CACAO-CUTTER",
                "BIO-CACAO-COMPOST",
                "TOOL-SPRAYER-16L",
                "PROT-NEEM-1L"
            ),
            qualitySchemaFields = listOf(
                QualitySchemaField("beanCount", "Bean Count / 100g", "ចំនួនគ្រាប់ក្នុង ១០០ក្រាម", "beans", "target 90-110 beans"),
                QualitySchemaField("moisturePct", "Moisture %", "កម្រិតសំណើម %", "%", "target 6.0% - 8.0%"),
                QualitySchemaField("fermentIndex", "Fermentation Index (Cut Test)", "សន្ទស្សន៍នៃការផ្អាប់", "", "Scale 1-3 (Target 3)", isNumeric = false, options = listOf("1 (Low)", "2 (Medium)", "3 (Fully Fermented)"))
            ),
            seasonalCalendar = listOf(
                CalendarEvent(
                    monthRange = "June-July",
                    titleEn = "Heavy Pruning & Shade Control",
                    titleKh = "ការកាត់តម្រឹមមែក និងការគ្រប់គ្រងម្លប់",
                    descriptionEn = "Prune vertical chupons and overlapping branches to maintain 30-40% canopy shade. Reduces moisture build-up.",
                    descriptionKh = "កាត់តម្រឹមមែកកាកាវចាស់ៗ និងកាត់បន្ថយម្លប់មកត្រឹម ៣០-៤០% ដើម្បីកាត់បន្ថយការបង្កាត់ជំងឺផ្សិត។",
                    deepLinkedSku = "TOOL-CACAO-CUTTER"
                ),
                CalendarEvent(
                    monthRange = "August-October",
                    titleEn = "Black Pod Preventive Fungicide",
                    titleKh = "ការការពារជំងឺរលួយផ្លែកាកាវ",
                    descriptionEn = "Spray crop-protection fungicide directly on developing pods during wet seasons to prevent Phytophthora outbreak.",
                    descriptionKh = "បាញ់ថ្នាំការពារផ្សិតលើផ្លែកាកាវខ្ចីៗ អំឡុងពេលរដូវភ្លៀង ដើម្បីទប់ស្កាត់ជំងឺរលួយផ្លែខ្មៅ។",
                    deepLinkedSku = "FUNG-CACAO-BLACK"
                ),
                CalendarEvent(
                    monthRange = "November-December",
                    titleEn = "Organic Soil Composting",
                    titleKh = "ការដាក់ជីកំប៉ុស្តសរីរាង្គកាកាវ",
                    descriptionEn = "Spread organic compost activated with Trichoderma around root zone to improve moisture holding and fight soil-borne pathogens.",
                    descriptionKh = "ដាក់ជីកំប៉ុស្តសរីរាង្គដែលលាយជាមួយពពួកផ្សិតល្អ Trichoderma នៅជុំវិញគល់កាកាវ ដើម្បីកម្ចាត់មេរោគក្នុងដី។",
                    deepLinkedSku = "BIO-CACAO-COMPOST"
                ),
                CalendarEvent(
                    monthRange = "Continuous",
                    titleEn = "Harvesting and Sweatbox Fermentation",
                    titleKh = "ការប្រមូលផល និងការផ្អាប់ទុកក្នុងប្រអប់",
                    descriptionEn = "Harvest fully yellow/orange pods, extract beans, and place in wooden sweatbox with ChocoFerment yeast for 6 days of fermentation.",
                    descriptionKh = "បេះផ្លែទុំពណ៌លឿងក្រហម យកគ្រាប់មកផ្អាប់ក្នុងប្រអប់ឈើដោយប្រើមេ ChocoFerment រយៈពេល ៦ថ្ងៃ ដើម្បីបង្កើនក្លិនឈ្ងុយ។",
                    deepLinkedSku = "POST-CACAO-YEAST"
                )
            ),
            provinces = listOf("Mondulkiri", "Ratanakiri", "Kampong Cham", "Kratie")
        ),
        
        // Coffee (v3, stubbed)
        CropModule(
            cropId = "coffee",
            displayNameEn = "Coffee (Mondulkiri)",
            displayNameKh = "កាហ្វេ (មណ្ឌលគីរី)",
            enabled = false,
            eudrRegulated = true,
            geminiSystemPrompt = "You are a coffee agronomy expert. Diagnose rust and borer issues for Robusta and Arabica in Cambodia.",
            inputCatalogIds = emptyList(),
            qualitySchemaFields = listOf(
                QualitySchemaField("moisturePct", "Moisture %", "កម្រិតសំណើម %", "%", "target ≤ 12%"),
                QualitySchemaField("defectCount", "Defect Count / 300g", "ចំនួនគ្រាប់ខូចក្នុង ៣០០ក្រាម", "defects", "target ≤ 5")
            ),
            seasonalCalendar = emptyList(),
            provinces = listOf("Mondulkiri")
        ),
        
        // Rubber (v4, stubbed)
        CropModule(
            cropId = "rubber",
            displayNameEn = "Natural Rubber",
            displayNameKh = "កៅស៊ូធម្មជាតិ",
            enabled = false,
            eudrRegulated = true,
            geminiSystemPrompt = "You are a rubber plantation expert. Diagnose leaf blight and bark dry out issues in Cambodia.",
            inputCatalogIds = emptyList(),
            qualitySchemaFields = listOf(
                QualitySchemaField("drc", "Dry Rubber Content % (DRC)", "ភាគរយសាច់កៅស៊ូស្ងួត", "%", "target 33% - 40%")
            ),
            seasonalCalendar = emptyList(),
            provinces = listOf("Kratie", "Kampong Cham", "Preah Vihear")
        ),
        
        // Pepper (v5, stubbed)
        CropModule(
            cropId = "pepper",
            displayNameEn = "Kampot Pepper (GI)",
            displayNameKh = "ម្រេចកំពត (ម៉ាកសម្គាល់ភូមិសាស្ត្រ GI)",
            enabled = false,
            eudrRegulated = false,
            geminiSystemPrompt = "You are a GI Kampot pepper expert. Diagnose root rot and thrips issues.",
            inputCatalogIds = emptyList(),
            qualitySchemaFields = listOf(
                QualitySchemaField("density", "Bulk Density (g/l)", "ដង់ស៊ីតេគ្រាប់ (ក្រាម/លីត្រ)", "g/l", "target ≥ 570 g/l"),
                QualitySchemaField("moisturePct", "Moisture %", "កម្រិតសំណើម %", "%", "target ≤ 12%")
            ),
            seasonalCalendar = emptyList(),
            provinces = listOf("Kampot", "Kep")
        )
    )

    fun getModule(cropId: String): CropModule {
        return modules.firstOrNull { it.cropId == cropId } ?: modules[0]
    }
}
