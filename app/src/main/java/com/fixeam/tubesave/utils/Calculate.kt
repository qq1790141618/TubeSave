package com.fixeam.tubesave.utils

import android.annotation.SuppressLint
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow

object Calculate {
    @SuppressLint("DefaultLocale")
    fun bytesToReadableSize(size: Long): String {
        if (size <= 0) {
            return "0 B"
        }
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = min((log10(size.toDouble()) / log10(1024.0)).toInt(), units.size - 1)
        return String.format("%.1f%s", size / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
    }
}