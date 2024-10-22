package com.fixeam.tubesave.network

import android.content.Context
import android.util.Log
import com.fixeam.tubesave.controller.newVersion
import com.fixeam.tubesave.model.PackageUpdate
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import retrofit2.Callback

class ApiUtils {
    private val serveHost = "https://fixeam.com/"
    var service: ApiPorts

    init {
        val client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
        val retrofit: Retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(serveHost)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(ApiPorts::class.java)
    }

    fun checkYoutubeConnectable(call: (Boolean) -> Unit) {
        service.get("https://www.youtube.com/").enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("ABLog", "Response Code: ${response.code()}")
                call(true)
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("ABLog", "Error: $t")
                call(t.message == "connection closed" || t.message?.contains("21") ?: false)
            }
        })
    }

    fun checkUpdate(context: Context, call: (Boolean) -> Unit) {
        val versionCode = context
            .packageManager
            .getPackageInfo(context.packageName, 0)
            .versionCode
        service.getVersions("https://tubesave.fixeam.com/api/app?platform=android&current=$versionCode").enqueue(object: Callback<PackageUpdate.ResponseBody> {
            override fun onResponse(call: Call<PackageUpdate.ResponseBody>, response: Response<PackageUpdate.ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success && body.data.isNotEmpty()) {
                        newVersion = body.data.first()
                        call(true)
                    } else {
                        call(false)
                    }
                } else {
                    call(false)
                }
            }
            override fun onFailure(call: Call<PackageUpdate.ResponseBody>, t: Throwable) {
                Log.e("ABLog", "Error: $t")
                call(false)
            }
        })
    }
}
