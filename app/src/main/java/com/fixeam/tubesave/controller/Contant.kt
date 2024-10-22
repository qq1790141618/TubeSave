package com.fixeam.tubesave.controller

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.fixeam.tubesave.model.DownloadItem
import com.fixeam.tubesave.utils.DownloadListUtils
import com.fixeam.tubesave.view.DownloadFragment
import com.fixeam.tubesave.view.SearchFragment

@SuppressLint("StaticFieldLeak")
var downloadListUtils: DownloadListUtils? = null
var searchFragment: SearchFragment? = null
var downloadFragment: DownloadFragment? = null
val downloadList = mutableListOf<DownloadItem>()
var isDownloading = 0

fun openYouTubeVideo(context: Context, videoId: String) {
    // 构建 YouTube 视频的 URL
    val videoUrl = "https://www.youtube.com/watch?v=$videoId"
    // 创建一个意图来打开 YouTube
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
    // 启动意图
    context.startActivity(intent)
}