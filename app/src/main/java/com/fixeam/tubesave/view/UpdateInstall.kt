package com.fixeam.tubesave.view

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.fixeam.tubesave.controller.newVersion
import com.fixeam.tubesave.databinding.UpdateInstallDialogBinding
import com.fixeam.tubesave.databinding.UpdateTipDialogBinding
import com.fixeam.tubesave.utils.Md5Utils
import java.io.File
import kotlin.system.exitProcess

class UpdateInstall: AppCompatActivity() {
    private lateinit var binding: UpdateInstallDialogBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = UpdateInstallDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(newVersion == null){
            finish()
        }

        binding.close.setOnClickListener { finish() }
        binding.install.setOnClickListener {
            finish()
            install()
        }
    }

    @SuppressLint("NewApi")
    private fun install() {
        val outputFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "${Md5Utils.generateMD5String(newVersion!!.resource)}.apk")
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", outputFile)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        Handler(Looper.getMainLooper()).postDelayed({
            window.setWindowAnimations(0)
            finishAndRemoveTask()
            ActivityCompat.finishAffinity(this)
            exitProcess(0)
        }, 100)
    }
}