package com.fixeam.tubesave.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fixeam.tubesave.R
import com.fixeam.tubesave.controller.downloadListUtils
import com.fixeam.tubesave.databinding.DownloadViewLayoutBinding
import com.fixeam.tubesave.model.DownloadItem
import com.fixeam.tubesave.model.TuDown
import com.fixeam.tubesave.utils.ActivityDisplay
import com.fixeam.tubesave.utils.Calculate
import com.fixeam.tubesave.utils.PanelButton
import com.fixeam.tubesave.utils.PopupWindowManager

class DownloadView(
    parentView: View,
    private val app: AppCompatActivity,
    private val item: TuDown.VideoData
) {
    private var activityDisplay: ActivityDisplay = ActivityDisplay(app)
    private lateinit var panel: PopupWindow

    init {
        panel = PopupWindowManager.createPopupWindow(
            app,
            getView(),
            parentView,
            PopupWindowManager.PopupWindowDirection.Bottom
        )
    }

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    fun getView(): LinearLayout {
        val panelBinding = DownloadViewLayoutBinding.inflate(LayoutInflater.from(app), null, false)

        val thumbnail = item.thumbnails.last()
        panelBinding.textInfo.text = item.title
        Glide.with(app.baseContext)
            .load(thumbnail.url)
            .placeholder(R.drawable.image_holder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(panelBinding.posterInfo)

        // 获取有效转码
        val usefulFormats = item.formats.filter { it.url.startsWith("https://rr") && it.ext == "webm" }
        // 找到音频格式
        val audioFormat: TuDown.VideoFormat? = usefulFormats.lastOrNull {
            it.resolution.contains("audio")
        }
        // 反向遍历格式并创建按钮
        usefulFormats.reversed().filter {
            !it.resolution.contains("audio")
        }.forEach { format ->
            val totalSize = (format.filesize + (audioFormat?.filesize ?: 0)).toLong()
            val text = "${format.resolution} ${format.fps}fps (${Calculate.bytesToReadableSize(totalSize)})"

            val button = PanelButton.get(app, text = text) {
                downloadListUtils?.addOne(DownloadItem(
                    item.id,
                    "${item.title}_${format.resolution}",
                    thumbnail,
                    format.formatId,
                    format.url,
                    0,
                    audioFormat?.url ?: "",
                    format.filesize.toLong(),
                    (audioFormat?.filesize ?: 0).toLong()
                ))
                close()
            }

            panelBinding.buttonGroup.addView(button)
        }

        panelBinding.close.setOnClickListener {
            close()
        }

        return panelBinding.root
    }

    fun close() {
        panel.dismiss()
    }
}