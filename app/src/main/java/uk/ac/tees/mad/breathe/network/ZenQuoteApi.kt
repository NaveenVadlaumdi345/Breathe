package uk.ac.tees.mad.breathe.network

import retrofit2.http.GET

data class ZenQuoteResponse(
    val q: String?,
    val a: String?,
    val h: String?
)

interface ZenQuotesApi {
    @GET("api/random")
    suspend fun getRandomQuote(): List<ZenQuoteResponse>
}