package com.fixeam.tubesave.utils

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object Md5Utils {
    fun generateMD5String(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        md.update(input.toByteArray())
        val digest = md.digest()
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
    fun generateMD5File(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        val inputStream = FileInputStream(file)
        val buffer = ByteArray(8192)
        var bytesRead = inputStream.read(buffer)
        while (bytesRead != -1) {
            md.update(buffer, 0, bytesRead)
            bytesRead = inputStream.read(buffer)
        }
        val digest = md.digest()
        inputStream.close()
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}