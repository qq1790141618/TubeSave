package com.fixeam.tubesave.utils

import android.content.Context
import com.fixeam.tubesave.controller.downloadList
import com.fixeam.tubesave.model.DownloadItem
import com.fixeam.tubesave.model.DownloadItemDeserializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

class DownloadListUtils(private val context: Context) {
    private val fileName = "download_list.json"

    fun addOne(item: DownloadItem) {
        val lastList = read()
        val lastTask = lastList.find { it.id == item.id && it.formatId == item.formatId }
        if (lastTask != null && lastTask.status != "fail" && lastTask.status != "deleted") {
            ScreenUtils(context).showToast("相同的下载任务已经存在")
            return
        }
        val list = mutableListOf<DownloadItem>()
        list.add(item)
        list.addAll(lastList)
        downloadList.clear()
        downloadList.addAll(list)
        save(list)
        ScreenUtils(context).showToast("下载任务创建成功")
    }

    fun save(downloadList: List<DownloadItem>) {
        val list = downloadList.map { it.copy(downloadManager = null, speed = 0, update = null) }
        val json = Gson().toJson(list)
        val file = File(context.filesDir, fileName)
        file.writeText(json)
    }

    fun read(): List<DownloadItem> {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            val jsonFromFile = file.readText()
            val gson: Gson = GsonBuilder()
                .registerTypeAdapter(DownloadItem::class.java, DownloadItemDeserializer())
                .create()
            val objectList = gson.fromJson(jsonFromFile, Array<DownloadItem>::class.java).toList()
            objectList
        } else {
            emptyList()
        }
    }
}