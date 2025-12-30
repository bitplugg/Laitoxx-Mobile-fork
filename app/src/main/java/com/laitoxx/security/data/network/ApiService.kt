package com.laitoxx.security.data.network

import com.laitoxx.security.data.model.IPInfo
import com.laitoxx.security.data.model.SubdomainResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {

    @GET("{ip}")
    suspend fun getIPInfo(@Path("ip") ip: String): Response<IPInfo>

    @GET
    suspend fun getSubdomains(@Url url: String): Response<List<SubdomainResult>>

    @GET
    suspend fun fetchUrl(@Url url: String): Response<String>
}
