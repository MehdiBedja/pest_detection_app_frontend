package com.bedjamahdi.scanpestai.screen.pest

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
import com.bedjamahdi.scanpestai.R
import com.bedjamahdi.scanpestai.screen.user.LanguagePref
import com.bedjamahdi.scanpestai.ui.theme.CustomTextStyles
import org.json.JSONArray
import java.io.IOException

data class PestInfo(
    val pest: String,
    val category: String,
    val recommendation: String,
    val source : String
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
    val pestNameTranslationsAr1 = mapOf(
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

    val pestNameTranslationsFr1 = mapOf(
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





    val pestNameTranslationsFr = mapOf(
        "rice leaf roller" to "Enrouleuse des feuilles du riz",
        "rice leaf caterpillar" to "Chenille des feuilles du riz",
        "paddy stem maggot" to "Mouche mineuse de la tige du riz",
        "asiatic rice borer" to "Foreur asiatique du riz",
        "yellow rice borer" to "Foreur jaune du riz",
        "rice gall midge" to "Cécidomyie du riz",
        "Rice Stem fly" to "Mouche de la tige du riz",
        "brown plant hopper" to "Sauteriau brun du riz",
        "white backed plant hopper" to "Sauteriau à dos blanc",
        "small brown plant hopper" to "Petit sauteriau brun",
        "rice water weevil" to "Charançon aquatique du riz",
        "rice leaf hopper" to "Cicadelle du riz",
        "grain spreader thrips" to "Thrips des grains",
        "rice shell pest" to "Parasite de l’enveloppe du riz",
        "grub" to "Vers blanc",
        "mole cricket" to "Grillon-taupe",
        "wireworm" to "Ver fil de fer",
        "white margined moth" to "Noctuelle à marge blanche",
        "black cutworm" to "Noctuelle noire",
        "large cutworm" to "Grande noctuelle coupure",
        "yellow cutworm" to "Noctuelle jaune",
        "red spider" to "Araignée rouge",
        "corn borer" to "Foreur du maïs",
        "army worm" to "Chenille militaire",
        "aphids" to "Pucerons",
        "Potosiabre vitarsis" to "Potosiabre vitarsis",
        "peach borer" to "Foreur du pêcher",
        "english grain aphid" to "Puceron des céréales anglais",
        "green bug" to "Puceron vert",
        "bird cherry-oat aphid" to "Puceron du merisier et de l’avoine",
        "wheat blossom midge" to "Cécidomyie du blé",
        "penthaleus major" to "Penthaleus major",
        "long legged spider mite" to "Acarien rouge à longues pattes",
        "wheat phloeo thrips" to "Thrips du blé",
        "wheat sawfly" to "Tenthrède du blé",
        "cerodonta denticornis" to "Cerodonta denticornis",
        "beet fly" to "Mouche de la betterave",
        "flea beetle" to "Altise",
        "cabbage army worm" to "Chenille de la chou",
        "beet army worm" to "Chenille de la betterave",
        "Beet spot flies" to "Mouches de la tache de la betterave",
        "meadow moth" to "Noctuelle des prés",
        "beet weevil" to "Charançon de la betterave",
        "sericaorient alismots chulsky" to "Sericaorient alismots chulsky",
        "alfalfa weevil" to "Charançon de la luzerne",
        "flax budworm" to "Tordeuse du lin",
        "alfalfa plant bug" to "Punaise de la luzerne",
        "tarnished plant bug" to "Punaise terne",
        "Locustoidea" to "Locustoidea",
        "lytta polita" to "Lytta polita",
        "legume blister beetle" to "Cantharide des légumineuses",
        "blister beetle" to "Cantharide",
        "odontothrips loti" to "Odontothrips loti",
        "Thrips" to "Thrips",
        "alfalfa seed chalcid" to "Chalcide de la graine de luzerne",
        "Pieris canidia" to "Pieris canidia",
        "Apolygus lucorum" to "Apolygus lucorum",
        "Limacodidae" to "Limacodidae",
        "Brevipalpus lewisi McGregor" to "Brevipalpus lewisi McGregor",
        "oides decempunctata" to "Oides decempunctata",
        "Pseudococcus comstocki Kuwana" to "Pseudococcus comstocki Kuwana",
        "parathrene regalis" to "Parathrene regalis",
        "Ampelophaga" to "Ampelophaga",
        "Lycorma delicatula" to "Lycorma delicatula (Lycorme délicate)",
        "Xylotrechus" to "Xylotrechus",
        "Cicadella viridis" to "Cicadelle verte (Cicadella viridis)",
        "Miridae" to "Mirides",
        "Trialeurodes vaporariorum" to "Aleurode des serres",
        "Papilio xuthus" to "Papilio xuthus (Papillon xuthus)",
        "Panonchus citri McGregor" to "Panonchus citri McGregor (Acarien des agrumes)",
        "Phyllocoptes oleiverus ashmead" to "Phyllocoptes oleiverus ashmead",
        "Icerya purchasi Maskell" to "Icerya purchasi Maskell (Cochénille australienne)",
        "Unaspis yanonensis" to "Unaspis yanonensis",
        "Ceroplastes rubens" to "Ceroplastes rubens",
        "Chrysomphalus aonidum" to "Chrysomphalus aonidum",
        "Nipaecoccus vastalor" to "Nipaecoccus vastalor",
        "Aleurocanthus spiniferus" to "Aleurocanthus spiniferus (Aleurode épineux)",
        "Tetradacus c Bactrocera minax" to "Bactrocera minax",
        "Dacus dorsalis(Hendel)" to "Dacus dorsalis (Hendel)",
        "Bactrocera tsuneonis" to "Bactrocera tsuneonis",
        "Prodenia litura" to "Chenille défoliatrice (Prodenia litura)",
        "Adristyrannus" to "Adristyrannus",
        "Phyllocnistis citrella Stainton" to "Mineuse des agrumes (Phyllocnistis citrella)",
        "Toxoptera aurantii" to "Puceron noir des agrumes",
        "Aphis citricola Vander Goot" to "Puceron vert des agrumes",
        "Scirtothrips dorsalis Hood" to "Thrips des fruits (Scirtothrips dorsalis)",
        "Lawana imitata Melichar" to "Lawana imitata Melichar",
        "Salurnis marginella Guerr" to "Salurnis marginella Guerr",
        "Deporaus marginatus Pascoe" to "Deporaus marginatus Pascoe",
        "Chlumetia transversa" to "Chlumetia transversa",
        "Rhytidodera bowrinii white" to "Rhytidodera bowrinii White",
        "Sternochetus frigidus" to "Sternochetus frigidus",
        "Cicadellidae" to "Cicadelles"
    )

    val pestNameTranslationsAr = mapOf(
        "rice leaf roller" to "حفّار أوراق الأرز",
        "rice leaf caterpillar" to "يرقة أوراق الأرز",
        "paddy stem maggot" to "ذُبابة ساق الأرز",
        "asiatic rice borer" to "حفّار الأرز الآسيوي",
        "yellow rice borer" to "حفّار الأرز الأصفر",
        "rice gall midge" to "بعوضة أورام الأرز",
        "Rice Stem fly" to "ذُبابة ساق الأرز",
        "brown plant hopper" to "نطاط النباتات البني",
        "white backed plant hopper" to "نطاط النباتات أبيض الظهر",
        "small brown plant hopper" to "نطاط النباتات البني الصغير",
        "rice water weevil" to "سوسة ماء الأرز",
        "rice leaf hopper" to "نطاط أوراق الأرز",
        "grain spreader thrips" to "تربس مبعثر الحبوب",
        "rice shell pest" to "آفة قشرة الأرز",
        "grub" to "يرقة الحشرة (الدودة البيضاء)",
        "mole cricket" to "صرصار الخُلد",
        "wireworm" to "الدودة السلكية",
        "white margined moth" to "عُثة بيضاء الحواف",
        "black cutworm" to "دودة قطع سوداء",
        "large cutworm" to "دودة قطع كبيرة",
        "yellow cutworm" to "دودة قطع صفراء",
        "red spider" to "العنكبوت الأحمر",
        "corn borer" to "حفّار الذرة",
        "army worm" to "دودة الحشد",
        "aphids" to "المنّ",
        "Potosiabre vitarsis" to "خنفساء بيتوسيابري",
        "peach borer" to "حفّار الخوخ",
        "english grain aphid" to "منّ الحبوب الإنجليزي",
        "green bug" to "بقّة خضراء",
        "bird cherry-oat aphid" to "منّ الكرز الطائر-الشوفان",
        "wheat blossom midge" to "بعوضة أزهار القمح",
        "penthaleus major" to "عنكبوت بنتاليوس الكبير",
        "long legged spider mite" to "عنكبوت طويل الأرجل",
        "wheat phloeo thrips" to "تربس القمح",
        "wheat sawfly" to "ذُبابة منشارية القمح",
        "cerodonta denticornis" to "سيرودونتا دنتيكورنس",
        "beet fly" to "ذبابة البنجر",
        "flea beetle" to "خنفساء البرغوث",
        "cabbage army worm" to "دودة الحشد للكرنب",
        "beet army worm" to "دودة الحشد للبنجر",
        "Beet spot flies" to "ذباب بقع البنجر",
        "meadow moth" to "عُثة المروج",
        "beet weevil" to "سوسة البنجر",
        "sericaorient alismots chulsky" to "سيريكا أورينتاليس موتس شولسكي",
        "alfalfa weevil" to "سوسة البرسيم",
        "flax budworm" to "دودة براعم الكتان",
        "alfalfa plant bug" to "بقّة البرسيم",
        "tarnished plant bug" to "بقّة النبات الملطخة",
        "Locustoidea" to "الجراديات (لوكوستويديا)",
        "lytta polita" to "خنفساء ليطا بوليتا",
        "legume blister beetle" to "خنفساء فقاعية للبقوليات",
        "blister beetle" to "الخنفساء الفقاعية",
        "odontothrips loti" to "أودونتوتربس لوطي",
        "Thrips" to "التربس",
        "alfalfa seed chalcid" to "حشرة البرسيم البذورية",
        "Pieris canidia" to "فراشة بيريس كانيديا",
        "Apolygus lucorum" to "بقّة أبوليغوس لوكوروم",
        "Limacodidae" to "عائلة ليمكوديداي (عُث الحلزون)",
        "Brevipalpus lewisi McGregor" to "بريفالبوس لويسي",
        "oides decempunctata" to "أويدس ديكيمبونكتاتا",
        "Pseudococcus comstocki Kuwana" to "بسيودوكوكوس كومستوكاي (بق الدقيق)",
        "parathrene regalis" to "باراثرين ريجاليس",
        "Ampelophaga" to "أمبلوفاجا",
        "Lycorma delicatula" to "ليكورما ديليكولاتا",
        "Xylotrechus" to "زيلوتريكوس",
        "Cicadella viridis" to "سيكاديلا فيريديس (نطاط أخضر)",
        "Miridae" to "الميريدات (بقّات نباتية)",
        "Trialeurodes vaporariorum" to "الذُبابة البيضاء للبيوت المحمية",
        "Papilio xuthus" to "فراشة بابيليو زوثوس",
        "Panonchus citri McGregor" to "بانونكوس الحمضيات",
        "Phyllocoptes oleiverus ashmead" to "فيلوكوبتس الزيتون",
        "Icerya purchasi Maskell" to "البق القشري القطني",
        "Unaspis yanonensis" to "أوناسبيس يانونينسيس",
        "Ceroplastes rubens" to "سيروبلاستيس روبنس (حشرة قشرية شمعية)",
        "Chrysomphalus aonidum" to "كريزومفالوس أونيدوم",
        "Nipaecoccus vastalor" to "نيبايكوكوس فاستالور",
        "Aleurocanthus spiniferus" to "أليوروكانثوس سبينيفيروس",
        "Tetradacus c Bactrocera minax" to "تتراداكوس / باكتروسيرا مينكس",
        "Dacus dorsalis(Hendel)" to "داكوس دورساليس",
        "Bactrocera tsuneonis" to "باكتروسيرا تسونيونيس",
        "Prodenia litura" to "برودينيا ليتورا",
        "Adristyrannus" to "أدريستيرانوس",
        "Phyllocnistis citrella Stainton" to "فيلوكنيستيس سيتريلا (حفّار أوراق الحمضيات)",
        "Toxoptera aurantii" to "توكزوبتيرا أورانتي (منّ الحمضيات)",
        "Aphis citricola Vander Goot" to "أفيس سيتريكولا (منّ الحمضيات)",
        "Scirtothrips dorsalis Hood" to "سكيرتو تربس دورساليس",
        "Lawana imitata Melichar" to "لاوانا إيميتاتا",
        "Salurnis marginella Guerr" to "سالورنس مارجينيلا",
        "Deporaus marginatus Pascoe" to "ديبوراوس مارجيناتوس",
        "Chlumetia transversa" to "خلوميتيا ترانزفيرسا",
        "Rhytidodera bowrinii white" to "ريثيدوديرا بواريني",
        "Sternochetus frigidus" to "ستيرنوكيتوس فريجيدوس",
        "Cicadellidae" to "السيكاديلداي (نطاطات الأوراق)"
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
            "ar" -> "pests_ar3.json"
            "fr" -> "pests_fr3.json"
            else -> "pestInfo3.json"
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
                    recommendation = jsonObject.getString("recommendation"),
                    source = jsonObject.getString("source")
                )
            )
        }
        pestList
    } catch (e: IOException) {
        e.printStackTrace()
        emptyList()
    }
}
/*
fun getPestImageResource1(pestName: String): Int {
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


 */
