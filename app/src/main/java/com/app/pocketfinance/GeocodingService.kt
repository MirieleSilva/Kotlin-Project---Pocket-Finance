package com.app.pocketfinance

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

data class AddressResponse(
    val display_name: String
)

interface GeocodingApi {
    @Headers("User-Agent: PocketFinanceApp")
    @GET("reverse?format=json")
    suspend fun getAddress(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Response<AddressResponse>
}