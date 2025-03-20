package model

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRate(
    val result: String,
    val base_code: String,
    val conversion_rates: Map<String, Double>
)