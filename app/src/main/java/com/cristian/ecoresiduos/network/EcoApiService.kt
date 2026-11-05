package com.cristian.ecoresiduos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

/** Métodos REST que pide Iván: GET, POST, PATCH y DELETE. */
interface EcoApiService {
    @POST("auth/v1/signup")
    suspend fun signUp(@Header("apikey") key: String, @Body input: LoginInput): AuthSession

    @POST("auth/v1/token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Header("apikey") key: String,
        @Body input: LoginInput
    ): AuthSession

    @POST("auth/v1/token")
    suspend fun refresh(
        @Query("grant_type") grantType: String = "refresh_token",
        @Header("apikey") key: String,
        @Body input: RefreshInput
    ): AuthSession

    @GET("rest/v1/residuos")
    suspend fun list(
        @Header("apikey") key: String,
        @Header("Authorization") token: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "date.desc"
    ): List<RemoteWaste>

    @GET("rest/v1/residuos")
    suspend fun detail(
        @Header("apikey") key: String,
        @Header("Authorization") token: String,
        @Query("id") filter: String,
        @Query("select") select: String = "*"
    ): List<RemoteWaste>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/residuos")
    suspend fun create(
        @Header("apikey") key: String,
        @Header("Authorization") token: String,
        @Body record: RemoteWaste
    ): List<RemoteWaste>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/residuos")
    suspend fun update(
        @Header("apikey") key: String,
        @Header("Authorization") token: String,
        @Query("id") filter: String,
        @Body record: RemoteWaste
    ): List<RemoteWaste>

    @DELETE("rest/v1/residuos")
    suspend fun delete(
        @Header("apikey") key: String,
        @Header("Authorization") token: String,
        @Query("id") filter: String
    ): Response<Unit>
}
