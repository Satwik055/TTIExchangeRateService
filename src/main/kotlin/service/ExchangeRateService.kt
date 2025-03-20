package service

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import model.ExchangeRate
import utlis.Constants
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("Exchange Rate Service")

object ExchangeRateService {

    private suspend fun getExchangeRates(ktorClient: HttpClient): ExchangeRate {
        val response: HttpResponse = ktorClient.get(Constants.SECRETS.API.URL)
        if (response.status == HttpStatusCode.OK) {
            val json = Json { ignoreUnknownKeys = true }
            val exchangeRate = json.decodeFromString<ExchangeRate>(response.bodyAsText())
            return exchangeRate
        } else {
            throw Exception("Failed to retrieve data. Status code: ${response.status}")
        }
    }


    suspend fun updateTtiRates(supabaseClient: SupabaseClient, ktorClient: HttpClient) {

        val exchangeRates = getExchangeRates(ktorClient)

        val usd = "%.2f".format(1 / exchangeRates.conversion_rates["USD"]!!)
        val eur = "%.2f".format(1 / exchangeRates.conversion_rates["EUR"]!!)
        val cad = "%.2f".format(1 / exchangeRates.conversion_rates["CAD"]!!)
        val aud = "%.2f".format(1 / exchangeRates.conversion_rates["AUD"]!!)
        val gbp = "%.2f".format(1 / exchangeRates.conversion_rates["GBP"]!!)

        val rates = mapOf(
            "EUR" to eur,
            "GBP" to gbp,
            "AUD" to aud,
            "CAD" to cad,
            "USD" to usd,
            "BANK" to usd
        )

        rates.forEach { (rowId, newPrice) ->
            try {
                supabaseClient.from("exchange_rate")
                    .update({
                        set("tti", newPrice)
                    }) {
                        filter {
                            eq("currency_name", rowId)
                        }
                    }
                logger.info("Successfully updated $rowId with new price $newPrice")
            } catch (e: Exception) {
                logger.severe("Error updating row $rowId: ${e.printStackTrace()}")
            }
        }
    }

}