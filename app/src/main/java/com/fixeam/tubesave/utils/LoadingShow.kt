package com.fixeam.tubesave.utils

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.fixeam.tubesave.view.LoadingView

class LoadingShow(context: Context, status: Boolean = true, text: String? = null) {
    init {
        when(status){
            true -> {
                val intent = Intent(context, LoadingView::class.java)
                text?.let {
                    intent.putExtra("text", it)
                }
                context.startActivity(intent)
            }
            false -> {
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent("close_loading_activity")
                    context.sendBroadcast(intent)
                }, 100)
            }
        }
    }
}