package com.royaal.data.network.igdb.typed

/** Typed APICalypse schema for the core Game graph. No code generation is involved. */
object GameFields {
    val id = LongField("id")
    val ageRatings = LongListField("age_ratings")
    val aggregatedRating = NumberField<Double>("aggregated_rating")
    val aggregatedRatingCount = NumberField<Int>("aggregated_rating_count")
    val alternativeNames = GameAlternativeNamesField("alternative_names")
    val artworks = GameImagesField("artworks")
    val bundles = LongListField("bundles")
    val category = NumberField<Int>("category")
    val checksum = StringField("checksum")
    val collection = LongField("collection")
    val collections = GameNamedRefsField("collections")
    val cover = GameCoverField("cover")
    val createdAt = LongField("created_at")
    val dlcs = LongListField("dlcs")
    val expandedGames = LongListField("expanded_games")
    val expansions = LongListField("expansions")
    val externalGames = LongListField("external_games")
    val firstReleaseDate = LongField("first_release_date")
    val follows = NumberField<Int>("follows")
    val forks = LongListField("forks")
    val franchise = LongField("franchise")
    val franchises = GameNamedRefsField("franchises")
    val gameEngines = LongListField("game_engines")
    val gameLocalizations = LongListField("game_localizations")
    val gameModes = LongListField("game_modes")
    val gameStatus = LongField("game_status")
    val gameType = LongField("game_type")
    val genres = GameGenresField("genres")
    val hypes = NumberField<Int>("hypes")
    val involvedCompanies = GameInvolvedCompaniesField("involved_companies")
    val keywords = LongListField("keywords")
    val languageSupports = LongListField("language_supports")
    val multiplayerModes = LongListField("multiplayer_modes")
    val name = StringField("name")
    val parentGame = LongField("parent_game")
    val platforms = GamePlatformsField("platforms")
    val playerPerspectives = LongListField("player_perspectives")
    val ports = LongListField("ports")
    val rating = NumberField<Double>("rating")
    val ratingCount = NumberField<Int>("rating_count")
    val releaseDates = GameReleaseDatesField("release_dates")
    val remakes = LongListField("remakes")
    val remasters = LongListField("remasters")
    val screenshots = GameScreenshotsField("screenshots")
    val similarGames = LongListField("similar_games")
    val slug = StringField("slug")
    val standaloneExpansions = LongListField("standalone_expansions")

    /** The 0..8 release-status enum (released/alpha/beta/...). NOT a reference - see [gameStatus] for that. */
    val status = NumberField<Int>("status")
    val storyline = StringField("storyline")
    val summary = StringField("summary")
    val tags = LongListField("tags")
    val themes = LongListField("themes")
    val totalRating = NumberField<Double>("total_rating")
    val totalRatingCount = NumberField<Int>("total_rating_count")
    val updatedAt = LongField("updated_at")
    val url = StringField("url")
    val versionParent = LongField("version_parent")
    val versionTitle = StringField("version_title")
    val videos = GameVideosField("videos")
    val websites = LongListField("websites")
}

/** A game -> cover reference. The field itself selects the cover id. */
class GameCoverField(path: String) : LongField(path) {
    val id = LongField("$path.id")
    val alphaChannel = BooleanField("$path.alpha_channel")
    val animated = BooleanField("$path.animated")
    val checksum = StringField("$path.checksum")
    val game = LongField("$path.game")
    val gameLocalization = LongField("$path.game_localization")
    val height = NumberField<Int>("$path.height")
    val imageId = StringField("$path.image_id")
    val url = StringField("$path.url")
    val width = NumberField<Int>("$path.width")
}

/** A game -> images reference list (artworks). The field itself selects image ids. */
class GameImagesField(path: String) : LongListField(path) {
    val id = LongField("$path.id")
    val imageId = StringField("$path.image_id")
    val width = NumberField<Int>("$path.width")
    val height = NumberField<Int>("$path.height")
}

