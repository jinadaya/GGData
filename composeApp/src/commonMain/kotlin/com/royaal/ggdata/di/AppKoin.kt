package com.royaal.ggdata.di

import com.royaal.data.di.KoinAppDeclaration
import com.royaal.data.di.initDataKoin

/** App-level Koin bootstrap: :data's modules plus every feature from [featureModules]. */
fun initAppKoin(clientId: String, clientSecret: String, appDeclaration: KoinAppDeclaration = {}) {
    initDataKoin(clientId, clientSecret) {
        appDeclaration()
        modules(featureModules)
    }
}
