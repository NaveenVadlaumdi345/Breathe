package uk.ac.tees.mad.breathe.network
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class AudioAnalysisRequest(val audio_base64: String)
data class AudioAnalysisResponse(val noise_level: Float?)

interface AssemblyApi {
    @Headers("authorization: YOUR_ASSEMBLYAI_API_KEY")
    @POST("analyze")
    suspend fun analyzeNoise(@Body body: AudioAnalysisRequest): AudioAnalysisResponse
}