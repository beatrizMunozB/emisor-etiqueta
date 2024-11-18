package com.example.myapplication

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class ItemResponse(
    var item: String,
    var descripcion: String,
    var CodigoChile1: String
)

interface ApiService
{
    @GET("api/generar-etiquetaC/{item}")
    suspend fun obtenerHerramienta(@Path("item") item: String) : List<ItemResponse>
}