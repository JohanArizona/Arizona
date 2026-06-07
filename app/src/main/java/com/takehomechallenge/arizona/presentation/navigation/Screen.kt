package com.takehomechallenge.arizona.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Profile : Screen("profile")
    data object EditProfile : Screen("edit_profile")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object Favorite : Screen("favorite")
    data object Detail : Screen("detail/{characterId}") {
        fun createRoute(characterId: Int) = "detail/$characterId"
    }
}
