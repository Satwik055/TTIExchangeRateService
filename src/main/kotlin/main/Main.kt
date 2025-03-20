package main

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import utlis.Constants
import java.time.Duration
import java.util.logging.Logger

fun main() = runBlocking {
    val ktorClient = HttpClient()
    val supabaseClient = createSupabaseClient(Constants.SECRETS.SUPABASE.URL, Constants.SECRETS.SUPABASE.KEY) { install(Postgrest) }
    val cycleTimeSeconds:Long = 10

    while (true) {
        updateTtiRates(
            supabaseClient = supabaseClient,
            ktorClient = ktorClient
        )
        Thread.sleep(Duration.ofSeconds(cycleTimeSeconds).toMillis())
    }
}