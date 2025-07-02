import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bedjamahdi.scanpestai.R
import com.bedjamahdi.scanpestai.screen.PagerIndicator
import com.bedjamahdi.scanpestai.screen.navigation.Screen
import com.bedjamahdi.scanpestai.ui.theme.CustomTextStyles
import com.bedjamahdi.scanpestai.screen.OnboardingManager
import com.bedjamahdi.scanpestai.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingFirstScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val onboardingManager = OnboardingManager(context)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Background - Extends to very top of screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f) // Takes up 50% of screen height
                        .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.screenfirst),
                        contentDescription = null,
                        contentScale = ContentScale.Crop, // Changed to Crop for better coverage
                        modifier = Modifier.fillMaxSize() // Fill the entire box
                    )
                }

                // Spacer to create space for card overlap
                Spacer(modifier = Modifier.height(80.dp))
            }

            // Skip Button - Floating at top right with status bar padding
            Button(
                onClick = {
                    onboardingManager.setOnboardingSeen(true)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.OnboardingFirst.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding() // Add status bar padding
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.skip),
                    style = CustomTextStyles.buttonText
                )
            }

            // Full-width Card overlapping the image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 360.dp)
                    .padding(bottom = 80.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = stringResource(R.string.onboarding_title_1),
                        style = CustomTextStyles.sectionHeader.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subtitle
                    Text(
                        text = stringResource(R.string.onboarding_subtitle_1),
                        style = AppTypography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Pager Indicator
                    PagerIndicator(
                        totalPages = 3,
                        currentPage = 0
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Continue Button - Fixed at bottom
            Button(
                onClick = {
                    navController.navigate(Screen.OnboardingSecond.route)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp)
                    .navigationBarsPadding(), // Add navigation bar padding
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.continue_button),
                    style = CustomTextStyles.buttonText
                )
            }
        }
    }
}