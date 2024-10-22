package com.fixeam.tubesave.model

import com.fixeam.tubesave.network.DownloadManager
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement

class DownloadItemDeserializer : JsonDeserializer<DownloadItem> {
    override fun deserialize(json: JsonElement, typeOfT: java.lang.reflect.Type, context: com.google.gson.JsonDeserializationContext): DownloadItem {
        val jsonObject = json.asJsonObject

        return DownloadItem(
            id = jsonObject.get("id").asString,
            title = jsonObject.get("title").asString,
            thumbnail = context.deserialize<TuDown.Thumbnail>(jsonObject.get("thumbnail"), TuDown.Thumbnail::class.java),
            formatId = jsonObject.get("formatId").asString,
            url = jsonObject.get("url").asString,
            speed = jsonObject.get("speed")?.asLong ?: 0,
            audioUrl = jsonObject.get("audioUrl").asString,
            size = jsonObject.get("size").asLong,
            audioSize = jsonObject.get("audioSize").asLong,
            status = jsonObject.get("status")?.asString ?: "wait",
            msg = jsonObject.get("msg")?.asString ?: "待下载",
            percentage = jsonObject.get("percentage")?.asLong ?: 0,
            audioPercentage = jsonObject.get("audioPercentage")?.asLong ?: 0,
            error = context.deserialize<MutableList<String>>(jsonObject.get("error"), MutableList::class.java) ?: mutableListOf(),
            downloadManager = context.deserialize<DownloadManager>(jsonObject.get("downloadManager"), DownloadManager::class.java),
            savePath = jsonObject.get("savePath")?.asString,
            update = null // 将 update 设为 null
        )
    }
}