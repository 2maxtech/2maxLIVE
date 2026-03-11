package com.twomax.live.ui.navigation

sealed class Screen(val route: String, val title: String, val icon: String) {
    data object Home : Screen("home", "Home", "home")
    data object Search : Screen("search", "Search", "search")
    data object LiveTv : Screen("live_tv", "Live TV", "tv")
    data object Movies : Screen("movies", "Movies", "movie")
    data object Series : Screen("series", "TV Shows", "series")
    data object Epg : Screen("epg", "EPG", "epg")
    data object Favorites : Screen("favorites", "Favorites", "favorites")
    data object Settings : Screen("settings", "Settings", "settings")
    data object Profiles : Screen("profiles", "Profiles", "profiles")
    data object Setup : Screen("setup", "Setup", "setup")
    data object Activation : Screen("activation", "Activation", "activation")
    data object Splash : Screen("splash", "Splash", "splash")
    data object Player : Screen("player/{streamUrl}/{streamTitle}", "Player", "player") {
        fun createRoute(streamUrl: String, streamTitle: String): String {
            val encodedUrl = android.util.Base64.encodeToString(streamUrl.toByteArray(), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
            val encodedTitle = android.util.Base64.encodeToString(streamTitle.toByteArray(), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
            return "player/$encodedUrl/$encodedTitle"
        }
    }
    data object SeriesDetail : Screen("series_detail/{seriesId}", "Series Detail", "series") {
        fun createRoute(seriesId: Long): String = "series_detail/$seriesId"
    }
    data object MovieDetail : Screen("movie_detail/{movieId}", "Movie Detail", "movie") {
        fun createRoute(movieId: Long): String = "movie_detail/$movieId"
    }
}
