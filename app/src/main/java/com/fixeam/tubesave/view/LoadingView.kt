package com.fixeam.tubesave.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.fixeam.tubesave.R
import com.fixeam.tubesave.databinding.LoadingViewBinding

class LoadingView : AppCompatActivity() {
    private lateinit var binding: LoadingViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoadingViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val text = intent.getStringExtra("text")
        text?.let {
            binding.textView.text = it
        }
        binding.imageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.loading))
    }

    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }

    @SuppressLint("InlinedApi")
    override fun onResume() {
        binding.imageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.loading))
        super.onResume()
        val filter = IntentFilter("close_loading_activity")
        registerReceiver(closeReceiver, filter, RECEIVER_EXPORTED)
    }

    override fun onPause() {
        binding.imageView.clearAnimation()
        super.onPause()
        unregisterReceiver(closeReceiver)
    }

    override fun onStop() {
        binding.imageView.clearAnimation()
        super.onStop()
    }

    override fun onDestroy() {
        binding.imageView.clearAnimation()
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // 不允许返回
    }
}