/*
fun getPestImageResource(pestName: String): Int {
    return when (pestName) {
        "rice leaf roller" -> R.drawable.IP000
        "rice leaf caterpillar" -> R.drawable.IP001000079
        "paddy stem maggot" -> R.drawable.IP002000022
        "asiatic rice borer" -> R.drawable.IP003000183
        "yellow rice borer" -> R.drawable.IP004000085
        "rice gall midge" -> R.drawable.IP005000047
        "Rice Stem fly" -> R.drawable.IP006000228
        "brown plant hopper" -> R.drawable.IP007000629
        "white backed plant hopper" -> R.drawable.IP008000892
        "small brown plant hopper" -> R.drawable.IP009000454
        "rice water weevil" -> R.drawable.IP010000546
        "rice leaf hopper" -> R.drawable.IP011000255
        "grain spreader thrips" -> R.drawable.IP012000167
        "rice shell pest" -> R.drawable.IP013000282
        "grub" -> R.drawable.IP014000007
        "mole cricket" -> R.drawable.IP015000016
        "wireworm" -> R.drawable.IP016000171
        "white margined moth" -> R.drawable.IP017000073
        "black cutworm" -> R.drawable.IP018000253
        "large cutworm" -> R.drawable.IP019000106
        "yellow cutworm" -> R.drawable.IP020000104
        "red spider" -> R.drawable.IP021000095
        "corn borer" -> R.drawable.IP022000270
        "army worm" -> R.drawable.IP023000707
        "aphids" -> R.drawable.IP024003186
        "Potosiabre vitarsis" -> R.drawable.IP025000245
        "peach borer" -> R.drawable.IP026000687
        "english grain aphid" -> R.drawable.IP027000447
        "green bug" -> R.drawable.IP028000204
        "bird cherry-oat aphid" -> R.drawable.IP029000084
        "wheat blossom midge" -> R.drawable.IP030000242
        "penthaleus major" -> R.drawable.IP032000024
        "long legged spider mite" -> R.drawable.IP033000156
        "wheat phloeo thrips" -> R.drawable.IP034000118
        "wheat sawfly" -> R.drawable.IP035000214
        "cerodonta denticornis" -> R.drawable.IP036000064
        "beet fly" -> R.drawable.IP037000082
        "flea beetle" -> R.drawable.IP038000131
        "cabbage army worm" -> R.drawable.IP039000460
        "beet army worm" -> R.drawable.IP040000554
        "Beet spot flies" -> R.drawable.IP041000214
        "meadow moth" -> R.drawable.IP042000163
        "beet weevil" -> R.drawable.IP043000201
        "sericaorient alismots chulsky" -> R.drawable.IP044000118
        "alfalfa weevil" -> R.drawable.IP045000515
        "flax budworm" -> R.drawable.IP046000586
        "alfalfa plant bug" -> R.drawable.IP047000149
        "tarnished plant bug" -> R.drawable.IP048000202
        "Locustoidea" -> R.drawable.IP049001230
        "lytta polita" -> R.drawable.IP050000306
        "legume blister beetle" -> R.drawable.IP051000392
        "blister beetle" -> R.drawable.IP052000193
        "therioaphis maculata Buckton" -> R.drawable.test
        "odontothrips loti" -> R.drawable.IP054000077
        "Thrips" -> R.drawable.IP055000083
        "alfalfa seed chalcid" -> R.drawable.IP056000057
        "Pieris canidia" -> R.drawable.IP057000256
        "Apolygus lucorum" -> R.drawable.IP058000266
        "Limacodidae" -> R.drawable.IP059000155
        "Viteus vitifoliae" -> R.drawable.test
        "Colomerus vitis" -> R.drawable.test
        "Brevipalpus lewisi McGregor" -> R.drawable.test
        "oides decempunctata" -> R.drawable.IP063000089
        "Polyphagotarsonemus latus" -> R.drawable.test
        "Pseudococcus comstocki Kuwana" -> R.drawable.IP065000027
        "parathrene regalis" -> R.drawable.IP066000121
        "Ampelophaga" -> R.drawable.IP067000519
        "Lycorma delicatula" -> R.drawable.IP068005049
        "Xylotrechus" -> R.drawable.IP069000863
        "Cicadella viridis" -> R.drawable.IP070000925
        "Miridae" -> R.drawable.IP071001610
        "Trialeurodes vaporariorum" -> R.drawable.IP072000190
        "Erythroneura apicalis" -> R.drawable.test
        "Papilio xuthus" -> R.drawable.IP074000432
        "Panonchus citri McGregor" -> R.drawable.IP075000203
        "Phyllocoptes oleiverus ashmead" -> R.drawable.test
        "Icerya purchasi Maskell" -> R.drawable.IP077000524
        "Unaspis yanonensis" -> R.drawable.IP078000050
        "Ceroplastes rubens" -> R.drawable.IP079000134
        "Chrysomphalus aonidum" -> R.drawable.IP080000041
        "Parlatoria zizyphus Lucus" -> R.drawable.test
        "Nipaecoccus vastalor" -> R.drawable.IP082000089
        "Aleurocanthus spiniferus" -> R.drawable.IP083000397
        "Tetradacus c Bactrocera minax" -> R.drawable.IP084000101
        "Dacus dorsalis(Hendel)" -> R.drawable.IP085000222
        "Bactrocera tsuneonis" -> R.drawable.IP086000127
        "Prodenia litura" -> R.drawable.IP087000225
        "Adristyrannus" -> R.drawable.IP088000235
        "Phyllocnistis citrella Stainton" -> R.drawable.IP089000240
        "Toxoptera citricidus" -> R.drawable.test
        "Toxoptera aurantii" -> R.drawable.IP091000178
        "Aphis citricola Vander Goot" -> R.drawable.test
        "Scirtothrips dorsalis Hood" -> R.drawable.IP093000520
        "Dasineura sp" -> R.drawable.test
        "Lawana imitata Melichar" -> R.drawable.IP095000505
        "Salurnis marginella Guerr" -> R.drawable.IP096000187
        "Deporaus marginatus Pascoe" -> R.drawable.IP097000106
        "Chlumetia transversa" -> R.drawable.IP098000118
        "Mango flat beak leafhopper" -> R.drawable.test
        "Rhytidodera bowrinii white" -> R.drawable.IP100000448
        "Sternochetus frigidus" -> R.drawable.IP101000286
        "Cicadellidae" -> R.drawable.IP102001586

        else -> R.drawable.visible // Add a default pest image
    }





}

 */