/** A game -> named entities reference list (series, franchises). The field itself selects their ids. */
class GameNamedRefsField(path: String) : LongListField(path) {
    val id = LongField("$path.id")
    val name = StringField("$path.name")
    val slug = StringField("$path.slug")
}

/** A game -> alternative names reference list. The field itself selects alternative-name ids. */
class GameAlternativeNamesField(path: String) : LongListField(path) {
    val id = LongField("$path.id")
    val checksum = StringField("$path.checksum")
    val comment = StringField("$path.comment")
    val game = LongField("$path.game")
    val name = StringField("$path.name")
}

/** A game -> platforms reference list. The field itself selects platform ids. */
class GamePlatformsField(path: String) : LongListField(path) {
    val id = LongField("$path.id")
    val abbreviation = StringField("$path.abbreviation")
    val alternativeName = StringField("$path.alternative_name")
    val category = NumberField<Int>("$path.category")
    val checksum = StringField("$path.checksum")
    val createdAt = LongField("$path.created_at")
    val generation = NumberField<Int>("$path.generation")
    val name = StringField("$path.name")
    val platformFamily = LongField("$path.platform_family")
    val platformLogo = LongField("$path.platform_logo")
    val platformType = LongField("$path.platform_type")
    val slug = StringField("$path.slug")
    val summary = StringField("$path.summary")
    val updatedAt = LongField("$path.updated_at")
    val url = StringField("$path.url")
    val versions = LongListField("$path.versions")
    val websites = LongListField("$path.websites")
}

/** A game -> genres reference list. The field itself selects genre ids. */
class GameGenresField(path: String) : LongListField(path) {
    val id = LongField("$path.id")
    val checksum = StringField("$path.checksum")
    val createdAt = LongField("$path.created_at")
    val name = StringField("$path.name")
    val slug = StringField("$path.slug")
    val updatedAt = LongField("$path.updated_at")
}

/** A game -> release dates reference list. The field itself selects release-date ids. */
class GameReleaseDatesField(path: String) : LongListField(path) {
    val id = LongField("$path.id")
    val category = NumberField<Int>("$path.category")
    val checksum = StringField("$path.checksum")
    val createdAt = LongField("$path.created_at")
    val date = LongField("$path.date")
    val dateFormat = LongField("$path.date_format")
    val game = LongField("$path.game")
    val human = StringField("$path.human")
    val month = NumberField<Int>("$path.m")
    val platform = LongField("$path.platform")
    val region = NumberField<Int>("$path.region")
    val releaseRegion = LongField("$path.release_region")
    val status = LongField("$path.status")
    val updatedAt = LongField("$path.updated_at")
    val year = NumberField<Int>("$path.y")
}

/** A game -> screenshots reference list. The field itself selects screenshot ids. */
class GameScreenshotsField(path: String) : LongListField(path) {
    val id = LongField("$path.id")
    val alphaChannel = BooleanField("$path.alpha_channel")
    val animated = BooleanField("$path.animated")
    val checksum = StringField("$path.checksum")
    val game = LongField("$path.game")
    val height = NumberField<Int>("$path.height")
    val imageId = StringField("$path.image_id")
    val url = StringField("$path.url")
    val width = NumberField<Int>("$path.width")
}

/** A game -> videos reference list. The field itself selects video ids. */
class GameVideosField(path: String) : LongListField(path) {
    val id = LongField("$path.id")
    val checksum = StringField("$path.checksum")
    val game = LongField("$path.game")
    val name = StringField("$path.name")
    val videoId = StringField("$path.video_id")
}

/** A game -> involved companies reference list. The field itself selects relation ids. */
class GameInvolvedCompaniesField(path: String) : LongListField(path) {
    val id = LongField("$path.id")
    val checksum = StringField("$path.checksum")
    val company = LongField("$path.company")
    val companyName = StringField("$path.company.name")
    val createdAt = LongField("$path.created_at")
    val developer = BooleanField("$path.developer")
    val game = LongField("$path.game")
    val porting = BooleanField("$path.porting")
    val publisher = BooleanField("$path.publisher")
    val supporting = BooleanField("$path.supporting")
    val updatedAt = LongField("$path.updated_at")
}

