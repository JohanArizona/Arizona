package com.takehomechallenge.arizona.presentation.screen.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.takehomechallenge.arizona.presentation.screen.auth.AuthViewModel
import com.takehomechallenge.arizona.presentation.theme.BackgroundDark
import com.takehomechallenge.arizona.presentation.theme.RickGreen
import com.takehomechallenge.arizona.presentation.theme.SurfaceDark
import com.takehomechallenge.arizona.presentation.theme.TextGray
import androidx.compose.ui.text.font.FontWeight
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import android.content.Context
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    
    var fullName by rememberSaveable { mutableStateOf(authState.user?.fullName ?: "") }
    var username by rememberSaveable { mutableStateOf(authState.user?.username ?: "") }
    var avatarUrl by rememberSaveable { mutableStateOf(authState.user?.avatarUrl ?: "") }

    // Sync HANYA jika local state masih kosong (inisialisasi awal)
    LaunchedEffect(authState.user) {
        authState.user?.let { user ->
            if (fullName.isEmpty()) fullName = user.fullName ?: ""
            if (username.isEmpty()) username = user.username ?: ""
            if (avatarUrl.isEmpty()) avatarUrl = user.avatarUrl ?: ""
        }
    }

    var isSavingRequested by rememberSaveable { mutableStateOf(false) }
    var isUploadingImage by rememberSaveable { mutableStateOf(false) }
    var selectedImageUri: Uri? by rememberSaveable { mutableStateOf(null) }
    var showCropDialog by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    // Image picker launcher - opens gallery
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            showCropDialog = true
        }
    }

    LaunchedEffect(authState.isLoading, authState.error) {
        // Only pop back if user clicked Save and upload finished, not during image upload
        if (!authState.isLoading && authState.error == null && isSavingRequested && !isUploadingImage) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl.isNotEmpty()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextGray, modifier = Modifier.size(50.dp))
                }
                
                // Show loading indicator during image upload or profile save
                if ((authState.isLoading && !isSavingRequested) || isUploadingImage) {
                    CircularProgressIndicator(color = RickGreen)
                }
            }
            
            Text(text = "Tap to change photo", color = RickGreen, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            Text(text = "(You can crop after selecting)", color = TextGray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RickGreen,
                    unfocusedBorderColor = TextGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RickGreen,
                    unfocusedBorderColor = TextGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    isSavingRequested = true
                    authViewModel.updateProfile(username, fullName, avatarUrl.ifEmpty { null })
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = RickGreen),
                enabled = !authState.isLoading
            ) {
                if (authState.isLoading && isSavingRequested) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            authState.error?.let {
                Text(text = it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }

    // Crop dialog when image is selected
    if (showCropDialog && selectedImageUri != null) {
        CropImageDialog(
            imageUri = selectedImageUri!!,
            onCropConfirm = { croppedBytes ->
                showCropDialog = false
                isUploadingImage = true
                try {
                    // Use user ID or fallback timestamp
                    val userId = authState.user?.id ?: "temp_${System.currentTimeMillis()}"
                    val fileName = "avatar_${userId}_${System.currentTimeMillis()}.jpg"
                    authViewModel.uploadAvatar(croppedBytes, fileName) { newUrl ->
                        avatarUrl = newUrl
                        isUploadingImage = false
                        selectedImageUri = null
                    }
                } catch (e: Exception) {
                    isUploadingImage = false
                    selectedImageUri = null
                }
            },
            onCropCancel = {
                showCropDialog = false
                selectedImageUri = null
            },
            context = context
        )
    }
}

@Composable
fun CropImageDialog(
    imageUri: Uri,
    onCropConfirm: (ByteArray) -> Unit,
    onCropCancel: () -> Unit,
    context: Context
) {
    AlertDialog(
        onDismissRequest = onCropCancel,
        title = { Text("Crop Profile Photo", color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Show preview with square aspect ratio indicator
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                        .border(2.dp, RickGreen, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Image will be cropped to square 1:1",
                    color = TextGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Crop and compress the bitmap
                    try {
                        val inputStream = context.contentResolver.openInputStream(imageUri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        if (bitmap != null) {
                            // Crop to square
                            val croppedSize = minOf(bitmap.width, bitmap.height)
                            val x = (bitmap.width - croppedSize) / 2
                            val y = (bitmap.height - croppedSize) / 2
                            val croppedBitmap = Bitmap.createBitmap(bitmap, x, y, croppedSize, croppedSize)

                            // Compress to reasonable size
                            val outputStream = ByteArrayOutputStream()
                            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                            val bytes = outputStream.toByteArray()

                            onCropConfirm(bytes)
                            bitmap.recycle()
                            croppedBitmap.recycle()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RickGreen)
            ) {
                Text("Crop & Upload", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onCropCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = BackgroundDark,
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

