package com.royaal.game.ui.company

import com.royaal.arch.mvi.InnerEvent
import com.royaal.arch.mvi.ScreenState
import com.royaal.arch.mvi.SideEffect
import com.royaal.arch.mvi.UserEvent
import com.royaal.entities.ui.UiGame
import com.royaal.game.domain.CompanyDetails

/** @param company `null` until loaded. */
internal data class CompanyScreenState(
    val companyId: Long,
    val company: CompanyDetails? = null,
    val games: List<UiGame> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadFailed: Boolean = false,
) : ScreenState

internal sealed interface CompanyUserEvent : UserEvent {
    data object Retry : CompanyUserEvent
    data class GameClicked(val gameId: Long) : CompanyUserEvent
}

internal sealed interface CompanySideEffect : SideEffect {
    data class OpenGame(val gameId: Long) : CompanySideEffect
}

internal sealed interface CompanyInnerEvent : InnerEvent {
    data object Loading : CompanyInnerEvent
    data class Loaded(val company: CompanyDetails, val games: List<UiGame>) : CompanyInnerEvent
    data object LoadFailed : CompanyInnerEvent
}
