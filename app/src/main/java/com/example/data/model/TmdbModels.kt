package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbPagedResponse<T>(
    @Json(name = "page") val page: Int = 1,
    @Json(name = "results") val results: List<T> = emptyList(),
    @Json(name = "total_pages") val totalPages: Int = 1,
    @Json(name = "total_results") val totalResults: Int = 0
)

@JsonClass(generateAdapter = true)
data class TmdbMovie(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String? = null,
    @Json(name = "original_title") val originalTitle: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0,
    @Json(name = "vote_count") val voteCount: Int = 0,
    @Json(name = "original_language") val originalLanguage: String? = null,
    @Json(name = "genre_ids") val genreIds: List<Int>? = null,
    @Json(name = "adult") val adult: Boolean = false
) {
    val displayTitle: String get() = title ?: originalTitle ?: "Untitled Movie"
    val year: String get() = releaseDate?.take(4) ?: "N/A"
    val fullPosterUrl: String? get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    val fullBackdropUrl: String? get() = backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" } ?: fullPosterUrl
}

@JsonClass(generateAdapter = true)
data class TmdbTv(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "original_name") val originalName: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0,
    @Json(name = "vote_count") val voteCount: Int = 0,
    @Json(name = "original_language") val originalLanguage: String? = null,
    @Json(name = "genre_ids") val genreIds: List<Int>? = null,
    @Json(name = "adult") val adult: Boolean = false
) {
    val displayTitle: String get() = name ?: originalName ?: "Untitled Show"
    val year: String get() = firstAirDate?.take(4) ?: "N/A"
    val fullPosterUrl: String? get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    val fullBackdropUrl: String? get() = backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" } ?: fullPosterUrl
}

@JsonClass(generateAdapter = true)
data class TmdbGenre(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class TmdbKeyword(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class TmdbProductionCompany(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "logo_path") val logoPath: String? = null,
    @Json(name = "origin_country") val originCountry: String? = null,
    @Json(name = "adult") val adult: Boolean = false
) {
    val fullLogoUrl: String? get() = logoPath?.let { "https://image.tmdb.org/t/p/w500$it" }
}

@JsonClass(generateAdapter = true)
data class TmdbExternalIds(
    @Json(name = "imdb_id") val imdbId: String? = null,
    @Json(name = "wikidata_id") val wikidataId: String? = null,
    @Json(name = "facebook_id") val facebookId: String? = null,
    @Json(name = "instagram_id") val instagramId: String? = null,
    @Json(name = "twitter_id") val twitterId: String? = null
)

@JsonClass(generateAdapter = true)
data class TmdbMovieDetail(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String? = null,
    @Json(name = "original_title") val originalTitle: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "runtime") val runtime: Int? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0,
    @Json(name = "vote_count") val voteCount: Int = 0,
    @Json(name = "tagline") val tagline: String? = null,
    @Json(name = "imdb_id") val imdbId: String? = null,
    @Json(name = "genres") val genres: List<TmdbGenre>? = null,
    @Json(name = "production_companies") val productionCompanies: List<TmdbProductionCompany>? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "external_ids") val externalIds: TmdbExternalIds? = null,
    @Json(name = "videos") val videos: TmdbVideoContainer? = null,
    @Json(name = "credits") val credits: TmdbCredits? = null,
    @Json(name = "similar") val similar: TmdbPagedResponse<TmdbMovie>? = null,
    @Json(name = "adult") val adult: Boolean = false
) {
    val effectiveImdbId: String? get() = imdbId ?: externalIds?.imdbId
    val displayTitle: String get() = title ?: originalTitle ?: "Untitled Movie"
    val year: String get() = releaseDate?.take(4) ?: "N/A"
    val formattedRuntime: String get() = runtime?.let { "${it / 60}h ${it % 60}m" } ?: "N/A"
    val fullPosterUrl: String? get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    val fullBackdropUrl: String? get() = backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" } ?: fullPosterUrl
}

@JsonClass(generateAdapter = true)
data class TmdbSeasonSummary(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "season_number") val seasonNumber: Int = 0,
    @Json(name = "episode_count") val episodeCount: Int = 0,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "air_date") val airDate: String? = null
)

@JsonClass(generateAdapter = true)
data class TmdbTvDetail(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "original_name") val originalName: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "number_of_seasons") val numberOfSeasons: Int = 1,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int = 1,
    @Json(name = "vote_average") val voteAverage: Double = 0.0,
    @Json(name = "vote_count") val voteCount: Int = 0,
    @Json(name = "tagline") val tagline: String? = null,
    @Json(name = "genres") val genres: List<TmdbGenre>? = null,
    @Json(name = "production_companies") val productionCompanies: List<TmdbProductionCompany>? = null,
    @Json(name = "external_ids") val externalIds: TmdbExternalIds? = null,
    @Json(name = "seasons") val seasons: List<TmdbSeasonSummary>? = null,
    @Json(name = "videos") val videos: TmdbVideoContainer? = null,
    @Json(name = "credits") val credits: TmdbCredits? = null,
    @Json(name = "similar") val similar: TmdbPagedResponse<TmdbTv>? = null,
    @Json(name = "adult") val adult: Boolean = false
) {
    val effectiveImdbId: String? get() = externalIds?.imdbId
    val displayTitle: String get() = name ?: originalName ?: "Untitled Show"
    val year: String get() = firstAirDate?.take(4) ?: "N/A"
    val fullPosterUrl: String? get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    val fullBackdropUrl: String? get() = backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" } ?: fullPosterUrl
}

