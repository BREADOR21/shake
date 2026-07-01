package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spendings")
data class SpendingEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val description: String,
    val category: String,
    val isImpulse: Boolean, // true = anlık, false = planlı
    val verdict: String, // "green", "yellow", "red"
    val isAdded: Boolean = true, // true = added (Yine de Ekle), false = avoided (Vazgeç)
    val timestamp: Long = System.currentTimeMillis(),
    val emotion: String = "🙂 Mantıklı", // "😌 Sakin", "🙂 Mantıklı", "😐 Kararsız", "😵 Dürtüsel", "🔥 Aşırı İstek"
    val regretProbability: String = "low", // "low", "medium", "high"
    val stillWantState: String = "PENDING" // "PENDING", "YES", "NO" (for Keşke Listesi)
)
