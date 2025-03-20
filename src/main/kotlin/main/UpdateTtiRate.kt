package main

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.ktor.client.*
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("Root Logger")

suspend fun updateTtiRates(supabaseClient: SupabaseClient, ktorClient: HttpClient){

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