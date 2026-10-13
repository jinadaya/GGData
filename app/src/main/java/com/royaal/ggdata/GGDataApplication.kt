package com.royaal.ggdata

import android.app.Application
import com.royaal.ggdata.di.initAppKoin
import org.koin.android.ext.koin.androidContext

class GGDataApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initAppKoin(
            clientId = BuildConfig.IGDB_CLIENT_ID,
            clientSecret = BuildConfig.IGDB_CLIENT_SECRET,
        ) {
            androidContext(this@GGDataApplication)
        }
    }
}
