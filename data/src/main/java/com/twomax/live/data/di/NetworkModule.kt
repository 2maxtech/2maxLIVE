package com.twomax.live.data.di

import com.squareup.moshi.Moshi
import com.twomax.live.data.remote.api.ActivationApi
import com.twomax.live.data.remote.api.OmdbApi
import com.twomax.live.data.remote.api.XtreamApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("epg")
    fun provideEpgOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        // All DTOs use @JsonClass(generateAdapter = true) so KSP generates
        // adapters at compile time — no KotlinJsonAdapterFactory (reflection)
        // needed. Removing it avoids the Class→ParameterizedType crash that
        // occurs when ProGuard strips Kotlin metadata in release builds.
        return Moshi.Builder().build()
    }

    @Provides
    @Singleton
    fun provideXtreamApi(okHttpClient: OkHttpClient, moshi: Moshi): XtreamApi {
        return Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(XtreamApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOmdbApi(okHttpClient: OkHttpClient, moshi: Moshi): OmdbApi {
        return Retrofit.Builder()
            .baseUrl("https://www.omdbapi.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OmdbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideActivationApi(okHttpClient: OkHttpClient, moshi: Moshi): ActivationApi {
        return Retrofit.Builder()
            .baseUrl("https://player.2max.tech/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ActivationApi::class.java)
    }
}
