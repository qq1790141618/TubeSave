package com.fixeam.tubesave.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import com.fixeam.tubesave.R
import com.fixeam.tubesave.controller.openYouTubeVideo
import com.fixeam.tubesave.databinding.PanelOptionBinding
import com.fixeam.tubesave.utils.PanelButton
import com.fixeam.tubesave.utils.PopupWindowManager

class DownloadOption(
    private val app: AppCompatActivity,
    parentView: View,
    private val view: () -> Unit,
    private val onDelete: () -> Unit,
    private val onDeleteFile: () -> Unit
) {
    private var panel: PopupWindow

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
        val panel = PanelOptionBinding.inflate(LayoutInflater.from(app), null, false)

        val viewOnYoutube = PanelButton.get(
            app,
            app.getDrawable(R.drawable.youtube),
            "在Youtube中查看"
        ) {
            close()
            view()
        }
        panel.buttonGroup.addView(viewOnYoutube)

        val delete = PanelButton.get(
            app,
            app.getDrawable(R.drawable.baseline_delete_24),
            "删除任务"
        ) {
            close()
            onDelete()
        }
        panel.buttonGroup.addView(delete)

        val deleteFile = PanelButton.get(
            app,
            app.getDrawable(R.drawable.baseline_delete_forever_24),
            "彻底删除（连文件）"
        ) {
            close()
            onDeleteFile()
        }
        panel.buttonGroup.addView(deleteFile)

        panel.close.setOnClickListener {
            close()
        }

        return panel.root
    }

    fun close() {
        panel.dismiss()
    }
}