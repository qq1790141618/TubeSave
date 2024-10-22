package com.fixeam.tubesave.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Space
import com.fixeam.tubesave.databinding.PanelButtonBinding

object PanelButton {
    fun get(context: Context, drawable: Drawable? = null, text: String, showStoke: Boolean = true, onClick: (View) -> Unit = {}): View {
        val button = PanelButtonBinding.inflate(LayoutInflater.from(context), null, false)
        button.root.text = text
        drawable?.let {
            button.root.icon = it
        }
        button.root.setOnClickListener {
            onClick(it)
        }
        if (!showStoke) {
            button.root.strokeWidth = 0
        }
        return button.root
    }
}