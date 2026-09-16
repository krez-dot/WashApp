package com.washapp

import android.app.Application
import com.google.firebase.FirebaseApp

class WashApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
