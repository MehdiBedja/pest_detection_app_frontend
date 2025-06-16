package com.example.pest_detection_app.screen.user

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pest_detection_app.R
import com.example.pest_detection_app.ViewModels.user.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    // Get if user is Google user
    val userId by viewModel.userId.collectAsState()

    LaunchedEffect(userId) {
        userId?.let { id ->
            viewModel.loadIsGoogle(id)
        }
    }
    val isGoogle by viewModel.isGoogle.collectAsState()


    // Password states
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Visibility states
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Validation states
    var passwordsMatch by remember { mutableStateOf(true) }
    var showValidation by remember { mutableStateOf(false) }

    // ViewModel states
    val loading by viewModel.passwordLoading
    val error by viewModel.passwordError
    val success by viewModel.passwordSuccess

    // Password validation
    LaunchedEffect(newPassword, confirmPassword) {
        if (confirmPassword.isNotEmpty()) {
            showValidation = true
            passwordsMatch = newPassword == confirmPassword
        } else {
            showValidation = false
        }
    }

    // Success handling
    LaunchedEffect(success) {
        if (success) {
            navController.popBackStack()
            viewModel.clearPasswordStates()
        }
    }

    // Clear states when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearPasswordStates()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(if (isGoogle == true) R.string.set_password else R.string.change_password),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Instructions
                Text(
                    text = stringResource(
                        if (isGoogle == true) R.string.set_password_instruction
                        else R.string.change_password_instruction
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Password Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Old Password Field (only for non-Google users)
                        if (isGoogle==false) {
                            OutlinedTextField(
                                value = oldPassword,
                                onValueChange = { oldPassword = it },
                                label = { Text(stringResource(R.string.current_password)) },
                                visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                                        Icon(
                                            painter = if (oldPasswordVisible) painterResource(id = R.drawable.visible)  else painterResource( id =R.drawable.notvisible),
                                            contentDescription = if (oldPasswordVisible) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        // New Password Field
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text(stringResource(R.string.new_password)) },
                            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(
                                        painter = if (oldPasswordVisible) painterResource(id = R.drawable.visible)  else painterResource( id =R.drawable.notvisible),
                                        contentDescription = if (newPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        // Confirm Password Field
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text(stringResource(R.string.confirm_password)) },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        painter = if (oldPasswordVisible) painterResource(id = R.drawable.visible)  else painterResource( id =R.drawable.notvisible),
                                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            isError = showValidation && !passwordsMatch,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (showValidation && !passwordsMatch) Color.Red else MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (showValidation && !passwordsMatch) Color.Red else MaterialTheme.colorScheme.outline,
                                errorBorderColor = Color.Red
                            )
                        )

                        // Password match validation
                        if (showValidation) {
                            Text(
                                text = if (passwordsMatch)
                                    stringResource(R.string.passwords_match)
                                else
                                    stringResource(R.string.passwords_dont_match),
                                color = if (passwordsMatch) Color.Green else Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        // Error message
                        error?.let { errorMessage ->
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Confirm Button
                        Button(
                            onClick = {
                                if (passwordsMatch && newPassword.isNotEmpty() &&
                                    (isGoogle ==true  || oldPassword.isNotEmpty())) {
                                    userId?.let {
                                        viewModel.changePassword(
                                            it,
                                            oldPassword = if (isGoogle ==true) null else oldPassword,
                                            newPassword = newPassword,
                                        )
                                    }
                                }
                            },
                            enabled = !loading && passwordsMatch && newPassword.isNotEmpty() &&
                                    (isGoogle == true  || oldPassword.isNotEmpty()),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.confirm),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))


            }
        }
    }
}