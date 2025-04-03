

package com.example.pest_detection_app.screen.user

import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
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
                popUpTo(Screen.Login.route) {
                    inclusive = true
                }
            }
        }
    }

    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            TopSection()

            Spacer(modifier = Modifier.padding(14.dp))

            Column(modifier = Modifier.padding(horizontal = 30.dp)) {
                Down(username, password, onUsernameChange = { username = it }, onPasswordChange = { password = it }, onLoginClick = {
                    viewModel.loginUser(username.text, password.text)
                })

                Spacer(modifier = Modifier.height(30.dp))

//                GoogleFacebook()

                CreateAnAccount(navController)
            }
        }
    }
}

@Composable
private fun CreateAnAccount(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxHeight(fraction = 0.8f)
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontFamily = Roboto
                    )
                ) {
                    append("Don't have an account?")
                }

                withStyle(
                    style = SpanStyle(
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontFamily = Roboto
                    )
                ) {
                    append("  ")
                    append("Create One?")
                }
            },
            modifier = Modifier.clickable { navController.navigate(Screen.SignUp.route) }
        )
    }
}


@Composable
private fun Down(username: TextFieldValue, password: TextFieldValue, onUsernameChange: (TextFieldValue) -> Unit, onPasswordChange: (TextFieldValue) -> Unit, onLoginClick: () -> Unit) {
    LoginTextField(label = "Username", value = username, onValueChange = onUsernameChange, trailing = "", modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.padding(15.dp))

    LoginTextField(label = "Password", value = password, onValueChange = onPasswordChange, trailing = "forgot?", modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.padding(15.dp))

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        onClick = onLoginClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSystemInDarkTheme()) BlueGray else Black,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(size = 4.dp)
    ) {
        Text(
            text = "Log in",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TopSection() {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Color.Black

    Box(contentAlignment = Alignment.TopCenter) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.46f),
            painter = painterResource(id = R.drawable.shape),
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )

        Row(
            modifier = Modifier.padding(80.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(42.dp),
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "content description",
                tint = Color.White
            )

            Spacer(modifier = Modifier.width(15.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ParkiDZ",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
//                Text(
//                    text = "gggggggggggg",
//                    style = MaterialTheme.typography.titleMedium,
//                    color = uiColor
//                )
            }
        }
        Text(
            modifier = Modifier
                .padding(10.dp)
                .align(alignment = Alignment.BottomCenter),
            text = "login",
            style = MaterialTheme.typography.headlineLarge,
            color = uiColor
        )
    }
}

@Composable
fun LoginTextField(label: String, value: TextFieldValue, onValueChange: (TextFieldValue) -> Unit, trailing: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        trailingIcon = {
            if (trailing.isNotEmpty()) {
                TextButton(onClick = { /* handle click */ }) {
                    Text(text = trailing)
                }
            }
        },
        visualTransformation = if (label == "Password") PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier
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
    var firstName by remember { mutableStateOf(TextFieldValue()) }
    var phoneNumber by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }

    val context = LocalContext.current


    val endpoint = userEndpoint.createEndpoint()
    val authRepository = AuthRepository(endpoint)
//    val viewModel = AccountViewModel.Factory(authRepository).create(AccountViewModel::class.java)
    val viewModel = AccountViewModel.getInstance(authRepository)

    if (viewModel.createdSuccess.value) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.Login.route)
        }
    }

    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            TopSectionSignUp()

            Spacer(modifier = Modifier.padding(14.dp))

            Column(modifier = Modifier.padding(horizontal = 30.dp)) {
                DownSignUp(
                    email, username, lastName, firstName, phoneNumber, password,
                    onEmailChange = { email = it },
                    onUsernameChange = { username = it },
                    onLastNameChange = { lastName = it },
                    onFirstNameChange = { firstName = it },
                    onPhoneNumberChange = { phoneNumber = it },
                    onPasswordChange = { password = it },
                    onSignUpClick = {
                        viewModel.signUpUser(
                            email.text, username.text, lastName.text, firstName.text, phoneNumber.text, password.text ,
                            MyApp.getContext()
                        )
                    }
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Optionally include GoogleFacebook() if needed
                AlreadyHaveAnAccount(navController)
            }
        }
    }
}

@Composable
private fun AlreadyHaveAnAccount(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxHeight(fraction = 0.8f)
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontFamily = Roboto
                    )
                ) {
                    append("Already have an account?")
                }

                withStyle(
                    style = SpanStyle(
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontFamily = Roboto
                    )
                ) {
                    append("  ")
                    append("Log in?")
                }
            },
            modifier = Modifier.clickable { navController.navigate(Screen.Login.route) }
        )
    }
}

@Composable
private fun DownSignUp(
    email: TextFieldValue,
    username: TextFieldValue,
    lastName: TextFieldValue,
    firstName: TextFieldValue,
    phoneNumber: TextFieldValue,
    password: TextFieldValue,
    onEmailChange: (TextFieldValue) -> Unit,
    onUsernameChange: (TextFieldValue) -> Unit,
    onLastNameChange: (TextFieldValue) -> Unit,
    onFirstNameChange: (TextFieldValue) -> Unit,
    onPhoneNumberChange: (TextFieldValue) -> Unit,
    onPasswordChange: (TextFieldValue) -> Unit,
    onSignUpClick: () -> Unit
) {
    SignUpTextField(label = "Email", value = email, onValueChange = onEmailChange)
    Spacer(modifier = Modifier.padding(8.dp))

    SignUpTextField(label = "Username", value = username, onValueChange = onUsernameChange)
    Spacer(modifier = Modifier.padding(8.dp))

    SignUpTextField(label = "Last Name", value = lastName, onValueChange = onLastNameChange)
    Spacer(modifier = Modifier.padding(8.dp))

    SignUpTextField(label = "First Name", value = firstName, onValueChange = onFirstNameChange)
    Spacer(modifier = Modifier.padding(8.dp))

    SignUpTextField(label = "Phone Number", value = phoneNumber, onValueChange = onPhoneNumberChange)
    Spacer(modifier = Modifier.padding(8.dp))

    SignUpTextField(label = "Password", value = password, onValueChange = onPasswordChange)
    Spacer(modifier = Modifier.padding(8.dp))

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        onClick = onSignUpClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSystemInDarkTheme()) BlueGray else Black,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(size = 4.dp)
    ) {
        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TopSectionSignUp() {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Color.Black

    Box(contentAlignment = Alignment.TopCenter) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.18f),
            painter = painterResource(id = R.drawable.shape),
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )

        Row(
            modifier = Modifier.padding(50.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(42.dp),
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "content description",
                tint = Color.White
            )

            Spacer(modifier = Modifier.width(15.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ParkiDZ",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
//                Text(
//                    text = "gggggggggggg",
//                    style = MaterialTheme.typography.titleMedium,
//                    color = uiColor
//                )
            }
        }
        Text(
            modifier = Modifier
                .padding(10.dp)
                .align(alignment = Alignment.BottomCenter),
            text = "Sign Up",
            style = MaterialTheme.typography.headlineLarge,
            color = uiColor
        )
    }
}

@Composable
fun SignUpTextField(label: String, value: TextFieldValue, onValueChange: (TextFieldValue) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}
