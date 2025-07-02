package com.bedjamahdi.scanpestai.screen.pest

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bedjamahdi.scanpestai.R
import com.bedjamahdi.scanpestai.screen.user.LanguagePref
import com.bedjamahdi.scanpestai.ui.theme.CustomTextStyles
import org.json.JSONArray
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PestDetailScreen(
    navController: NavController,
    pestName: String
) {
    val context = LocalContext.current
    val currentLanguage = LanguagePref.getLanguage(context)

    var pestInfo by remember { mutableStateOf<PestInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load pest data
    LaunchedEffect(pestName, currentLanguage) {
        pestInfo = loadSpecificPestData(context, pestName, currentLanguage)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.pest_details),
                            style = CustomTextStyles.sectionHeader.copy(
                                color = MaterialTheme.colorScheme.onPrimary
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    // Invisible spacer for proper centering
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                pestInfo?.let { pest ->
                    PestDetailContent(pest = pest, currentLanguage = currentLanguage)
                } ?: run {
                    // Error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.pest_not_found),
                            style = CustomTextStyles.sectionHeader,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PestDetailContent(pest: PestInfo, currentLanguage: String) {
    val pestImageRes = getPestImageResource(pest.pest)

    val pestNameTranslationsAr = mapOf(
        "Grub" to "اليرقة البيضاء",
        "Mole Cricket" to "صرصور الحقل",
        "Wireworm" to "الدودة السلكية",
        "Corn Borer" to "ثاقبة الذرة",
        "Aphids" to "المنّ",
        "Beet Armyworm" to "دودة الجيش",
        "Flax Budworm" to "يرقة براعم الكتان",
        "Lytta Polita" to "ذبابة الزيتون",
        "Legume Blister beetle" to "خنفساء التقرح البقولية",
        "Blister Beetle" to "خنفساء التقرح",
        "Miridae" to "حشرات الميري",
        "Prodenia Litura" to "يرقة الحشد",
        "Cicadellidae" to "النطاطات"
    )

    val pestNameTranslationsFr = mapOf(
        "Grub" to "Vers blanc",
        "Mole Cricket" to "Grillon des champs",
        "Wireworm" to "Ver fil de fer",
        "Corn Borer" to "Foreur du maïs",
        "Aphids" to "Pucerons",
        "Beet Armyworm" to "Chenille de la betterave",
        "Flax Budworm" to "Chenille du lin",
        "Lytta Polita" to "Mouche de l'olive",
        "Legume Blister beetle" to "Cantharide des légumineuses",
        "Blister Beetle" to "Cantharide",
        "Miridae" to "Mirides",
        "Prodenia Litura" to "Chenille défoliatrice",
        "Cicadellidae" to "Cicadelles"
    )

    val displayName = when (currentLanguage) {
        "ar" -> pestNameTranslationsAr[pest.pest] ?: pest.pest
        "fr" -> pestNameTranslationsFr[pest.pest] ?: pest.pest
        else -> pest.pest
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Image Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = pestImageRes),
                    contentDescription = displayName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                )

                // Pest name overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = displayName,
                        style = CustomTextStyles.welcomeText.copy(
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Pest Information Cards
        PestInfoCard(
            title = stringResource(R.string.crop_category),
            content = pest.category,
            gradientColors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            )
        )

        PestInfoCard(
            title = stringResource(R.string.treatment_recommendation),
            content = pest.recommendation,
            gradientColors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            )
        )
    }
}

@Composable
fun PestInfoCard(
    title: String,
    content: String,
    gradientColors: List<Color>,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = CustomTextStyles.cardTitle.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                )

                Divider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )

                Text(
                    text = content,
                    style = CustomTextStyles.noteContent.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                )
            }
        }
    }
}


fun loadSpecificPestData(context: Context, pestName: String, language: String): PestInfo? {
    return try {
        val fileName = when (language) {
            "ar" -> "pests_ar.json"
            "fr" -> "pests_fr.json"
            else -> "pestInfo.json"
        }

        val inputStream = context.assets.open(fileName)
        val json = inputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(json)

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val pest = jsonObject.getString("pest")
            if (pest == pestName) {
                return PestInfo(
                    pest = pest,
                    category = jsonObject.getString("category"),
                    recommendation = jsonObject.getString("recommendation")
                )
            }
        }
        null
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}