fun getPestImageResource(pestName: String): Int {
    return when (pestName) {
        "rice leaf roller" -> R.drawable.ip000
        "rice leaf caterpillar" -> R.drawable.ip001000079
        "paddy stem maggot" -> R.drawable.ip002000022
        "asiatic rice borer" -> R.drawable.ip003000183
        "yellow rice borer" -> R.drawable.ip004000085
        "rice gall midge" -> R.drawable.ip005000047
        "Rice Stem fly" -> R.drawable.ip006000228
        "brown plant hopper" -> R.drawable.ip007000629
        "white backed plant hopper" -> R.drawable.ip008000892
        "small brown plant hopper" -> R.drawable.ip009000454
        "rice water weevil" -> R.drawable.ip010000546
        "rice leaf hopper" -> R.drawable.ip011000255
        "grain spreader thrips" -> R.drawable.ip012000167
        "rice shell pest" -> R.drawable.ip013000282
        "grub" -> R.drawable.ip014000007
        "mole cricket" -> R.drawable.ip015000016
        "wireworm" -> R.drawable.ip016000171
        "white margined moth" -> R.drawable.ip017000073
        "black cutworm" -> R.drawable.ip018000253
        "large cutworm" -> R.drawable.ip019000106
        "yellow cutworm" -> R.drawable.ip020000104
        "red spider" -> R.drawable.ip021000095
        "corn borer" -> R.drawable.ip022000270
        "army worm" -> R.drawable.ip023000707
        "aphids" -> R.drawable.ip024003186
        "Potosiabre vitarsis" -> R.drawable.ip025000245
        "peach borer" -> R.drawable.ip026000687
        "english grain aphid" -> R.drawable.ip027000447
        "green bug" -> R.drawable.ip028000204
        "bird cherry-oat aphid" -> R.drawable.ip029000084
        "wheat blossom midge" -> R.drawable.ip030000242
        "penthaleus major" -> R.drawable.ip032000024
        "long legged spider mite" -> R.drawable.ip033000156
        "wheat phloeo thrips" -> R.drawable.ip034000118
        "wheat sawfly" -> R.drawable.ip035000214
        "cerodonta denticornis" -> R.drawable.ip036000064
        "beet fly" -> R.drawable.ip037000082
        "flea beetle" -> R.drawable.ip038000131
        "cabbage army worm" -> R.drawable.ip039000460
        "beet army worm" -> R.drawable.ip040000554
        "Beet spot flies" -> R.drawable.ip041000214
        "meadow moth" -> R.drawable.ip042000163
        "beet weevil" -> R.drawable.ip043000201
        "sericaorient alismots chulsky" -> R.drawable.ip044000118
        "alfalfa weevil" -> R.drawable.ip045000515
        "flax budworm" -> R.drawable.ip046000586
        "alfalfa plant bug" -> R.drawable.ip047000149
        "tarnished plant bug" -> R.drawable.ip048000202
        "Locustoidea" -> R.drawable.ip049001230
        "lytta polita" -> R.drawable.ip050000306
        "legume blister beetle" -> R.drawable.ip051000392
        "blister beetle" -> R.drawable.ip052000193
        "odontothrips loti" -> R.drawable.ip054000077
        "Thrips" -> R.drawable.ip055000083
        "alfalfa seed chalcid" -> R.drawable.ip056000057
        "Pieris canidia" -> R.drawable.ip057000256
        "Apolygus lucorum" -> R.drawable.ip058000266
        "Limacodidae" -> R.drawable.ip059000155
        "oides decempunctata" -> R.drawable.ip063000089
        "Pseudococcus comstocki Kuwana" -> R.drawable.ip065000027
        "parathrene regalis" -> R.drawable.ip066000121
        "Ampelophaga" -> R.drawable.ip067000519
        "Lycorma delicatula" -> R.drawable.ip068005049
        "Xylotrechus" -> R.drawable.ip069000863
        "Cicadella viridis" -> R.drawable.ip070000925
        "Miridae" -> R.drawable.ip071001610
        "Trialeurodes vaporariorum" -> R.drawable.ip072000190
        "Papilio xuthus" -> R.drawable.ip074000432
        "Panonchus citri McGregor" -> R.drawable.ip075000203
        "Icerya purchasi Maskell" -> R.drawable.ip077000524
        "Unaspis yanonensis" -> R.drawable.ip078000050
        "Ceroplastes rubens" -> R.drawable.ip079000134
        "Chrysomphalus aonidum" -> R.drawable.ip080000041
        "Nipaecoccus vastalor" -> R.drawable.ip082000089
        "Aleurocanthus spiniferus" -> R.drawable.ip083000397
        "Tetradacus c Bactrocera minax" -> R.drawable.ip084000101
        "Dacus dorsalis(Hendel)" -> R.drawable.ip085000222
        "Bactrocera tsuneonis" -> R.drawable.ip086000127
        "Prodenia litura" -> R.drawable.ip087000225
        "Adristyrannus" -> R.drawable.ip088000235
        "Phyllocnistis citrella Stainton" -> R.drawable.ip089000240
        "Toxoptera aurantii" -> R.drawable.ip091000178
        "Scirtothrips dorsalis Hood" -> R.drawable.ip093000520
        "Lawana imitata Melichar" -> R.drawable.ip095000505
        "Salurnis marginella Guerr" -> R.drawable.ip096000187
        "Deporaus marginatus Pascoe" -> R.drawable.ip097000106
        "Chlumetia transversa" -> R.drawable.ip098000118
        "Rhytidodera bowrinii white" -> R.drawable.ip100000448
        "Sternochetus frigidus" -> R.drawable.ip101000286
        "Cicadellidae" -> R.drawable.ip102001586

        else -> R.drawable.visible // Add a default pest image
    }





}