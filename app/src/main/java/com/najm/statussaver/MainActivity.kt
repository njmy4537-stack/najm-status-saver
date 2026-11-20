package com.najm.statussaver

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                NajmStatusSaverScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NajmStatusSaverScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("najm_status_prefs", Context.MODE_PRIVATE) }

    var savedUriString by remember { mutableStateOf(prefs.getString("tree_uri", null)) }
    var statusList by remember { mutableStateOf<List<DocumentFile>>(emptyList()) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val appBackgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF075E54),
            Color(0xFF0D2B27),
            Color(0xFF09141A)
        )
    )

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()
    }

    LaunchedEffect(savedUriString, refreshTrigger) {
        savedUriString?.let { uriStr ->
            statusList = StatusHelper.getStatusFiles(context, Uri.parse(uriStr))
        }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            prefs.edit().putString("tree_uri", uri.toString()).apply()
            savedUriString = uri.toString()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = appBackgroundGradient)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF075E54).copy(alpha = 0.85f),
                        titleContentColor = Color.White
                    ),
                    title = {
                        Column {
                            Text(
                                text = "حالات نجم الوجيه",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "تطوير وتصميم: نجم الوجيه",
                                fontSize = 12.sp,
                                color = Color(0xFFB2DFDB)
                            )
                        }
                    },
                    actions = {
                        if (savedUriString != null) {
                            IconButton(onClick = {
                                refreshTrigger++
                                Toast.makeText(context, "تم تحديث الحالات", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "تحديث",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "جميع الحقوق محفوظة © نجم الوجيه",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (savedUriString == null) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.12f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "أهلاً بك في تطبيق نجم الوجيه",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "اضغط على الزر بالأسفل واختر (استخدام هذا المجلد) لعرض وحفظ حالات واتساب.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    folderPickerLauncher.launch(StatusHelper.getStatusFolderIntent(isBusiness = false))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "السماح بالوصول للحالات",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else if (statusList.isEmpty()) {
                    Text(
                        text = "لا توجد حالات حالياً.\nشاهد بعض الحالات في واتساب ثم اضغط زر التحديث بالأعلى.",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(statusList) { file ->
                            NajmStatusCard(
                                file = file,
                                imageLoader = imageLoader,
                                onSaveClick = {
                                    val saved = StatusHelper.saveStatusToGallery(context, file)
                                    val msg = if (saved) "تم حفظ الحالة في معرض الصور" else "فشل حفظ الحالة"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NajmStatusCard(
    file: DocumentFile,
    imageLoader: ImageLoader,
    onSaveClick: () -> Unit
) {
    val isVideo = file.name?.lowercase()?.endsWith(".mp4") == true

    Box(
        modifier = Modifier
            .aspectRatio(0.78f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.4f))
    ) {
        AsyncImage(
            model = file.uri,
            imageLoader = imageLoader,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (isVideo) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "فيديو",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    .padding(8.dp)
            )
        }

        IconButton(
            onClick = onSaveClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .background(Color(0xFF25D366), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "حفظ",
                tint = Color.Black
            )
        }
    }
}