object CoverFields {
    val id = LongField("id")
    val alphaChannel = BooleanField("alpha_channel")
    val animated = BooleanField("animated")
    val checksum = StringField("checksum")
    val game = LongField("game")
    val gameLocalization = LongField("game_localization")
    val height = NumberField<Int>("height")
    val imageId = StringField("image_id")
    val url = StringField("url")
    val width = NumberField<Int>("width")
}

object ArtworkFields {
    val id = LongField("id")
    val alphaChannel = BooleanField("alpha_channel")
    val animated = BooleanField("animated")
    val artworkType = LongField("artwork_type")
    val checksum = StringField("checksum")
    val game = LongField("game")
    val height = NumberField<Int>("height")
    val imageId = StringField("image_id")
    val url = StringField("url")
    val width = NumberField<Int>("width")
}

object ScreenshotFields {
    val id = LongField("id")
    val alphaChannel = BooleanField("alpha_channel")
    val animated = BooleanField("animated")
    val checksum = StringField("checksum")
    val game = LongField("game")
    val height = NumberField<Int>("height")
    val imageId = StringField("image_id")
    val url = StringField("url")
    val width = NumberField<Int>("width")
}

object GenreFields {
    val id = LongField("id")
    val checksum = StringField("checksum")
    val createdAt = LongField("created_at")
    val name = StringField("name")
    val slug = StringField("slug")
    val updatedAt = LongField("updated_at")
}

object PlatformFields {
    val id = LongField("id")
    val abbreviation = StringField("abbreviation")
    val alternativeName = StringField("alternative_name")
    val category = NumberField<Int>("category")
    val checksum = StringField("checksum")
    val createdAt = LongField("created_at")
    val generation = NumberField<Int>("generation")
    val name = StringField("name")
    val platformFamily = LongField("platform_family")
    val platformLogo = LongField("platform_logo")
    val platformType = LongField("platform_type")
    val slug = StringField("slug")
    val summary = StringField("summary")
    val updatedAt = LongField("updated_at")
    val url = StringField("url")
    val versions = LongListField("versions")
    val websites = LongListField("websites")
}

object ReleaseDateFields {
    val id = LongField("id")
    val category = NumberField<Int>("category")
    val checksum = StringField("checksum")
    val createdAt = LongField("created_at")
    val date = LongField("date")
    val dateFormat = LongField("date_format")
    val game = LongField("game")
    val human = StringField("human")
    val month = NumberField<Int>("m")
    val platform = LongField("platform")
    val region = NumberField<Int>("region")
    val releaseRegion = LongField("release_region")
    val status = LongField("status")
    val updatedAt = LongField("updated_at")
    val year = NumberField<Int>("y")
}

object InvolvedCompanyFields {
    val id = LongField("id")
    val checksum = StringField("checksum")
    val company = LongField("company")
    val companyName = StringField("company.name")
    val createdAt = LongField("created_at")
    val developer = BooleanField("developer")
    val game = LongField("game")
    val porting = BooleanField("porting")
    val publisher = BooleanField("publisher")
    val supporting = BooleanField("supporting")
    val updatedAt = LongField("updated_at")
}

object CompanyFields {
    val id = LongField("id")
    val country = NumberField<Int>("country")
    val description = StringField("description")
    val developed = LongListField("developed")
    val logo = LongField("logo")
    val name = StringField("name")
    val parent = LongField("parent")
    val published = LongListField("published")
    val slug = StringField("slug")
    val startDate = LongField("start_date")
    val url = StringField("url")
}

object CompanyLogoFields {
    val id = LongField("id")
    val imageId = StringField("image_id")
    val width = NumberField<Int>("width")
    val height = NumberField<Int>("height")
}

object GameTimeToBeatFields {
    val id = LongField("id")
    val gameId = LongField("game_id")
    val hastily = NumberField<Int>("hastily")
    val normally = NumberField<Int>("normally")
    val completely = NumberField<Int>("completely")
}
