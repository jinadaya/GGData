package com.royaal.search.di

import com.royaal.navigation.screen
import com.royaal.search.data.SearchRepositoryImpl
import com.royaal.search.domain.SearchRepository
import com.royaal.search.navigation.SearchRoute
import com.royaal.search.ui.SearchScreen
import com.royaal.search.ui.SearchScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val searchModule = module {
    screen<SearchRoute> { SearchScreen() }
    single<SearchRepository> { SearchRepositoryImpl(get(), get()) }
    viewModel<SearchScreenViewModel> { SearchScreenViewModel(get(), get()) }
}