@JsonClass(generateAdapter = true)
data class TmdbSeasonDetail(
    @Json(name = "id") val id: Int,
    @Json(name = "season_number") val seasonNumber: Int = 0,
    @Json(name = "name") val name: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "episodes") val episodes: List<TmdbEpisode> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbEpisode(
    @Json(name = "id") val id: Int,
    @Json(name = "episode_number") val episodeNumber: Int = 1,
    @Json(name = "season_number") val seasonNumber: Int = 1,
    @Json(name = "name") val name: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "still_path") val stillPath: String? = null,
    @Json(name = "runtime") val runtime: Int? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0,
    @Json(name = "air_date") val airDate: String? = null,
    @Json(name = "external_ids") val externalIds: TmdbExternalIds? = null
) {
    val effectiveImdbId: String? get() = externalIds?.imdbId
    val displayTitle: String get() = name ?: "Episode $episodeNumber"
    val fullStillUrl: String? get() = stillPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    val formattedDuration: String get() = runtime?.let { "${it}m" } ?: "45m"
}

@JsonClass(generateAdapter = true)
data class TmdbVideoContainer(
    @Json(name = "results") val results: List<TmdbVideo> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbVideo(
    @Json(name = "id") val id: String,
    @Json(name = "key") val key: String,
    @Json(name = "name") val name: String,
    @Json(name = "site") val site: String,
    @Json(name = "type") val type: String
)

@JsonClass(generateAdapter = true)
data class TmdbCredits(
    @Json(name = "cast") val cast: List<TmdbCastMember> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbCastMember(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "character") val character: String? = null,
    @Json(name = "profile_path") val profilePath: String? = null
) {
    val fullProfileUrl: String? get() = profilePath?.let { "https://image.tmdb.org/t/p/w185$it" }
}

@JsonClass(generateAdapter = true)
data class TmdbCollectionDetail(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "parts") val parts: List<TmdbMovie> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbCollectionItem(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "original_language") val originalLanguage: String? = null
) {
    val displayTitle: String get() = name ?: "Untitled Collection"
    val fullPosterUrl: String? get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    val fullBackdropUrl: String? get() = backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" } ?: fullPosterUrl
}

// === PEOPLE & CAST MODELS ===
@JsonClass(generateAdapter = true)
data class TmdbPerson(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "original_name") val originalName: String? = null,
    @Json(name = "profile_path") val profilePath: String? = null,
    @Json(name = "known_for_department") val knownForDepartment: String? = null,
    @Json(name = "popularity") val popularity: Double = 0.0,
    @Json(name = "adult") val adult: Boolean = false
) {
    val fullProfileUrl: String? get() = profilePath?.let { "https://image.tmdb.org/t/p/w500$it" }
}

@JsonClass(generateAdapter = true)
data class TmdbPersonMovieCredits(
    @Json(name = "cast") val cast: List<TmdbMovie> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbPersonTvCredits(
    @Json(name = "cast") val cast: List<TmdbTv> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbPersonCombinedCredits(
    @Json(name = "cast") val cast: List<TmdbPersonCreditItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbPersonCreditItem(
    @Json(name = "id") val id: Int,
    @Json(name = "media_type") val mediaType: String? = null, // "movie" or "tv"
    @Json(name = "title") val title: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "character") val character: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0,
    @Json(name = "adult") val adult: Boolean = false
) {
    val displayTitle: String get() = title ?: name ?: "Untitled"
    val year: String get() = (releaseDate ?: firstAirDate)?.take(4) ?: "N/A"
    val fullPosterUrl: String? get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
}

@JsonClass(generateAdapter = true)
data class TmdbPersonDetail(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "biography") val biography: String? = null,
    @Json(name = "birthday") val birthday: String? = null,
    @Json(name = "deathday") val deathday: String? = null,
    @Json(name = "place_of_birth") val placeOfBirth: String? = null,
    @Json(name = "profile_path") val profilePath: String? = null,
    @Json(name = "known_for_department") val knownForDepartment: String? = null,
    @Json(name = "movie_credits") val movieCredits: TmdbPersonMovieCredits? = null,
    @Json(name = "tv_credits") val tvCredits: TmdbPersonTvCredits? = null,
    @Json(name = "adult") val adult: Boolean = false
) {
    val fullProfileUrl: String? get() = profilePath?.let { "https://image.tmdb.org/t/p/w500$it" }
}

