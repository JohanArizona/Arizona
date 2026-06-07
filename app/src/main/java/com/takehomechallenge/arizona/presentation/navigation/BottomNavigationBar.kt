package com.takehomechallenge.arizona.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.takehomechallenge.arizona.presentation.theme.BackgroundDark
import com.takehomechallenge.arizona.presentation.theme.RickGreen
import com.takehomechallenge.arizona.presentation.theme.SurfaceDark
import com.takehomechallenge.arizona.presentation.theme.TextGray

@Composable
fun BottomNavigationBar(
    navController: NavController,
    onReselect: (Screen) -> Unit = {}
) {
    val items = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Profile
    )

    NavigationBar(
        containerColor = BackgroundDark,
        contentColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            val isSelected = currentRoute == screen.route
            val icon = when (screen) {
                Screen.Home -> Icons.Default.Home
                Screen.Search -> Icons.Default.Search
                Screen.Profile -> Icons.Default.Person
                else -> Icons.Default.Home
            }

            NavigationBarItem(
                icon = { Icon(icon, contentDescription = screen.route) },
                label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                selected = isSelected,
                onClick = {
                    if (isSelected) {
                        onReselect(screen)
                    } else {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RickGreen,
                    selectedTextColor = RickGreen,
                    indicatorColor = SurfaceDark,
                    unselectedIconColor = TextGray,
                    unselectedTextColor = TextGray
                )
            )
        }
    }
}