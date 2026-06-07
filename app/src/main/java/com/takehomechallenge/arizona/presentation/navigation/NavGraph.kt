package com.takehomechallenge.arizona.presentation.navigation

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.takehomechallenge.arizona.presentation.screen.auth.LoginScreen
import com.takehomechallenge.arizona.presentation.screen.auth.SignUpScreen
import com.takehomechallenge.arizona.presentation.screen.detail.DetailScreen
import com.takehomechallenge.arizona.presentation.screen.favorite.FavoriteScreen
import com.takehomechallenge.arizona.presentation.screen.home.HomeScreen
import com.takehomechallenge.arizona.presentation.screen.profile.EditProfileScreen
import com.takehomechallenge.arizona.presentation.screen.profile.ProfileScreen
import com.takehomechallenge.arizona.presentation.screen.search.SearchScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    homeListState: LazyGridState,
    searchListState: LazyGridState,
    favoriteListState: LazyGridState
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                listState = homeListState
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                favoriteListState = favoriteListState
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }

        composable(Screen.Favorite.route) {
            FavoriteScreen(
                navController = navController,
                listState = favoriteListState
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }

        composable(Screen.Search.route) {
            SearchScreen(
                navController = navController,
                listState = searchListState
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("characterId") { type = NavType.IntType })
        ) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getInt("characterId") ?: 0
            DetailScreen(characterId = characterId, navController = navController)
        }
    }
}