package com.fixeam.tubesave.model

import com.google.gson.annotations.SerializedName

object TuDown {
    data class TuDownRequestBody(
        var url: String
    )

    data class TuDownResponse(
        val code: Int,
        val message: String,
        val data: VideoData
    )

    data class VideoData(
        val id: String,
        val title: String,
        val formats: List<VideoFormat>,
        val thumbnails: List<Thumbnail>
    )

    data class Thumbnail(
        val url: String,
        val height: Int,
        val width: Int,
        val preference: Int,
        val id: String,
        val resolution: String
    )

    data class VideoFormat(
        val asr: Int,
        val filesize: Int,
        @SerializedName("format_id")
        val formatId: String,
        val formatNote: String,
        val sourcePreference: Int,
        val fps: Double?,
        val audioChannels: Int,
        val height: Int?,
        val quality: Double,
        val hasDrm: Boolean,
        val tbr: Double,
        val filesizeApprox: Int,
        val url: String,
        val manifestUrl: String,
        var audioUrl: String?,
        val width: Int?,
        val language: String,
        val languagePreference: Int,
        val preference: Int?,
        val ext: String,
        val vcodec: String,
        val acodec: String,
        val dynamicRange: String?,
        val container: String,
        val downloaderOptions: DownloaderOptions,
        val protocol: String,
        val resolution: String,
        val aspectRatio: String?,
        val httpHeaders: HttpHeaders,
        val audioExt: String,
        val videoExt: String,
        val vbr: Double,
        val abr: Double,
        val format: String
    )

    data class DownloaderOptions(
        val httpChunkSize: Int
    )

    data class HttpHeaders(
        val userAgent: String,
        val accept: String,
        val acceptLanguage: String,
        val secFetchMode: String
    )
}