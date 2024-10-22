package com.fixeam.tubesave.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings

class NetworkUtils(private val context: Context) {

    private fun networkCapabilities(): NetworkCapabilities? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    }

    /**
     * 检测是否启用了 VPN
     * @return 如果启用了 VPN，返回 true；否则返回 false。
     */
    fun isVpnActive(): Boolean {
        val networkCapabilities = networkCapabilities()
        return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
    }

    /**
     * 检测是否启用了代理
     * @return 如果启用了代理，返回 true；否则返回 false。
     */
    fun isProxyEnabled(): Boolean {
        return try {
            val proxyHost = Settings.Global.getString(context.contentResolver, "http_proxy")
            proxyHost != null && proxyHost.isNotEmpty()
        } catch (e: Exception) {
            false // 处理可能的异常，确保不会崩溃
        }
    }

    /**
     * 检测是否启用了 VPN 或代理
     * @return 如果启用了 VPN 或代理，返回 true；否则返回 false。
     */
    fun isVpnOrProxyEnabled(): Boolean {
        return isVpnActive() || isProxyEnabled()
    }

    /**
     * 检查当前的网络状态
     * @return 返回一个网络类型标识：
     *         0 - 无网络
     *         1 - Wi-Fi
     *         2 - 移动数据
     */
    fun checkForNetwork(): Int {
        val networkCapabilities = networkCapabilities()
        return if (networkCapabilities == null || !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            0 // 无网络
        } else {
            when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> 1 // Wi-Fi
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> 2 // 移动数据
                else -> 0 // 其他类型
            }
        }
    }
}