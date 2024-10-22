package com.fixeam.tubesave.model

import com.google.gson.annotations.SerializedName

object PackageUpdate {
    data class ResponseBody(
        val success: Boolean,
        val data: List<Info>,
        val message: String?
    )
    data class Info(
        val id: Int,
        val platform: String,
        val version: String,
        @SerializedName("version_id")
        val versionId: Int,
        val type: String,
        val publish: String,
        val resource: String,
        val size: Int,
        val info: String
    )
}