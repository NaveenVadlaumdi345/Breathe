package uk.ac.tees.mad.breathe.network

import retrofit2.http.GET

// ZenQuotes returns an array like: [{"q":"quote text","a":"author","h":"<blockquote>"}]
data class ZenQuoteResponse(
    val q: String?,
    val a: String?,
    val h: String?
)

interface ZenQuotesApi {
    @GET("random")
    suspend fun getRandomQuote(): List<ZenQuoteResponse>
}