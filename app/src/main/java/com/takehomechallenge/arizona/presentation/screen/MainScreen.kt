package com.takehomechallenge.arizona.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.takehomechallenge.arizona.presentation.navigation.BottomNavigationBar
import com.takehomechallenge.arizona.presentation.navigation.NavGraph
import com.takehomechallenge.arizona.presentation.navigation.Screen
import com.takehomechallenge.arizona.presentation.screen.auth.AuthViewModel
import com.takehomechallenge.arizona.presentation.theme.BackgroundDark
import com.takehomechallenge.arizona.presentation.theme.RickGreen
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val scope = rememberCoroutineScope()

    // Logika Navigasi: Hanya pindah halaman kalau status login berubah secara explisit
    // PENTING: Exclude EditProfile screen dari automatic logout check, karena user perlu upload avatar tanpa diputus
    LaunchedEffect(authState.isAuthenticated, authState.isChecking, currentRoute) {
        if (!authState.isChecking && !authState.isAuthenticated) {
            // Jangan redirect dari screens yang authenticated users boleh akses
            val protectedRoutes = setOf(
                Screen.Login.route,
                Screen.SignUp.route,
                Screen.EditProfile.route  // Allow users to stay on EditProfile while uploading
            )
            if (currentRoute !in protectedRoutes) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val homeListState = rememberLazyGridState()
    val searchListState = rememberLazyGridState()
    val favoriteListState = rememberLazyGridState()

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Profile.route
    )

    // JANGAN PERNAH HAPUS SCAFFOLD/NAVGRAPH DARI COMPOSITION!
    // Pake Box buat nampilin Loading di ATAS NavGraph biar NavGraph gak reset.
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomBar && !authState.isChecking) {
                    BottomNavigationBar(
                        navController = navController,
                        onReselect = { screen ->
                            scope.launch {
                                when (screen) {
                                    Screen.Home -> homeListState.animateScrollToItem(0)
                                    Screen.Search -> searchListState.animateScrollToItem(0)
                                    Screen.Profile -> favoriteListState.animateScrollToItem(0)
                                    else -> {}
                                }
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(paddingValues),
                homeListState = homeListState,
                searchListState = searchListState,
                favoriteListState = favoriteListState
            )
        }

        // Tampilkan loading screen sebagai overlay jika masih mengecek auth
        if (authState.isChecking) {
            Box(
                modifier = Modifier.fillMaxSize().background(BackgroundDark),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RickGreen)
            }
        }
    }
}
