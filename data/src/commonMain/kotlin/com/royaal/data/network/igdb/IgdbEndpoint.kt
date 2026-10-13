package com.royaal.data.network.igdb

/**
 * An IGDB endpoint path (relative to `https://api.igdb.com/v4/`).
 *
 * All known v4 endpoints are predefined; for anything IGDB adds later just use
 * `IgdbEndpoint("new_endpoint")`.
 */
class IgdbEndpoint(path: String) {
    val path: String = path.trim().trim('/')

    init {
        require(this.path.isNotEmpty()) { "Endpoint path must not be blank" }
    }

    override fun equals(other: Any?): Boolean = other is IgdbEndpoint && other.path == path
    override fun hashCode(): Int = path.hashCode()
    override fun toString(): String = path

    companion object {
        val AgeRatings = IgdbEndpoint("age_ratings")
        val AgeRatingCategories = IgdbEndpoint("age_rating_categories")
        val AgeRatingContentDescriptions = IgdbEndpoint("age_rating_content_descriptions")
        val AgeRatingContentDescriptionsV2 = IgdbEndpoint("age_rating_content_descriptions_v2")
        val AgeRatingOrganizations = IgdbEndpoint("age_rating_organizations")
        val AlternativeNames = IgdbEndpoint("alternative_names")
        val Artworks = IgdbEndpoint("artworks")
        val ArtworkTypes = IgdbEndpoint("artwork_types")
        val CharacterGenders = IgdbEndpoint("character_genders")
        val CharacterMugShots = IgdbEndpoint("character_mug_shots")
        val Characters = IgdbEndpoint("characters")
        val CharacterSpecies = IgdbEndpoint("character_species")
        val CollectionMemberships = IgdbEndpoint("collection_memberships")
        val CollectionMembershipTypes = IgdbEndpoint("collection_membership_types")
        val CollectionRelations = IgdbEndpoint("collection_relations")
        val CollectionRelationTypes = IgdbEndpoint("collection_relation_types")
        val Collections = IgdbEndpoint("collections")
        val CollectionTypes = IgdbEndpoint("collection_types")
        val Companies = IgdbEndpoint("companies")
        val CompanyLogos = IgdbEndpoint("company_logos")
        val CompanyStatuses = IgdbEndpoint("company_statuses")
        val CompanyWebsites = IgdbEndpoint("company_websites")
        val Covers = IgdbEndpoint("covers")
        val DateFormats = IgdbEndpoint("date_formats")
        val EventLogos = IgdbEndpoint("event_logos")
        val EventNetworks = IgdbEndpoint("event_networks")
        val Events = IgdbEndpoint("events")
        val ExternalGames = IgdbEndpoint("external_games")
        val ExternalGameSources = IgdbEndpoint("external_game_sources")
        val Franchises = IgdbEndpoint("franchises")
        val GameEngineLogos = IgdbEndpoint("game_engine_logos")
        val GameEngines = IgdbEndpoint("game_engines")
        val GameLocalizations = IgdbEndpoint("game_localizations")
        val GameModes = IgdbEndpoint("game_modes")
        val GameReleaseFormats = IgdbEndpoint("game_release_formats")
        val Games = IgdbEndpoint("games")
        val GameStatuses = IgdbEndpoint("game_statuses")
        val GameTimeToBeats = IgdbEndpoint("game_time_to_beats")
        val GameTypes = IgdbEndpoint("game_types")
        val GameVersions = IgdbEndpoint("game_versions")
        val GameVersionFeatures = IgdbEndpoint("game_version_features")
        val GameVersionFeatureValues = IgdbEndpoint("game_version_feature_values")
        val GameVideos = IgdbEndpoint("game_videos")
        val Genres = IgdbEndpoint("genres")
        val InvolvedCompanies = IgdbEndpoint("involved_companies")
        val Keywords = IgdbEndpoint("keywords")
        val Languages = IgdbEndpoint("languages")
        val LanguageSupports = IgdbEndpoint("language_supports")
        val LanguageSupportTypes = IgdbEndpoint("language_support_types")
        val MultiplayerModes = IgdbEndpoint("multiplayer_modes")
        val NetworkTypes = IgdbEndpoint("network_types")
        val PlatformFamilies = IgdbEndpoint("platform_families")
        val PlatformLogos = IgdbEndpoint("platform_logos")
        val Platforms = IgdbEndpoint("platforms")
        val PlatformTypes = IgdbEndpoint("platform_types")
        val PlatformVersions = IgdbEndpoint("platform_versions")
        val PlatformVersionCompanies = IgdbEndpoint("platform_version_companies")
        val PlatformVersionReleaseDates = IgdbEndpoint("platform_version_release_dates")
        val PlatformWebsites = IgdbEndpoint("platform_websites")
        val PlayerPerspectives = IgdbEndpoint("player_perspectives")
        val PopularityPrimitives = IgdbEndpoint("popularity_primitives")
        val PopularityTypes = IgdbEndpoint("popularity_types")
        val Regions = IgdbEndpoint("regions")
        val ReleaseDates = IgdbEndpoint("release_dates")
        val ReleaseDateRegions = IgdbEndpoint("release_date_regions")
        val ReleaseDateStatuses = IgdbEndpoint("release_date_statuses")
        val Screenshots = IgdbEndpoint("screenshots")
        val Search = IgdbEndpoint("search")
        val Themes = IgdbEndpoint("themes")
        val Websites = IgdbEndpoint("websites")
        val WebsiteTypes = IgdbEndpoint("website_types")

        /** Every predefined endpoint (declared last so all vals above are initialised). */
        val all: List<IgdbEndpoint> = listOf(
            AgeRatings, AgeRatingCategories, AgeRatingContentDescriptions, AgeRatingContentDescriptionsV2,
            AgeRatingOrganizations, AlternativeNames, Artworks, ArtworkTypes, CharacterGenders,
            CharacterMugShots, Characters, CharacterSpecies, CollectionMemberships, CollectionMembershipTypes,
            CollectionRelations, CollectionRelationTypes, Collections, CollectionTypes, Companies, CompanyLogos,
            CompanyStatuses, CompanyWebsites, Covers, DateFormats, EventLogos, EventNetworks, Events,
            ExternalGames, ExternalGameSources, Franchises, GameEngineLogos, GameEngines, GameLocalizations,
            GameModes, GameReleaseFormats, Games, GameStatuses, GameTimeToBeats, GameTypes, GameVersions,
            GameVersionFeatures, GameVersionFeatureValues, GameVideos, Genres, InvolvedCompanies, Keywords,
            Languages, LanguageSupports, LanguageSupportTypes, MultiplayerModes, NetworkTypes, PlatformFamilies,
            PlatformLogos, Platforms, PlatformTypes, PlatformVersions, PlatformVersionCompanies,
            PlatformVersionReleaseDates, PlatformWebsites, PlayerPerspectives, PopularityPrimitives,
            PopularityTypes, Regions, ReleaseDates, ReleaseDateRegions, ReleaseDateStatuses, Screenshots,
            Search, Themes, Websites, WebsiteTypes,
        )
    }
}
