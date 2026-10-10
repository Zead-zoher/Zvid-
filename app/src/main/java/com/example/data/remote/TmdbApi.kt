package com.example.data.remote

import com.example.data.model.TmdbMovie
import com.example.data.model.TmdbMovieDetail
import com.example.data.model.TmdbPagedResponse
import com.example.data.model.TmdbSeasonDetail
import com.example.data.model.TmdbTv
import com.example.data.model.TmdbTvDetail
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    // === MOVIES ===
    @GET("trending/movie/{time_window}")
    suspend fun getTrendingMovies(
        @Path("time_window") timeWindow: String = "day",
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null
    ): TmdbPagedResponse<TmdbMovie>

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null,
        @Query("region") region: String? = null
    ): TmdbPagedResponse<TmdbMovie>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null,
        @Query("region") region: String? = null
    ): TmdbPagedResponse<TmdbMovie>

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("with_genres") withGenres: String? = null,
        @Query("with_companies") withCompanies: String? = null,
        @Query("with_keywords") withKeywords: String? = null,
        @Query("without_keywords") withoutKeywords: String? = null,
        @Query("without_companies") withoutCompanies: String? = null,
        @Query("with_original_language") withOriginalLanguage: String? = null,
        @Query("region") region: String? = null,
        @Query("language") language: String? = null,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("vote_count.gte") voteCountGte: Int? = null,
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbPagedResponse<TmdbMovie>

    @GET("discover/movie")
    suspend fun discoverMoviesByGenre(
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("language") language: String? = null,
        @Query("without_keywords") withoutKeywords: String? = null,
        @Query("without_companies") withoutCompanies: String? = null,
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbPagedResponse<TmdbMovie>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null,
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbPagedResponse<TmdbMovie>

    @GET("search/keyword")
    suspend fun searchKeywords(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<com.example.data.model.TmdbKeyword>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "credits,videos,similar,keywords",
        @Query("language") language: String? = null
    ): TmdbMovieDetail

    @GET("collection/{collection_id}")
    suspend fun getCollectionDetails(
        @Path("collection_id") collectionId: Int,
        @Query("language") language: String? = null
    ): com.example.data.model.TmdbCollectionDetail

    @GET("search/collection")
    suspend fun searchCollections(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null
    ): TmdbPagedResponse<com.example.data.model.TmdbCollectionItem>

    // === TV SERIES ===
    @GET("trending/tv/{time_window}")
    suspend fun getTrendingTv(
        @Path("time_window") timeWindow: String = "day",
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null
    ): TmdbPagedResponse<TmdbTv>

    @GET("tv/popular")
    suspend fun getPopularTv(
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null
    ): TmdbPagedResponse<TmdbTv>

    @GET("tv/top_rated")
    suspend fun getTopRatedTv(
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null
    ): TmdbPagedResponse<TmdbTv>

    @GET("discover/tv")
    suspend fun discoverTv(
        @Query("with_genres") withGenres: String? = null,
        @Query("with_companies") withCompanies: String? = null,
        @Query("with_keywords") withKeywords: String? = null,
        @Query("without_keywords") withoutKeywords: String? = null,
        @Query("without_companies") withoutCompanies: String? = null,
        @Query("with_original_language") withOriginalLanguage: String? = null,
        @Query("language") language: String? = null,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("vote_count.gte") voteCountGte: Int? = null,
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbPagedResponse<TmdbTv>

    @GET("discover/tv")
    suspend fun discoverTvByGenre(
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("language") language: String? = null,
        @Query("without_keywords") withoutKeywords: String? = null,
        @Query("without_companies") withoutCompanies: String? = null,
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbPagedResponse<TmdbTv>

    @GET("search/tv")
    suspend fun searchTv(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null,
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbPagedResponse<TmdbTv>

    @GET("tv/{tv_id}")
    suspend fun getTvDetails(
        @Path("tv_id") tvId: Int,
        @Query("append_to_response") appendToResponse: String = "credits,videos,similar,keywords",
        @Query("language") language: String? = null
    ): TmdbTvDetail

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null,
        @Query("region") region: String? = null
    ): TmdbPagedResponse<TmdbMovie>

    @GET("tv/on_the_air")
    suspend fun getOnTheAirTv(
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null
    ): TmdbPagedResponse<TmdbTv>

    // === COMPANIES ===
    @GET("company/{company_id}")
    suspend fun getCompanyDetails(
        @Path("company_id") companyId: Int
    ): com.example.data.model.TmdbProductionCompany

    @GET("search/company")
    suspend fun searchCompanies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<com.example.data.model.TmdbProductionCompany>

    // === PEOPLE ===
    @GET("person/popular")
    suspend fun getPopularPeople(
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null
    ): TmdbPagedResponse<com.example.data.model.TmdbPerson>

    @GET("search/person")
    suspend fun searchPeople(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null,
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbPagedResponse<com.example.data.model.TmdbPerson>

    @GET("person/{person_id}")
    suspend fun getPersonDetails(
        @Path("person_id") personId: Int,
        @Query("append_to_response") appendToResponse: String = "movie_credits,tv_credits",
        @Query("language") language: String? = null
    ): com.example.data.model.TmdbPersonDetail

    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getTvSeasonDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("language") language: String? = null
    ): TmdbSeasonDetail
}
