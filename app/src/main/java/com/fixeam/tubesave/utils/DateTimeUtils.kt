package com.fixeam.tubesave.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis() / 1000
    }

    fun timestampToDateTimeString(timestamp: Long = getCurrentTimestamp(), format: String = "yyyy-MM-dd HH:mm:ss"): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        val date = Date(timestamp * 1000)
        return sdf.format(date)
    }

    fun dateTimeStringToTimestamp(dateTimeString: String, format: String = "yyyy-MM-dd HH:mm:ss"): Long {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        val date = sdf.parse(dateTimeString)
        return date?.time?.div(1000) ?: 0
    }

    fun getDaysInMonth(monthYear: String): Int {
        val (year, month) = monthYear.split("-")
        val yearMonth = YearMonth.of(year.toInt(), month.toInt())
        return yearMonth.lengthOfMonth()
    }

    @SuppressLint("DefaultLocale")
    fun formatTimeCn(seconds: Long): String {
        // 计算小时、分钟和秒
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        // 格式化时间字符串
        return if (hours > 0) {
            String.format("%d时%02d分", hours, minutes)
        } else {
            String.format("%02d分", minutes)
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = seconds % 3600 / 60
        val remainingSeconds = seconds % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        } else {
            String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }

    // 计算时间差
    fun calculateTimeAgo(eventTime: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(eventTime, formatter)
        val now = LocalDateTime.now()

        val seconds = ChronoUnit.SECONDS.between(dateTime, now)
        if (seconds < 60) {
            return "${seconds}秒前"
        }

        val minutes = ChronoUnit.MINUTES.between(dateTime, now)
        if (minutes < 60) {
            return "${minutes}分钟前"
        }

        val today = LocalDate.now()
        val eventDate = dateTime.toLocalDate()

        if (eventDate == today.minusDays(1)) {
            return "昨天" + dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        }

        if (eventDate == today.minusDays(2)) {
            return "前天" + dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        }

        val hours = ChronoUnit.HOURS.between(dateTime, now)
        if (hours < 24) {
            return "${hours}小时前"
        }

        val days = ChronoUnit.DAYS.between(dateTime, now)
        if (days < 7) {
            return "${days}天前"
        }

        val weeks = days / 7
        if (weeks < 4) {
            return "${weeks}周前"
        }

        val months = ChronoUnit.MONTHS.between(dateTime, now)
        if (months < 12) {
            return "${(months + 1)}个月前"
        }

        val years = ChronoUnit.YEARS.between(dateTime, now)
        return "${years}年前"
    }
}