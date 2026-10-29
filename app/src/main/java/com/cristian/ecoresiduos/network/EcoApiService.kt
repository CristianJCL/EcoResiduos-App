package com.cristian.ecoresiduos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Rutas REST utilizadas para las operaciones HTTP de prueba. */
interface EcoApiService {
    @GET("posts")
    suspend fun getAll(@Query("_limit") limit: Int = 5): List<RemotePost>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Int): RemotePost

    @POST("posts")
    suspend fun create(@Body post: RemotePost): RemotePost

    @PUT("posts/{id}")
    suspend fun update(@Path("id") id: Int, @Body post: RemotePost): RemotePost

    @DELETE("posts/{id}")
    suspend fun delete(@Path("id") id: Int): Response<Unit>
}
