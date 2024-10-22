package com.fixeam.tubesave.view

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.fixeam.tubesave.controller.newVersion
import com.fixeam.tubesave.databinding.UpdateTipDialogBinding
import com.fixeam.tubesave.utils.Md5Utils
import com.fixeam.tubesave.utils.ScreenUtils

class UpdateView: AppCompatActivity() {
    private lateinit var binding: UpdateTipDialogBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = UpdateTipDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(newVersion == null){
            finish()
        }

        binding.close.setOnClickListener { finish() }
        binding.update.setOnClickListener {
            finish()
            startDownload()
        }
    }

    private fun startDownload() {
        ScreenUtils(this).showToast("将在后台下载更新")
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(newVersion!!.resource)
        val request = DownloadManager.Request(uri)

        // 设置下载的标题和描述
        request.setTitle("正在下载更新...")
//        request.setDescription("Downloading new version of the app")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        // 设置下载存储的位置
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${ Md5Utils.generateMD5String(
            newVersion!!.resource) }.apk")

        // 启动下载
        val downloadId = downloadManager.enqueue(request)

        // 注册 BroadcastReceiver 来监听下载完成
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    // 下载完成，启动 UpdateInstall Activity
                    val updateIntent = Intent(context, UpdateInstall::class.java)
                    context.startActivity(updateIntent)
                    unregisterReceiver(this) // 取消注册接收器
                }
            }
        }

        // 检查当前设备的 API 级别
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 级别大于或等于 33，使用 RECEIVER_EXPORTED
            registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED)
        } else {
            // API 级别小于 33，不使用 RECEIVER_EXPORTED
            registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }
}