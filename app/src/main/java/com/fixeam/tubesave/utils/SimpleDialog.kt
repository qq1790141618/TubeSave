package com.fixeam.tubesave.utils

import android.annotation.SuppressLint
import android.content.Context
import com.fixeam.tubesave.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@SuppressLint("UseCompatLoadingForDrawables")
class SimpleDialog (
    context: Context,
    message: String,
    showCancel: Boolean = false,
    title: String? = null,
    onClick: (Boolean) -> Unit = {}
) {
    init {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setBackground(context.getDrawable(R.drawable.linear_card_background))

        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(context.getString(R.string.confirm)) { _, _ -> onClick(true) }
        if(showCancel){
            builder.setNegativeButton(context.getString(R.string.cancel)) { _, _ -> onClick(false) }
        }
        builder.setOnDismissListener {
            onClick(false)
        }

        builder.show()
    }
}