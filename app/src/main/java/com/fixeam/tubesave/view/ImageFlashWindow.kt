package com.fixeam.tubesave.view

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.fixeam.tubesave.R
import com.fixeam.tubesave.databinding.PanelButtonBinding
import com.fixeam.tubesave.databinding.PanelImageBinding
import com.fixeam.tubesave.utils.LoadingShow
import com.fixeam.tubesave.utils.PopupWindowManager
import com.fixeam.tubesave.utils.ScreenUtils
import com.fixeam.tubesave.utils.ShareHelper

class ImageFlashWindow(
    private val app: AppCompatActivity,
    parentView: View,
    private val imageUrl: String,
    private val displayImage: Boolean = true
) {
    private lateinit var modelPanel: PopupWindow

    init {
        LoadingShow(app, true)
        getView {
            LoadingShow(app, false)
            modelPanel = PopupWindowManager.createPopupWindow(
                app,
                it,
                parentView,
                if (displayImage) PopupWindowManager.PopupWindowDirection.Center else PopupWindowManager.PopupWindowDirection.Bottom
            )
        }
    }

    @SuppressLint("SetTextI18n")
    fun getView(call: (LinearLayout) -> Unit) {
        val screenUtils = ScreenUtils(app)
        val panelBinding = PanelImageBinding.inflate(LayoutInflater.from(app), null, false)

        val screenWidth = screenUtils.getScreenWidth()
        val screenHeight = screenUtils.getScreenHeight()
        val imageViewWidth = screenWidth - screenUtils.dp2px(80)

        if (!displayImage) {
            val params = panelBinding.imageView.layoutParams as ViewGroup.MarginLayoutParams
            params.width = imageViewWidth
            initButtons(panelBinding)
            call(panelBinding.root)
            return
        }

        Glide.with(app.baseContext)
            .load(imageUrl)
            .placeholder(R.drawable.image_holder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    val imageWidth = resource.intrinsicWidth
                    val imageHeight = resource.intrinsicHeight

                    if (imageWidth > 0 && imageHeight > 0) {
                        val scale = imageViewWidth.toFloat() / imageWidth
                        var scaledHeight = (imageHeight * scale).toInt()
                        val maxBl = 0.5
                        if (scaledHeight > screenHeight * maxBl) {
                            scaledHeight = (screenHeight * maxBl).toInt()
                        }

                        val params = panelBinding.imageView.layoutParams as ViewGroup.MarginLayoutParams
                        params.width = imageViewWidth
                        params.height = scaledHeight
                    }
                    panelBinding.imageView.setImageDrawable(resource)

                    initButtons(panelBinding)
                    call(panelBinding.root)
                }
                override fun onLoadCleared(placeholder: Drawable?) { }
                override fun onLoadFailed(errorDrawable: Drawable?) { }
            })
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun initButtons(panelBinding: PanelImageBinding) {
        val screenUtils = ScreenUtils(app)

        val download = PanelButtonBinding.inflate(LayoutInflater.from(app), panelBinding.buttonGroup, false)
        download.root.text = app.getString(R.string.download)
        download.root.icon = app.getDrawable(R.drawable.download)
        download.root.setOnClickListener {
            LoadingShow(app, true, "保存中...")
            ShareHelper.saveImageToGallery(app, imageUrl) {
                close()
                LoadingShow(app, false)
                screenUtils.showToast("保存成功")
            }
        }
        panelBinding.buttonGroup.addView(download.root)

        panelBinding.close.setOnClickListener {
            close()
        }
    }

    fun close() {
        modelPanel.dismiss()
    }
}