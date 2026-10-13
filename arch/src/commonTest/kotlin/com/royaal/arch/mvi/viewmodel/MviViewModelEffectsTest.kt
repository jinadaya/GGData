package com.royaal.arch.mvi.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.royaal.arch.mvi.InnerEvent
import com.royaal.arch.mvi.ScreenSnapshot
import com.royaal.arch.mvi.ScreenState
import com.royaal.arch.mvi.SideEffect
import com.royaal.arch.mvi.SnapshotHandler
import com.royaal.arch.mvi.UserEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MviViewModelEffectsTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun effectsPostedWithoutCollectorAreDeliveredOnceCollected() = runTest {
        val viewModel = TestViewModel()

        viewModel.sendEvent(Post(1))
        viewModel.sendEvent(Post(2))

        assertEquals(listOf(Effect(1), Effect(2)), viewModel.effects.take(2).toList())
    }

    @Test
    fun eachEffectIsDeliveredOnlyOnce() = runTest {
        val viewModel = TestViewModel()
        viewModel.sendEvent(Post(1))
        assertEquals(listOf(Effect(1)), viewModel.effects.take(1).toList())

        viewModel.sendEvent(Post(2))

        assertEquals(listOf(Effect(2)), viewModel.effects.take(1).toList())
    }

    private data object State : ScreenState
    private data class Post(val id: Int) : UserEvent
    private data class Effect(val id: Int) : SideEffect
    private interface Inner : InnerEvent

    private class TestViewModel : MviViewModel<State, Post, Inner, Effect, ScreenSnapshot.NoSnapshot>(
        savedStateHandle = SavedStateHandle(),
        initialState = State,
        reducer = { state, _ -> state },
        snapshotHandler = SnapshotHandler.noSnapshot(State),
    ) {
        override fun onEvent(event: Post) = intent(event) { postEffect(Effect(event.id)) }
    }
}
