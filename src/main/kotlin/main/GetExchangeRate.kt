package main

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import model.ExchangeRate
import utlis.Constants

suspend fun getExchangeRates(ktorClient: HttpClient): ExchangeRate {
    val response: HttpResponse = ktorClient.get(Constants.SECRETS.API.URL)
    if (response.status == HttpStatusCode.OK) {
        val json = Json { ignoreUnknownKeys = true }
        val exchangeRate = json.decodeFromString<ExchangeRate>(response.bodyAsText())
        return exchangeRate
    } else {
        throw Exception("Failed to retrieve data. Status code: ${response.status}")
    }
}
