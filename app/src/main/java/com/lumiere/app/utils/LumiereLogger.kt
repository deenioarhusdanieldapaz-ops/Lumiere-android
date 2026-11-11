package com.lumiere.app.utils

import android.util.Log

object LumiereLogger {
    private const val TAG = "LumiereApp"

    fun d(tag: String, msg: String) = Log.d("$TAG-$tag", msg)
    fun e(tag: String, msg: String, throwable: Throwable? = null) = Log.e("$TAG-$tag", msg, throwable)
    fun i(tag: String, msg: String) = Log.i("$TAG-$tag", msg)
}
