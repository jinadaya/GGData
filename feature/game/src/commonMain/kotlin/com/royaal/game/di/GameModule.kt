package com.royaal.game.di

import com.royaal.game.data.GameRepositoryImpl
import com.royaal.game.domain.GameRepository
import com.royaal.game.navigation.CompanyRoute
import com.royaal.game.navigation.GameRoute
import com.royaal.game.ui.company.CompanyScreen
import com.royaal.game.ui.company.CompanyViewModel
import com.royaal.game.ui.game.GameScreen
import com.royaal.game.ui.game.GameViewModel
import com.royaal.game.ui.library.LibraryViewModel
import com.royaal.navigation.screen
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val gameModule = module {
    screen<GameRoute> { route -> GameScreen(route.gameId) }
    screen<CompanyRoute> { route -> CompanyScreen(route.companyId) }
    viewModel { params -> GameViewModel(get(), params.get<Long>(), get()) }
    viewModel { params -> CompanyViewModel(get(), params.get<Long>(), get()) }
    viewModel { params -> LibraryViewModel(get(), params.get<Long>(), get()) }
    single<GameRepository> {
        GameRepositoryImpl(
            gameDao = get(),
            userGameDao = get(),
            collectionDao = get(),
            crossRefDao = get(),
            client = get(),
            lastOpenedGameStorage = get(),
        )
    }
}
