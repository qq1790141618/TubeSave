package com.fixeam.tubesave.utils

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi

object ShareHelper {
    // 分享文字
    fun shareTextContent(text: String, title: String = "来自iCoser的分享", context: Context) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(shareIntent, title))
    }

    // 仅下载图片
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun downloadImage(imageUrl: String, context: Context) {
        val downloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // 创建下载请求
        val request = DownloadManager.Request(Uri.parse(imageUrl))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                imageUrl.substringAfterLast("/")
            )

        // 将下载请求加入下载队列
        val downloadId = downloadManager.enqueue(request)

        // 注册下载完成的广播接收器
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                    val downloadIdCompleted =
                        intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
                    if (downloadIdCompleted == downloadId) {
                        downloadManager.getUriForDownloadedFile(downloadIdCompleted)
                        Toast.makeText(
                            context,
                            "图片已经保存到相册${imageUrl.substringAfterLast("/")}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // 将 setResultCode 和其它相关方法放在 onReceive 方法内部
                resultCode = Activity.RESULT_OK
                resultData = null
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
    }

    // 分享图片调用
    fun shareImageContent(
        imageUrl: String,
        title: String = "来自iCoser的分享",
        context: Context
    ) {
        LoadingShow(context, true)
        saveImageToGallery(context, imageUrl) {
            LoadingShow(context, false)
            Handler(Looper.getMainLooper()).postDelayed({
                shareImageUri(it, title, context)
            }, 300)
        }
    }

    // 分享图片
    private fun shareImageUri(imageUri: Uri?, title: String = "来自iCoser的分享", context: Context) {
        imageUri?.let {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            context.startActivity(Intent.createChooser(shareIntent, title))
        }
    }

    // 保存图片到相册
    fun saveImageToGallery(context: Context, imageUrl: String, call: (Uri) -> Unit) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // 创建下载请求
        val request = DownloadManager.Request(Uri.parse(imageUrl))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                imageUrl.substringAfterLast("/")
            )

        // 将下载请求加入下载队列
        val downloadId = downloadManager.enqueue(request)

        // 注册下载完成的广播接收器
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                    val downloadIdCompleted =
                        intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
                    if (downloadIdCompleted == downloadId) {
                        val downloadedUri = downloadManager.getUriForDownloadedFile(downloadIdCompleted)
                        context?.let { call(downloadedUri) }
                    }
                }
                // 将 setResultCode 和其它相关方法放在 onReceive 方法内部
                resultCode = Activity.RESULT_OK
                resultData = null
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        }
    }

    // 复制到剪贴板
    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
    }
}