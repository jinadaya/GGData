package com.royaal.journal.di

import com.royaal.journal.data.JournalRepositoryImpl
import com.royaal.journal.domain.JournalRepository
import com.royaal.journal.navigation.JournalRoute
import com.royaal.journal.ui.JournalScreen
import com.royaal.journal.ui.JournalViewModel
import com.royaal.navigation.screen
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val journalModule = module {
    screen<JournalRoute> { JournalScreen() }
    viewModel { JournalViewModel(get(), get()) }
    single<JournalRepository> { JournalRepositoryImpl(get()) }
}
