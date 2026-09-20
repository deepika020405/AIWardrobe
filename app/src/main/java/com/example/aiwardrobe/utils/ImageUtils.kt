package com.example.aiwardrobe.utils

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.InputStream

fun imageUriToBase64(
    context: Context,
    uri: Uri
): String {

    val inputStream: InputStream =
        context.contentResolver.openInputStream(uri)
            ?: throw Exception("Unable to open image")

    val bytes = inputStream.use {
        it.readBytes()
    }

    return Base64.encodeToString(
        bytes,
        Base64.NO_WRAP
    )
}