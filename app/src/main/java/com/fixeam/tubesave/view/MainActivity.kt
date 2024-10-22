package com.fixeam.tubesave.view

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.fixeam.tubesave.R
import com.fixeam.tubesave.controller.downloadFragment
import com.fixeam.tubesave.controller.downloadList
import com.fixeam.tubesave.controller.downloadListUtils
import com.fixeam.tubesave.controller.isDownloading
import com.fixeam.tubesave.controller.searchFragment
import com.fixeam.tubesave.databinding.ActivityMainBinding
import com.fixeam.tubesave.model.DownloadItem
import com.fixeam.tubesave.network.DownloadManager
import com.fixeam.tubesave.utils.ActivityDisplay
import com.fixeam.tubesave.utils.DownloadListUtils
import com.fixeam.tubesave.utils.Md5Utils
import com.fixeam.tubesave.utils.ScreenUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityDisplay(this).setDisplayArea(binding.root)
        downloadListUtils = DownloadListUtils(this)
        searchFragment = SearchFragment()
        downloadFragment = DownloadFragment()
        setNavigation()
        read()
        refreshList()
    }

    private fun setNavigation() {
        binding.bottomNavigation.itemActiveIndicatorColor = ColorStateList.valueOf(getColor(R.color.brand_primary_light))
        binding.bottomNavigation.setOnNavigationItemSelectedListener { it ->
            val fragmentToShow = when (it.itemId) {
                R.id.navigation_search -> searchFragment
                R.id.navigation_downloads -> downloadFragment
                else -> return@setOnNavigationItemSelectedListener false
            }

            // 如果当前显示的片段与所选择的片段相同，则不进行替换
            if (supportFragmentManager.findFragmentById(R.id.fragment_container) != fragmentToShow) {
                fragmentToShow?.let { it1 ->
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, it1)
                        .commit()
                }
            }
            true
        }
        binding.bottomNavigation.selectedItemId = R.id.navigation_search
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun read() {
        isDownloading = 0
        downloadList.clear()
        downloadListUtils?.let {
            downloadList.addAll(it.read())
            downloadList.map { item ->
                if (item.status == "downloading") {
                    item.status = "interrupt"
                }
                if (item.status == "interrupt") {
                    item.msg = "等待中断恢复"
                }
                if (item.status == "done" && !item.savePath.isNullOrBlank()) {
                    val file = File(item.savePath!!)
                    if (!file.exists()) {
                        item.status = "deleted"
                        item.msg = "已删除"
                    }
                }
                saveList()
                Log.d("ABLog", "$item")
            }
        }
    }

    private fun saveList() {
        downloadListUtils?.save(downloadList)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshList() {
        var runnable: Runnable? = null
        val handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            downloadList.map { item ->
                if ((item.status == "interrupt" || item.status == "wait") && isDownloading < 3) {
                    item.status = "downloading"
                    isDownloading++
                    runDownload(item) {
                        item.status = "done"
                        isDownloading--
                    }
                }
            }
            saveList()
            runnable?.let { handler.postDelayed(it, 1000) }
        }
        runnable.let { handler.postDelayed(it, 1000) }
    }

    private fun runDownload(item: DownloadItem, call: () -> Unit = {}) {
        item.msg = "下载中"
        item.update?.invoke()
        var count = 0
        fun addCount() {
            count++
            if (count >= 2) {
                runVideoAudioBind(item) {
                    call()
                }
            }
        }
        runDownloadAudio(item) {
            addCount()
        }
        runDownloadVideo(item) {
            addCount()
        }
    }

    private var retryTime = 12
    private var failTime = 0

    private fun runDownloadAudio(item: DownloadItem, call: () -> Unit = {}) {
        val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outputFile = File(downloadFolder, Md5Utils.generateMD5String(item.audioUrl) + ".audio")
        if (item.downloadManager == null) item.downloadManager = DownloadManager()

        failTime = 0
        lifecycleScope.launch {
            item.downloadManager!!.downloadFile(item.audioUrl, outputFile, item.audioSize, object : DownloadManager.DownloadProgressListener {
                override fun onProgress(progress: Long, speed: Long) {
                    Log.d("ABLog", "audio progress: $progress, speed: $speed")
                    if (progress > item.audioPercentage) item.audioPercentage = progress
                    item.audioPercentage = progress
                    item.speed = speed
                    item.update?.invoke()
                }
                override fun onComplete(file: File) {
                    item.audioPercentage = item.audioSize
                    call()
                }
                override fun onError(e: Exception) {
                    failTime++
                    if (failTime < retryTime) {
                        runDownloadAudio(item, call)
                    } else {
                        Log.e("ABLog", ".audio $e")
                        item.msg = "下载失败"
                        item.status = "fail"
                        isDownloading--
                        item.update?.invoke()
                    }
                }
            })
        }
    }

    private fun runDownloadVideo(item: DownloadItem, call: () -> Unit = {}) {
        val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outputFile = File(downloadFolder, Md5Utils.generateMD5String(item.url) + ".video")
        if (item.downloadManager == null) item.downloadManager = DownloadManager()

        failTime = 0
        lifecycleScope.launch {
            item.downloadManager!!.downloadFile(
                item.url,
                outputFile,
                item.size,
                object : DownloadManager.DownloadProgressListener {
                    override fun onProgress(progress: Long, speed: Long) {
                        Log.d("ABLog", "video progress: $progress, speed: $speed")
                        if (progress > item.percentage) item.percentage = progress
                        item.percentage = progress
                        item.speed = speed
                        item.update?.invoke()
                    }

                    override fun onComplete(file: File) {
                        item.percentage = item.size
                        call()
                    }

                    override fun onError(e: Exception) {
                        failTime++
                        if (failTime < retryTime) {
                            runDownloadVideo(item, call)
                        } else {
                            Log.e("ABLog", ".video $e")
                            item.msg = "下载失败"
                            item.status = "fail"
                            isDownloading--
                            item.update?.invoke()
                        }
                    }
                })

        }
    }

    private fun runVideoAudioBind(item: DownloadItem, call: () -> Unit = {}) {
        item.msg = "合并视频中..."
        val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val video = File(downloadFolder, Md5Utils.generateMD5String(item.url) + ".video")
        val audio = File(downloadFolder, Md5Utils.generateMD5String(item.audioUrl) + ".audio")
        val videoPath = video.absolutePath
        val audioPath = audio.absolutePath
        val sanitizedTitle = item.title.replace(Regex("[<>:\"/\\|?*]"), "_")
        val outputPath = File(downloadFolder, "$sanitizedTitle.webm").absolutePath

        val command = "-y -i \"$videoPath\" -i \"$audioPath\" -c:v copy -c:a libvorbis -shortest \"$outputPath\""

        // 使用协程在 IO 线程中执行 FFmpeg 命令
        lifecycleScope.launch(Dispatchers.IO) {
            val rc = FFmpeg.execute(command)

            launch(Dispatchers.Main) {
                if (rc == Config.RETURN_CODE_SUCCESS) {
                    item.msg = "下载完成"
                    // 通知相册更新
                    MediaScannerConnection.scanFile(
                        this@MainActivity,
                        arrayOf(outputPath),
                        null,
                        null
                    )
                    video.delete()
                    audio.delete()
                    item.savePath = outputPath
                    call()
                } else {
                    Log.e("ABLog", ".video $rc")
                    item.msg = "合并失败"
                    item.status = "fail"
                    isDownloading--
                }
                item.update?.invoke()
            }
        }
    }

    private var pressCount = 0
    @Deprecated("Deprecated in Java",
        ReplaceWith("super.onBackPressed()", "androidx.appcompat.app.AppCompatActivity")
    )
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (pressCount == 1) {
            finishAndRemoveTask()
            ActivityCompat.finishAffinity(this)
            exitProcess(0)
        } else {
            pressCount ++
            ScreenUtils(this).showToast(getString(R.string.press_back_to_leave))
            Handler(Looper.getMainLooper()).postDelayed({
                pressCount = 0
            }, 2000)
        }
    }
}