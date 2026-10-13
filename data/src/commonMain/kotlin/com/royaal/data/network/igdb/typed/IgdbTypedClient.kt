package com.royaal.data.network.igdb.typed

import com.royaal.data.network.igdb.*
import com.royaal.data.network.igdb.model.*
import kotlinx.serialization.serializer
import kotlin.concurrent.Volatile

/** Typed endpoint facade. It is deliberately only a wrapper around the existing IgdbClient. */
class IgdbTypedClient internal constructor(private val client: IgdbClient) {
    val games get() = IgdbTypedEndpoint(client.games, serializer<GameResponse>())
    val expandedGames get() = IgdbTypedEndpoint(client.games, serializer<GameExpandedResponse>())
    val expandedCompanies
        get() = IgdbTypedEndpoint(
            client.companies,
            serializer<CompanyExpandedResponse>()
        )
    val expandedInvolvedCompanies
        get() = IgdbTypedEndpoint(
            client.involvedCompanies,
            serializer<InvolvedCompanyExpandedResponse>()
        )
    val covers get() = IgdbTypedEndpoint(client.covers, serializer<CoverResponse>())
    val artworks get() = IgdbTypedEndpoint(client.artworks, serializer<ArtworkResponse>())
    val screenshots get() = IgdbTypedEndpoint(client.screenshots, serializer<ScreenshotResponse>())
    val genres get() = IgdbTypedEndpoint(client.genres, serializer<GenreResponse>())
    val themes get() = IgdbTypedEndpoint(client.themes, serializer<ThemeResponse>())
    val keywords get() = IgdbTypedEndpoint(client.keywords, serializer<KeywordResponse>())
    val franchises get() = IgdbTypedEndpoint(client.franchises, serializer<FranchiseResponse>())
    val collections get() = IgdbTypedEndpoint(client.collections, serializer<CollectionResponse>())
    val companies get() = IgdbTypedEndpoint(client.companies, serializer<CompanyResponse>())
    val companyLogos get() = IgdbTypedEndpoint(client.companyLogos, serializer<CompanyLogoResponse>())
    val involvedCompanies
        get() = IgdbTypedEndpoint(
            client.involvedCompanies,
            serializer<InvolvedCompanyResponse>()
        )
    val platforms get() = IgdbTypedEndpoint(client.platforms, serializer<PlatformResponse>())
    val releaseDates
        get() = IgdbTypedEndpoint(
            client.releaseDates,
            serializer<ReleaseDateResponse>()
        )
    val gameVideos get() = IgdbTypedEndpoint(client.gameVideos, serializer<GameVideoResponse>())
    val websites get() = IgdbTypedEndpoint(client.websites, serializer<WebsiteResponse>())
    val externalGames
        get() = IgdbTypedEndpoint(
            client.externalGames,
            serializer<ExternalGameResponse>()
        )
    val ageRatings get() = IgdbTypedEndpoint(client.ageRatings, serializer<AgeRatingResponse>())
    val gameEngines get() = IgdbTypedEndpoint(client.gameEngines, serializer<GameEngineResponse>())
    val gameModes get() = IgdbTypedEndpoint(client.gameModes, serializer<GameModeResponse>())
    val gameStatuses
        get() = IgdbTypedEndpoint(
            client.gameStatuses,
            serializer<GameStatusResponse>()
        )
    val gameTypes get() = IgdbTypedEndpoint(client.gameTypes, serializer<GameTypeResponse>())
    val playerPerspectives
        get() = IgdbTypedEndpoint(
            client.playerPerspectives,
            serializer<PlayerPerspectiveResponse>()
        )
    val languageSupports
        get() = IgdbTypedEndpoint(
            client.languageSupports,
            serializer<LanguageSupportResponse>()
        )
    val multiplayerModes
        get() = IgdbTypedEndpoint(
            client.multiplayerModes,
            serializer<MultiplayerModeResponse>()
        )
    val gameTimeToBeats
        get() = IgdbTypedEndpoint(
            client.gameTimeToBeats,
            serializer<GameTimeToBeatResponse>()
        )
    val gameLocalizations
        get() = IgdbTypedEndpoint(
            client.gameLocalizations,
            serializer<GameLocalizationResponse>()
        )
    val platformFamilies
        get() = IgdbTypedEndpoint(
            client.platformFamilies,
            serializer<PlatformFamilyResponse>()
        )
    val platformLogos
        get() = IgdbTypedEndpoint(
            client.platformLogos,
            serializer<PlatformLogoResponse>()
        )
    val languages get() = IgdbTypedEndpoint(client.languages, serializer<LanguageResponse>())
}

val IgdbClient.typed: IgdbTypedClient
    get() = IgdbTypedClient(this)
