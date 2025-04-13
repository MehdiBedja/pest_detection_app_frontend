

package com.example.pest_detection_app.screen.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.pest_detection_app.MyApp
import com.example.pest_detection_app.R
import com.example.pest_detection_app.endpoint.user.userEndpoint
import com.example.pest_detection_app.repository.user.AuthRepository
import com.example.pest_detection_app.screen.navigation.Screen
import com.example.pest_detection_app.ViewModels.user.AccountViewModel
import com.example.pest_detection_app.ViewModels.user.LoginViewModel
import com.example.pest_detection_app.ui.theme.*










@Composable
fun LogInScreen(navController: NavHostController, viewModel: LoginViewModel) {
    var username by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }

    if (viewModel.login.value) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CardBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(Color(0xFF5C6BC0)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.login1copy),
                    contentDescription = "Login Illustration",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 280.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5C6BC0)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LoginTextField("Username", username, { username = it }, isPassword = false)
                        Spacer(modifier = Modifier.height(12.dp))

                        LoginTextField("Password", password, { password = it }, isPassword = true)
                        Spacer(modifier = Modifier.height(20.dp))

                        if (viewModel.loading.value) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(top = 8.dp),
                                color = Color(0xFF5C6BC0)
                            )
                        } else {
                            Button(
                                onClick = {
                                    viewModel.loginUser(username.text, password.text)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF5C6BC0),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("LOGIN", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (viewModel.error.value != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = viewModel.error.value ?: "",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Not yet registered?", color = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sign Up Now",
                        color = Color(0xFF5C6BC0),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.SignUp.route)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun LoginTextField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isPassword: Boolean = false
) {
    val visualTransformation =
        if (isPassword) PasswordVisualTransformation() else VisualTransformation.None

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        visualTransformation = visualTransformation,
        leadingIcon = {
            Icon(
                imageVector = if (label == "Password") Icons.Default.Lock else Icons.Default.Person,
                contentDescription = null
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF5C6BC0),
            unfocusedBorderColor = Color.LightGray,
            cursorColor = Color(0xFF5C6BC0)
        )
    )
}



@Composable
fun LogoutScreen(navController: NavController, viewModel: LoginViewModel) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        onClick = { viewModel.logout()
            navController.navigate("home_screen") {
                popUpTo("profile") { inclusive = true }
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSystemInDarkTheme()) BlueGray else Black,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(size = 4.dp)
    ) {
        Text(
            text = "LogOut",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun SignUpScreen(navController: NavHostController) {
    var email by remember { mutableStateOf(TextFieldValue()) }
    var username by remember { mutableStateOf(TextFieldValue()) }
    var lastName by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var repeatPassword by remember { mutableStateOf(TextFieldValue()) }

    // Error state for validations
    var emailError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var repeatPasswordError by remember { mutableStateOf<String?>(null) }

    val endpoint = userEndpoint.createEndpoint()
    val authRepository = AuthRepository(endpoint)
    val viewModel = AccountViewModel.getInstance(authRepository)

    if (viewModel.createdSuccess.value) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.SignUp.route) { inclusive = true }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CardBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sign Up",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5C6BC0)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SignUpTextField("Email", email, onValueChange = {
                            email = it
                            emailError = null
                        })
                        if (emailError != null) Text(emailError!!, color = Color.Red, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        SignUpTextField("Username", username, onValueChange = {
                            username = it
                            usernameError = null
                        })
                        if (usernameError != null) Text(usernameError!!, color = Color.Red, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        SignUpTextField("Full Name", lastName, onValueChange = {
                            lastName = it
                            lastNameError = null
                        })
                        if (lastNameError != null) Text(lastNameError!!, color = Color.Red, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        SignUpTextField("Password", password, onValueChange = {
                            password = it
                            passwordError = null
                        }, isPassword = true)
                        if (passwordError != null) Text(passwordError!!, color = Color.Red, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        SignUpTextField("Repeat Password", repeatPassword, onValueChange = {
                            repeatPassword = it
                            repeatPasswordError = null
                        }, isPassword = true)
                        if (repeatPasswordError != null) Text(repeatPasswordError!!, color = Color.Red, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(20.dp))

                        if (viewModel.loading.value) {
                            CircularProgressIndicator(color = Color(0xFF5C6BC0))
                        } else {
                            Button(
                                onClick = {
                                    // Validation
                                    var isValid = true
                                    if (email.text.isBlank()) {
                                        emailError = "Email cannot be empty"
                                        isValid = false
                                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.text).matches()) {
                                        emailError = "Invalid email format"
                                        isValid = false
                                    }

                                    if (username.text.isBlank()) {
                                        usernameError = "Username cannot be empty"
                                        isValid = false
                                    }

                                    if (lastName.text.isBlank()) {
                                        lastNameError = "Full name cannot be empty"
                                        isValid = false
                                    }

                                    if (password.text.isBlank()) {
                                        passwordError = "Password cannot be empty"
                                        isValid = false
                                    } else if (password.text.length < 6) {
                                        passwordError = "Password must be at least 6 characters"
                                        isValid = false
                                    }

                                    if (repeatPassword.text != password.text) {
                                        repeatPasswordError = "Passwords do not match"
                                        isValid = false
                                    }

                                    if (isValid) {
                                        viewModel.signUpUser(
                                            email.text,
                                            username.text,
                                            lastName.text,
                                            null.toString(),
                                            null.toString(),
                                            password.text,
                                            MyApp.getContext()
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF5C6BC0),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("SIGN UP", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (viewModel.error.value != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = viewModel.error.value ?: "",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Already have an account?", color = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Log In",
                        color = Color(0xFF5C6BC0),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.Login.route)
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpTextField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (isPassword) {
                val iconRes = if (passwordVisible) R.drawable.visible else R.drawable.notvisible

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Toggle password visibility",
                        tint = Color.Unspecified // Keep original image color
                    )
                }
            }

        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color(0xFF5C6BC0),
            focusedLabelColor = Color(0xFF5C6BC0)
        )
    )
}
