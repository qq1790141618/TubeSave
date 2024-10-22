package com.fixeam.tubesave.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.fixeam.tubesave.network.ApiUtils
import com.fixeam.tubesave.utils.LoadingShow
import com.fixeam.tubesave.utils.ScreenUtils
import com.fixeam.tubesave.utils.SimpleDialog
import kotlin.system.exitProcess

@SuppressLint("CustomSplashScreen")
class SplashActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LoadingShow(this, text = "正在检测Youtube连接...")
        ApiUtils().checkYoutubeConnectable {
            LoadingShow(this, false)
            if (!it) {
                SimpleDialog(this, "无法连接到Youtube服务器") {
                    exitProcess(0)
                }
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                ScreenUtils(this).showToast("连接成功")
            }
        }
    }
}