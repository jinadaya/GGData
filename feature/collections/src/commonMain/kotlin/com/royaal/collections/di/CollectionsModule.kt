package com.royaal.collections.di

import com.royaal.collections.data.CollectionsRepositoryImpl
import com.royaal.collections.domain.CollectionsRepository
import com.royaal.collections.navigation.CollectionRoute
import com.royaal.collections.navigation.CollectionsRoute
import com.royaal.collections.navigation.FavouritesRoute
import com.royaal.collections.navigation.StatusCollectionRoute
import com.royaal.collections.domain.CollectionKey
import com.royaal.collections.ui.details.CollectionDetailsScreen
import com.royaal.collections.ui.details.CollectionViewModel
import com.royaal.collections.ui.details.FavouritesScreen
import com.royaal.collections.ui.list.CollectionsScreen
import com.royaal.collections.ui.list.CollectionsViewModel
import com.royaal.navigation.screen
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val collectionsModule = module {
    screen<CollectionRoute> { route ->
        CollectionDetailsScreen(route.id)
    }
    screen<CollectionsRoute> { CollectionsScreen() }
    screen<StatusCollectionRoute> { route ->
        CollectionDetailsScreen(route.status)
    }
    screen<FavouritesRoute> { FavouritesScreen() }
    viewModel { params -> CollectionViewModel(get(), params.get<CollectionKey>(), get()) }
    viewModel { CollectionsViewModel(get(), get()) }
    single<CollectionsRepository> { CollectionsRepositoryImpl(get(), get(), get()) }
}