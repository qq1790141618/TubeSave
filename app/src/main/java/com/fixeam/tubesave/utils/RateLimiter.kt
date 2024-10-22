package com.fixeam.tubesave.utils

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*

object RateLimiter {
    private var job: Job? = null

    // 防抖器函数
    @OptIn(DelicateCoroutinesApi::class)
    fun <T> debounce(delayMillis: Long, action: (T) -> Unit): (T) -> Unit {
        return { value: T ->
            job?.cancel() // 取消之前的任务
            job = GlobalScope.launch {
                delay(delayMillis)
                action(value)
            }
        }
    }

    // 节流器函数
    fun <T> throttle(delayMillis: Long, action: (T) -> Unit): (T) -> Unit {
        var lastCallTime = 0L
        return { value: T ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastCallTime >= delayMillis) {
                lastCallTime = currentTime
                action(value)
            }
        }
    }
}