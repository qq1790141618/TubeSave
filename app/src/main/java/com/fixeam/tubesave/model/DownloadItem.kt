package com.fixeam.tubesave.model

import com.fixeam.tubesave.network.DownloadManager

data class DownloadItem(
    val id: String,
    val title: String,
    val thumbnail: TuDown.Thumbnail,
    val formatId: String,
    val url: String,
    var speed: Long = 0,
    val audioUrl: String,
    val size: Long,
    val audioSize: Long,
    var status: String = "wait",
    var msg: String = "待下载",
    var percentage: Long = 0,
    var audioPercentage: Long = 0,
    var error: MutableList<String> = mutableListOf(),
    var downloadManager: DownloadManager? = null,
    var savePath: String? = null,
    var update: (() -> Unit)? = null
)