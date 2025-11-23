package uk.ac.tees.mad.breathe

import android.content.Context
import androidx.room.Room
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.http.ContentType.Application.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uk.ac.tees.mad.breathe.data.local.AppDatabase
import uk.ac.tees.mad.breathe.data.local.SessionDao
import uk.ac.tees.mad.breathe.network.ZenQuotesApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltModule {

    val base_URL = "https://api.assemblyai.com/v2/"

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "breathe_db").build()

    @Provides
    fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .callTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://zenquotes.io/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
    }

    @Provides
    @Singleton
    fun provideZenQuotesApi(retrofit: Retrofit): ZenQuotesApi =
        retrofit.create(ZenQuotesApi::class.java)

    @Provides
    @Singleton
    fun providesAuth() : FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun providesFirestore() : FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

}