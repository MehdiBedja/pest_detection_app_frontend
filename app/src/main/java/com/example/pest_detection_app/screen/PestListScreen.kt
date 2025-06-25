package com.example.pest_detection_app.screen.pest

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pest_detection_app.R
import com.example.pest_detection_app.screen.user.LanguagePref
import com.example.pest_detection_app.ui.theme.CustomTextStyles
import org.json.JSONArray
import java.io.IOException

data class PestInfo(
    val pest: String,
    val category: String,
    val recommendation: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PestListScreen(navController: NavController) {
    val context = LocalContext.current
    val currentLanguage = LanguagePref.getLanguage(context)

    var pestList by remember { mutableStateOf<List<PestInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load pest data based on current language
    LaunchedEffect(currentLanguage) {
        pestList = loadPestData(context, currentLanguage)
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
                            text = stringResource(R.string.detectable_pests),
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                    ,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Pest cards
                    items(pestList) { pest ->
                        PestCard(
                            pest = pest,
                            currentLanguage = currentLanguage,
                            onClick = {
                                navController.navigate("pest_detail/${pest.pest}")
                            }
                        )
                    }

                    // Footer spacer
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}



@Composable
fun PestCard(
    pest: PestInfo,
    currentLanguage: String,
    onClick: () -> Unit
) {
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

    // Get pest image resource
    val pestImageRes = getPestImageResource(pest.pest)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pest image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Image(
                    painter = painterResource(id = pestImageRes),
                    contentDescription = displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Pest information
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayName,
                    style = CustomTextStyles.cardTitle.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.category_label) + ": " + pest.category,
                    style = CustomTextStyles.detectionSubtitle.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // View details button
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.view_details),
                        style = CustomTextStyles.buttonText.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    )

                    Spacer(modifier = Modifier.width(4.dp))


                }
            }
        }
    }
}

fun loadPestData(context: Context, language: String): List<PestInfo> {
    return try {
        val fileName = when (language) {
            "ar" -> "pests_ar.json"
            "fr" -> "pests_fr.json"
            else -> "pestInfo.json"
        }

        val inputStream = context.assets.open(fileName)
        val json = inputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(json)

        val pestList = mutableListOf<PestInfo>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            pestList.add(
                PestInfo(
                    pest = jsonObject.getString("pest"),
                    category = jsonObject.getString("category"),
                    recommendation = jsonObject.getString("recommendation")
                )
            )
        }
        pestList
    } catch (e: IOException) {
        e.printStackTrace()
        emptyList()
    }
}

fun getPestImageResource(pestName: String): Int {
    return when (pestName) {
        "Grub" -> R.drawable.grub
        "Mole Cricket" -> R.drawable.mole_cricket
        "Wireworm" -> R.drawable.wireworm
        "Corn Borer" -> R.drawable.corn_borer
        "Aphids" -> R.drawable.aphids
        "Beet Armyworm" -> R.drawable.beet_armyworm
        "Flax Budworm" -> R.drawable.flax_budworm
        "Lytta Polita" -> R.drawable.lytta_polita
        "Legume Blister beetle" -> R.drawable.legume_blister_beetle
        "Blister Beetle" -> R.drawable.blister_beetle
        "Miridae" -> R.drawable.miridae
        "Prodenia Litura" -> R.drawable.prodenia_litura
        "Cicadellidae" -> R.drawable.cicadellidae
        else -> R.drawable.visible // Add a default pest image
    }
}