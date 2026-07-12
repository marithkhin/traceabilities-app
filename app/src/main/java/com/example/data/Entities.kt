package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farmers")
data class Farmer(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val village: String,
    val province: String,
    val joinDate: String,
    val trustScore: Int
)

@Entity(tableName = "plots")
data class Plot(
    @PrimaryKey val id: String,
    val farmerId: String,
    val cropType: String = "cashew",
    val polygonGeojson: String,
    val areaHa: Double,
    val treeCount: Int,
    val variety: String, // e.g. M23
    val plantingYear: Int
)

@Entity(tableName = "diagnoses")
data class Diagnosis(
    @PrimaryKey val id: String,
    val farmerId: String,
    val plotId: String,
    val cropType: String = "cashew",
    val photoUrl: String,
    val gps: String,
    val timestamp: Long,
    val diagnosisText: String,
    val severity: String,
    val rootCause: String,
    val recommendedActionsJson: String, // JSON string for list of actions
    val khmerSummary: String
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val sku: String,
    val nameEn: String,
    val nameKh: String,
    val category: String, // Fertilizers, Biofertilizers, Micronutrients, Crop protection, Tools
    val priceUsd: Double,
    val priceKhr: Long,
    val descriptionEn: String,
    val descriptionKh: String,
    val imageResName: String // name of local drawable or asset placeholder
)

@Entity(tableName = "input_orders")
data class InputOrder(
    @PrimaryKey val id: String,
    val farmerId: String,
    val lineItemsJson: String, // JSON list of (ProductSKU, Quantity, Price)
    val totalKhr: Long,
    val totalUsd: Double,
    val paymentMethod: String, // Cash on Delivery, Mobile Wallet, Input Credit
    val status: String, // Pending, Approved, Delivered
    val deliveryAgentId: String,
    val timestamp: Long
)

@Entity(tableName = "sell_offers")
data class SellOffer(
    @PrimaryKey val id: String,
    val farmerId: String,
    val cropType: String = "cashew",
    val estimatedKg: Double,
    val readyDate: String,
    val aggregationPointId: String,
    val status: String, // Pending, Accepted, Completed
    val timestamp: Long
)

@Entity(tableName = "intake_records")
data class IntakeRecord(
    @PrimaryKey val id: String,
    val sellOfferId: String,
    val farmerId: String,
    val aggregatorId: String,
    val cropType: String = "cashew",
    val grossKg: Double,
    val moisturePct: Double,
    val defectPct: Double,
    val grade: String, // W180, W210, W240, W320, Reject
    val pricePaidKhr: Long,
    val transactionId: String,
    val timestamp: Long
)

@Entity(tableName = "batches")
data class Batch(
    @PrimaryKey val id: String,
    val aggregatorId: String,
    val cropType: String = "cashew",
    val intakeRecordIdsJson: String, // JSON list of intake record IDs
    val totalKg: Double,
    val blendedGrade: String,
    val createdAt: Long
)

@Entity(tableName = "lots")
data class Lot(
    @PrimaryKey val id: String,
    val cropType: String = "cashew",
    val batchIdsJson: String, // JSON list of batch IDs
    val totalKg: Double,
    val grade: String,
    val qualityReportJson: String, // JSON of quality metrics (moisture, defects, outturn)
    val sustainabilityAttrsJson: String, // JSON of sustainability stats (agroforestry, organic, etc.)
    val sourceFarmerIdsJson: String, // JSON list of farmer IDs
    val aggregatedPolygonGeojson: String,
    val province: String,
    val harvestDate: String,
    val certification: String // Organic, Fairtrade, None, etc.
)

@Entity(tableName = "buyer_orders")
data class BuyerOrder(
    @PrimaryKey val id: String,
    val buyerId: String,
    val lotIdsJson: String, // JSON list of lot IDs
    val totalKg: Double,
    val pricePerKgUsd: Double,
    val status: String, // Pending, Approved, Shipped, Completed
    val invoiceId: String,
    val timestamp: Long
)

@Entity(tableName = "traceability_records")
data class TraceabilityRecord(
    @PrimaryKey val id: String,
    val lotId: String,
    val buyerOrderId: String,
    val generatedAt: Long,
    val ddsJson: String, // EUDR DDS-compatible JSON structure
    val pdfUrl: String
)

// Simple representation of Aggregators & Buyers for display/filtering
@Entity(tableName = "aggregators")
data class Aggregator(
    @PrimaryKey val id: String,
    val nameEn: String,
    val nameKh: String,
    val village: String,
    val province: String,
    val currentBuyPriceKhr: Long
)

@Entity(tableName = "buyers")
data class Buyer(
    @PrimaryKey val id: String,
    val name: String,
    val country: String,
    val verifiedTonnes: Double
)
