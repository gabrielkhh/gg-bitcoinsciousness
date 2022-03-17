package com.nghanyi.bitcoinsciousness

data class Price(
    val date: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
)
