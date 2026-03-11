package com.twomax.live.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.twomax.live.ui.screens.home.HomeScreen
import com.twomax.live.ui.screens.search.SearchScreen
import com.twomax.live.ui.screens.livetv.LiveTvScreen
import com.twomax.live.ui.screens.movies.MoviesScreen
import com.twomax.live.ui.screens.series.SeriesScreen
import com.twomax.live.ui.screens.series.SeriesDetailScreen
import com.twomax.live.ui.screens.epg.EpgScreen
import com.twomax.live.ui.screens.player.PlayerScreen
import com.twomax.live.ui.screens.setup.SetupScreen
import com.twomax.live.ui.screens.profiles.ProfilesScreen
import com.twomax.live.ui.screens.favorites.FavoritesScreen
import com.twomax.live.ui.screens.activation.ActivationScreen
import com.twomax.live.ui.screens.movies.MovieDetailScreen
import com.twomax.live.ui.screens.settings.SettingsScreen
import com.twomax.live.ui.screens.splash.SplashScreen
import com.twomax.live.data.sync.RemotePlaylistSync

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    remotePlaylistSync: RemotePlaylistSync,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) { SplashScreen(navController, remotePlaylistSync) }
        composable(Screen.Activation.route) { ActivationScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Search.route) { SearchScreen(navController) }
        composable(Screen.LiveTv.route) { LiveTvScreen(navController) }
        composable(Screen.Movies.route) { MoviesScreen(navController) }
        composable(Screen.Series.route) { SeriesScreen(navController) }
        composable(
            Screen.SeriesDetail.route,
            arguments = listOf(navArgument("seriesId") { type = NavType.LongType })
        ) { backStackEntry ->
            SeriesDetailScreen(
                seriesId = backStackEntry.arguments?.getLong("seriesId") ?: 0L,
                navController = navController
            )
        }
        composable(Screen.Epg.route) { EpgScreen(navController) }
        composable(
            Screen.Player.route,
            arguments = listOf(
                navArgument("streamUrl") { type = NavType.StringType },
                navArgument("streamTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rawUrl = backStackEntry.arguments?.getString("streamUrl") ?: ""
            val rawTitle = backStackEntry.arguments?.getString("streamTitle") ?: ""
            PlayerScreen(
                streamUrl = try { String(android.util.Base64.decode(rawUrl, android.util.Base64.URL_SAFE)) } catch (e: Exception) { rawUrl },
                streamTitle = try { String(android.util.Base64.decode(rawTitle, android.util.Base64.URL_SAFE)) } catch (e: Exception) { rawTitle },
                navController = navController
            )
        }
        composable(
            Screen.MovieDetail.route,
            arguments = listOf(navArgument("movieId") { type = NavType.LongType })
        ) { backStackEntry ->
            MovieDetailScreen(
                movieId = backStackEntry.arguments?.getLong("movieId") ?: 0L,
                navController = navController
            )
        }
        composable(Screen.Setup.route) { SetupScreen(navController) }
        composable(Screen.Profiles.route) { ProfilesScreen(navController) }
        composable(Screen.Favorites.route) { FavoritesScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
    }
}
