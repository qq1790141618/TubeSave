package com.fixeam.tubesave.utils

import android.content.res.Configuration
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ActivityDisplay(private val app: AppCompatActivity) {
    // 窗口大小设置
    fun setDisplayArea(view: View, safeArea: Boolean = false) {
        app.enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val bottom = if (safeArea) systemBars.bottom else 0
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom)
            insets
        }
    }
    // 检测是否为深色主题
    fun isDarken(): Boolean{
        val currentTheme = app.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentTheme == Configuration.UI_MODE_NIGHT_YES
    }
    // 全局窗口亮度调整
    fun changeBackgroundDim(isDim: Boolean) {
        val window = app.window
        val layoutParams = window.attributes
        layoutParams.alpha = if (isDim) 0.5f else 1.0f
        window.attributes = layoutParams
    }
    // 设置颜色模式
    fun setColorMode(index: Int) {
        when(index){
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> throw IllegalStateException("下标不符合预期")
        }
    }
}