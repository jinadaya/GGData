package com.royaal.ggdata

import com.royaal.ggdata.di.initAppKoin

/** Swift-callable bridge: `KoinInitializerKt.doInitKoin(clientId:clientSecret:)`. */
fun doInitKoin(clientId: String, clientSecret: String) {
    initAppKoin(clientId, clientSecret)
}
