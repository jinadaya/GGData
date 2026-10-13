package com.royaal.ggdata.di

import com.royaal.collections.di.collectionsModule
import com.royaal.game.di.gameModule
import com.royaal.journal.di.journalModule
import com.royaal.main.di.mainModule
import com.royaal.search.di.searchModule
import org.koin.core.module.Module

/** Every feature's Koin module. Screens declared there via `screen<Route>` show up in NavHost. */
internal val featureModules: List<Module> = listOf(
    mainModule,
    searchModule,
    collectionsModule,
    gameModule,
    journalModule,
)
