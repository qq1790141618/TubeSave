package com.fixeam.tubesave.model

object YoutubeSearch {
    // 用于存储搜索结果的主要响应类
    data class SearchListResponse(
        val kind: String,
        val etag: String,
        val nextPageToken: String?,
        val prevPageToken: String?,
        val regionCode: String,
        val pageInfo: PageInfo,
        val items: List<SearchResult>
    )

    // 用于存储分页信息
    data class PageInfo(
        val totalResults: Int,
        val resultsPerPage: Int
    )

    // 用于存储单个搜索结果
    data class SearchResult(
        val kind: String,
        val etag: String,
        val id: VideoId,
        val snippet: Snippet,
        var statistics: Statistics? = null
    )

    // 用于存储视频 ID 信息
    data class VideoId(
        val kind: String,
        val videoId: String
    )

    // 用于存储视频的详细信息
    data class Snippet(
        val publishedAt: String,
        val channelId: String,
        val title: String,
        val description: String,
        val thumbnails: Thumbnails,
        val channelTitle: String,
        val liveBroadcastContent: String,
        val publishTime: String
    )

    // 用于存储缩略图信息
    data class Thumbnails(
        val default: ThumbnailDetail,
        val medium: ThumbnailDetail,
        val high: ThumbnailDetail
    )

    // 用于存储缩略图的详细信息
    data class ThumbnailDetail(
        val url: String,
        val width: Int,
        val height: Int
    )

    data class VideoListResponse(
        val kind: String,
        val etag: String,
        val items: List<Video>,
        val pageInfo: PageInfo
    )

    data class Video(
        val kind: String,
        val etag: String,
        val id: String,
        val statistics: Statistics
    )

    data class Statistics(
        val viewCount: String,
        val likeCount: String,
        val favoriteCount: String,
        val commentCount: String
    )
}