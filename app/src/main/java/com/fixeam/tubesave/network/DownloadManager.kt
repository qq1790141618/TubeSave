package com.fixeam.tubesave.network

import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class DownloadManager {
    private val client = OkHttpClient()
    private var isPaused = false
    private var isCanceled = false
    private var downloadedSize = 0L
    private var lastDownloadedSize = 0L
    private var lastTime = System.currentTimeMillis()

    interface DownloadProgressListener {
        fun onProgress(progress: Long, speed: Long)
        fun onComplete(file: File)
        fun onError(e: Exception)
    }

    suspend fun downloadFile(
        url: String,
        destinationFile: File,
        totalSize: Long,
        listener: DownloadProgressListener
    ) {
        withContext(Dispatchers.IO) {
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null

            try {
                // 检查目标文件是否存在
                if (destinationFile.exists()) {
                    // 如果文件已经下载完成，直接调用 onComplete
                    if (destinationFile.length() == totalSize) {
                        listener.onComplete(destinationFile)
                        return@withContext
                    }
                    downloadedSize = destinationFile.length()
                } else {
                    destinationFile.parentFile?.mkdirs()
                    downloadedSize = 0
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Range", "bytes=$downloadedSize-")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful && response.code != 206) {
                    throw IOException("Unexpected code $response")
                }

                inputStream = response.body?.byteStream() ?: throw IOException("Response body is null")
                outputStream = FileOutputStream(destinationFile, true)

                val buffer = ByteArray(8 * 1024)
                var read: Int

                while (true) {
                    if (isCanceled) {
                        throw IOException("Download canceled")
                    }

                    // 检查暂停状态
                    if (isPaused) {
                        // 等待直到恢复
                        delay(1000) // 可以根据需要调整此延迟
                        continue // 继续下一次循环
                    }

                    read = inputStream.read(buffer)
                    if (read == -1) break // 下载完成

                    outputStream.write(buffer, 0, read)
                    downloadedSize += read

                    // 计算下载速度
                    val currentTime = System.currentTimeMillis()
                    val elapsedTime = currentTime - lastTime
                    if (elapsedTime > 1000) { // 每秒更新一次速度
                        val speed = (downloadedSize - lastDownloadedSize) * 1000 / elapsedTime
                        listener.onProgress(downloadedSize, speed)
                        lastDownloadedSize = downloadedSize
                        lastTime = currentTime
                    }
                }

                outputStream.flush()
                listener.onComplete(destinationFile)
            } catch (e: Exception) {
                listener.onError(e)
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        }
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }

    fun cancel() {
        isCanceled = true
    }
}
