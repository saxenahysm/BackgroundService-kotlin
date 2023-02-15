package com.shyam.roomdbexample.UtilsForBG

import android.os.Binder

class MyBinder : Binder() {
    fun getService(): LocationService.Companion {
        return LocationService
    }
}