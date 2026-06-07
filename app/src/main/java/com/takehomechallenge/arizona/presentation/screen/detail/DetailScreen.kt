package com.takehomechallenge.arizona.presentation.screen.detail

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.takehomechallenge.arizona.R
import com.takehomechallenge.arizona.domain.model.Comment
import com.takehomechallenge.arizona.presentation.component.common.CharacterCard
import com.takehomechallenge.arizona.presentation.navigation.Screen
import com.takehomechallenge.arizona.presentation.theme.*

@Composable
fun DetailScreen(
    characterId: Int,
    navController: NavController,
    viewModel: DetailViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val socialState by socialViewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()

    var commentText by rememberSaveable { mutableStateOf("") }
    var editingCommentId by rememberSaveable { mutableLongStateOf(-1L) }

    val showStickyBar by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 600
        }
    }

    val barColor by animateColorAsState(
        targetValue = if (showStickyBar) BackgroundDark else Color.Transparent,
        animationSpec = tween(300),
        label = "BarColor"
    )

    val iconButtonBackground by animateColorAsState(
        targetValue = if (showStickyBar) Color.Transparent else Color.Black.copy(alpha = 0.5f),
        animationSpec = tween(300),
        label = "IconBackground"
    )

    LaunchedEffect(characterId) {
        viewModel.getCharacterDetail(characterId)
        socialViewModel.loadSocialData(characterId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = RickGreen
            )
        } else if (state.character != null) {
            val character = state.character!!

            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {

                item {
                    Box(modifier = Modifier
                        .height(350.dp)
                        .fillMaxWidth()) {
                        AsyncImage(
                            model = character.image,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, BackgroundDark),
                                        startY = 400f
                                    )
                                )
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .offset(y = (-40).dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        GlassCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = character.name,
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        StatusBadgeLarge(status = character.status.value, species = character.species)
                                    }

                                    IconButton(
                                        onClick = { viewModel.toggleFavorite(character) },
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.6f))
                                    ) {
                                        Icon(
                                            imageVector = if (state.isFavorite)
                                                Icons.Default.Favorite
                                            else
                                                Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = RickGreen,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "BIO & APPEARANCE",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextGray
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        BioItem(
                                            icon = R.drawable.ic_gender,
                                            label = "Gender",
                                            value = character.gender.value
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Box(modifier = Modifier.weight(1f)) {
                                        BioItem(
                                            icon = R.drawable.ic_species,
                                            label = "Species",
                                            value = character.species
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Box(modifier = Modifier.weight(1f)) {
                                        BioItem(
                                            icon = R.drawable.ic_type,
                                            label = "Type",
                                            value = if(character.type.isEmpty()) "N/A" else character.type
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "LOCATIONS",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextGray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    LocationItem(
                                        modifier = Modifier.weight(1f),
                                        label = "Origin",
                                        value = character.originName,
                                        icon = R.drawable.ic_origin
                                    )
                                    LocationItem(
                                        modifier = Modifier.weight(1f),
                                        label = "Last Known Location",
                                        value = character.locationName,
                                        icon = R.drawable.ic_last_location
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "EPISODE APPEARANCES",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(character.episode) { episodeUrl ->
                                val episodeNum = episodeUrl.split("/").last()
                                EpisodeChip(episodeNum)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

                // Social Stats
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { socialViewModel.toggleLike(characterId) }) {
                            Icon(
                                imageVector = if (socialState.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Like",
                                tint = if (socialState.isLiked) RickGreen else Color.White
                            )
                        }
                        Text(
                            text = "${socialState.likesCount} Likes",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Comments Section
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "COMMENTS",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Add/Edit Comment
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { Text(if (editingCommentId == -1L) "Add a comment..." else "Edit comment...", color = TextGray) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RickGreen,
                                    unfocusedBorderColor = SurfaceDark,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(24.dp),
                                trailingIcon = {
                                    if (editingCommentId != -1L) {
                                        IconButton(onClick = { 
                                            editingCommentId = -1L
                                            commentText = ""
                                        }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Cancel", tint = Color.Red)
                                        }
                                    }
                                }
                            )
                            IconButton(onClick = {
                                if (commentText.isNotBlank()) {
                                    if (editingCommentId == -1L) {
                                        socialViewModel.addComment(characterId, commentText)
                                    } else {
                                        socialViewModel.updateComment(editingCommentId, commentText, characterId)
                                        editingCommentId = -1L
                                    }
                                    commentText = ""
                                }
                            }) {
                                Icon(
                                    imageVector = if (editingCommentId == -1L) Icons.AutoMirrored.Filled.Send else Icons.Default.Check, 
                                    contentDescription = "Send", 
                                    tint = RickGreen
                                )
                            }
                        }

                        socialState.error?.let {
                            Text(
                                text = it,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Comments List
                        socialState.comments.forEach { comment ->
                            CommentItem(
                                comment = comment,
                                isOwnComment = comment.userId == socialState.currentUserId,
                                onDelete = { socialViewModel.deleteComment(comment.id, characterId) },
                                onEdit = {
                                    editingCommentId = comment.id
                                    commentText = comment.content
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

                item {
                    Column {
                        Text(
                            text = "YOU MIGHT ALSO LIKE",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextGray,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.recommendations) { recChar ->
                                Box(modifier = Modifier.width(160.dp)) {
                                    CharacterCard(
                                        character = recChar,
                                        onClick = { navController.navigate(Screen.Detail.createRoute(recChar.id)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(barColor)
                    .align(Alignment.TopStart)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(iconButtonBackground)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    AnimatedVisibility(
                        visible = showStickyBar,
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        Text(
                            text = character.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(start = 16.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    isOwnComment: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        if (comment.userProfile?.avatarUrl != null) {
            AsyncImage(
                model = comment.userProfile.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.userProfile?.username ?: "Anonymous",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isOwnComment) RickGreen else Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (isOwnComment) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(RickGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("You", color = RickGreen, fontSize = 10.sp)
                    }
                }
            }
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = comment.createdAt.split("T").firstOrNull() ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = TextGray
            )
        }

        if (isOwnComment) {
            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextGray)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(SurfaceDark)
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit", color = Color.White) },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}

@Composable
fun GlassCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark.copy(alpha = 0.8f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
    ) {
        content()
    }
}

@Composable
fun StatusBadgeLarge(status: String, species: String) {
    val color = when (status.lowercase()) {
        "alive" -> StatusGreen
        "dead" -> StatusRed
        else -> StatusGray
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$status | $species",
            color = color,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun BioItem(icon: Int, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = Color(0xFFC5C7D7),
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                color = TextGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LocationItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: Int
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = RickGreen,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, color = TextGray, fontSize = 12.sp)
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EpisodeChip(episodeNum: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2C2C2E))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Episode $episodeNum", color = Color.White, fontSize = 14.sp)
        }
    }
}
