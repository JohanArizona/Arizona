package com.takehomechallenge.arizona.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.takehomechallenge.arizona.presentation.component.common.CharacterCard
import com.takehomechallenge.arizona.presentation.navigation.Screen
import com.takehomechallenge.arizona.presentation.screen.auth.AuthViewModel
import com.takehomechallenge.arizona.presentation.theme.BackgroundDark
import com.takehomechallenge.arizona.presentation.theme.RickGreen
import com.takehomechallenge.arizona.presentation.theme.SurfaceDark
import com.takehomechallenge.arizona.presentation.theme.TextGray

@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    favoriteListState: LazyGridState
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    LaunchedEffect(authState.isAuthenticated) {
        if (!authState.isAuthenticated) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route)
            }
        }
    }

    if (uiState.user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RickGreen)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(16.dp)
        ) {
            Text(
                text = "My Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Info Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.user?.avatarUrl != null) {
                    AsyncImage(
                        model = uiState.user?.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(SurfaceDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = TextGray, modifier = Modifier.size(40.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.user?.fullName ?: "User",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "@${uiState.user?.username ?: "username"}",
                        fontSize = 14.sp,
                        color = TextGray
                    )
                }

                IconButton(
                    onClick = { navController.navigate(Screen.EditProfile.route) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceDark)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = RickGreen)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Settings / Menu
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceDark,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Default.Favorite,
                        label = "My Favorites",
                        onClick = { navController.navigate(Screen.Favorite.route) }
                    )
                    HorizontalDivider(color = BackgroundDark, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        label = "Logout",
                        labelColor = Color.Red,
                        onClick = { authViewModel.signOut() }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    labelColor: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (labelColor == Color.Red) Color.Red else RickGreen)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, color = labelColor, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
    }
}

private fun Modifier.fillWeight(weight: Float): Modifier = this.then(Modifier.fillMaxHeight(weight))
