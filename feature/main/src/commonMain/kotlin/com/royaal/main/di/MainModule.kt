package com.royaal.main.di

import com.royaal.main.data.MainScreenRepositoryImpl
import com.royaal.main.domain.MainScreenRepository
import com.royaal.main.navigation.MainRoute
import com.royaal.main.ui.MainScreen
import com.royaal.main.ui.MainScreenViewModel
import com.royaal.navigation.screen
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel
import org.koin.viewmodel.scope.viewModelScope

val mainModule = module {
    screen<MainRoute> { MainScreen() }
    single<MainScreenRepository> {
        MainScreenRepositoryImpl(
            userGameDao = get(),
            collectionDao = get(),
            gameDao = get(),
            client = get(),
            lastOpenedGameStorage = get(),
        )
    }
    viewModel<MainScreenViewModel> { MainScreenViewModel(get(), get()) }
}
