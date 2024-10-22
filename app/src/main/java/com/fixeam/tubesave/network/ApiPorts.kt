package com.fixeam.tubesave.network

import com.fixeam.tubesave.model.PackageUpdate
import com.fixeam.tubesave.model.TuDown
import com.fixeam.tubesave.model.YoutubeSearch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface ApiPorts {
    @GET
    fun get(@Url url: String): Call<ResponseBody>
    @GET
    fun search(@Url url: String): Call<YoutubeSearch.SearchListResponse>
    @GET
    fun getViews(@Url url: String): Call<YoutubeSearch.VideoListResponse>
    @POST
    fun getDowns(@Url url: String, @Body body: TuDown.TuDownRequestBody): Call<TuDown.TuDownResponse>
    @GET
    fun getVersions(@Url url: String): Call<PackageUpdate.ResponseBody>
}