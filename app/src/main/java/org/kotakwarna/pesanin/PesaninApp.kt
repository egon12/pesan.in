package org.kotakwarna.pesanin

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PesaninApp : Application() {
    companion object {
        lateinit var instance: PesaninApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}