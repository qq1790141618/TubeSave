package com.fixeam.tubesave.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast

class ScreenUtils(private val context: Context) {
    // 获取屏幕宽度
    fun getScreenWidth(): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }
    // 获取屏幕高度
    fun getScreenHeight(): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }
    // dp转换成px
    fun dp2px(dpValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
    // 提示内容
    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}