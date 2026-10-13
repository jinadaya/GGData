package com.royaal.journal.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** The user's journal: their games by status, with hours, ratings, notes and stats. */
@Serializable
data object JournalRoute : NavKey
