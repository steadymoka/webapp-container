package com.moka.androidwebapp

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics

class _Application : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        FirebaseAnalytics.getInstance(this)
        NotificationUtil.init(this)
    }

}