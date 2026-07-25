package com.example.itihasa.ui

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.GetCredentialRequest
import com.example.itihasa.R
import com.example.itihasa.ui.theme.PlusJakartaSans
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

private fun saveUserToFirestore(
    uid: String,
    name: String,
    email: String,
    profilePic: String = "",
    onComplete: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userData = hashMapOf(
        "uid" to uid,
        "name" to name,
        "email" to email,
        "profilePicUrl" to profilePic,
        "role" to "explorer",
        "points" to 0,
        "huntsCompleted" to 0,
        "badgesEarned" to 0,
        "activeHunts" to emptyList<String>(),
        "lastLogin" to Timestamp.now()
    )
    db.collection("users").document(email)
        .set(userData, SetOptions.merge())
        .addOnSuccessListener {
            Log.d("ITIHASA_USER_DATA", "User data saved to Firestore successfully for $email")
            onComplete()
        }
        .addOnFailureListener { e ->
            Log.e("ITIHASA_USER_DATA", "Failed to save user data to Firestore for $email", e)
            onComplete()
        }
}

@Composable
fun AuthScreen(onLoginSuccess: (name: String, email: String) -> Unit) {
    var isLogin by remember { mutableStateOf(true) }
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val credentialManager = CredentialManager.create(context)
    // Use the Google web client ID generated from google-services.json so it always stays in sync
    val webClientId = context.getString(R.string.default_web_client_id)

    fun signInWithGoogleAccount(
        idToken: String,
        nameFallback: String?,
        emailFallback: String?,
        profilePicFallback: String?
    ) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(firebaseCredential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                val name = user?.displayName ?: nameFallback ?: "Explorer"
                val email = user?.email ?: emailFallback ?: ""
                val profilePic = user?.photoUrl?.toString() ?: profilePicFallback ?: ""
                val uid = user?.uid ?: email.ifBlank { nameFallback ?: "unknown_user" }

                saveUserToFirestore(uid, name, email, profilePic) {
                    isLoading = false
                    onLoginSuccess(name, email)
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = "Firebase Auth Error: ${e.localizedMessage}"
                Log.e("ITIHASA_USER_DATA", "Firebase Sign-In Failed", e)
            }
    }

    fun handleGoogleCredential(credential: androidx.credentials.Credential) {
        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    signInWithGoogleAccount(
                        idToken = googleCredential.idToken,
                        nameFallback = googleCredential.displayName,
                        emailFallback = googleCredential.id,
                        profilePicFallback = null
                    )
                } else {
                    isLoading = false
                    errorMessage = "Unsupported Google credential type."
                    Log.e("ITIHASA_USER_DATA", "Unsupported credential type: ${credential.type}")
                }
            }
            else -> {
                isLoading = false
                errorMessage = "Unsupported credential returned by the device."
                Log.e("ITIHASA_USER_DATA", "Unsupported credential class: ${credential::class.java.name}")
            }
        }
    }

    fun handleLegacyGoogleAccount(account: GoogleSignInAccount?) {
        if (account == null) {
            isLoading = false
            errorMessage = "Legacy Google Sign-In returned no account."
            return
        }

        val idToken = account.idToken
        if (idToken.isNullOrBlank()) {
            isLoading = false
            errorMessage = "Legacy Google Sign-In did not return an ID token."
            Log.e("ITIHASA_USER_DATA", "Legacy Google account missing ID token: ${account.email}")
            return
        }

        signInWithGoogleAccount(
            idToken = idToken,
            nameFallback = account.displayName,
            emailFallback = account.email,
            profilePicFallback = account.photoUrl?.toString()
        )
    }

    val legacyGoogleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data == null) {
            isLoading = false
            errorMessage = "Google Sign-In cancelled."
            return@rememberLauncherForActivityResult
        }

        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            handleLegacyGoogleAccount(account)
        } catch (e: ApiException) {
            isLoading = false
            errorMessage = "Legacy Google Sign-In Error: ${e.localizedMessage}"
            Log.e("ITIHASA_USER_DATA", "Legacy Google Sign-In failed", e)
        }
    }

    fun startLegacyGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        legacyGoogleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    fun onManualAuth() {
        if (emailInput.isBlank() || passwordInput.isBlank()) {
            errorMessage = "Please fill all fields"
            return
        }
        isLoading = true
        errorMessage = null

        if (isLogin) {
            // Manual Login
            auth.signInWithEmailAndPassword(emailInput, passwordInput)
                .addOnSuccessListener { result ->
                    val user = result.user
                    val name = user?.displayName ?: "Explorer"
                    val email = user?.email ?: emailInput
                    val uid = user?.uid ?: email
                    saveUserToFirestore(uid, name, email, user?.photoUrl?.toString() ?: "") {
                        isLoading = false
                        onLoginSuccess(name, email)
                    }
                }
                .addOnFailureListener { e ->
                    isLoading = false
                    errorMessage = e.localizedMessage
                }
        } else {
            // Manual Sign Up
            if (nameInput.isBlank()) {
                errorMessage = "Please enter your name"
                isLoading = false
                return
            }
            auth.createUserWithEmailAndPassword(emailInput, passwordInput)
                .addOnSuccessListener { result ->
                    val user = result.user
                    val uid = user?.uid ?: emailInput
                    saveUserToFirestore(uid, nameInput, emailInput, "") {
                        isLoading = false
                        onLoginSuccess(nameInput, emailInput)
                    }
                }
                .addOnFailureListener { e ->
                    isLoading = false
                    errorMessage = e.localizedMessage
                }
        }
    }

    fun onForgotPassword() {
        val email = emailInput.trim()
        if (email.isBlank()) {
            errorMessage = "Enter your email address first"
            return
        }

        isLoading = true
        errorMessage = null
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                isLoading = false
                Toast.makeText(
                    context,
                    "Password reset link sent to $email",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.localizedMessage ?: "Unable to send reset email"
            }
    }

    fun onGoogleSignIn() {
        isLoading = true
        errorMessage = null
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                handleGoogleCredential(result.credential)
            } catch (e: GetCredentialException) {
                Log.w("ITIHASA_USER_DATA", "Credential Manager failed, checking fallback", e)
                startLegacyGoogleSignIn()
            } catch (e: Exception) {
                val fallbackToLegacy = e.localizedMessage?.contains("User disabled the feature", ignoreCase = true) == true ||
                    e.localizedMessage?.contains("one tap", ignoreCase = true) == true ||
                    e.localizedMessage?.contains("16", ignoreCase = true) == true

                if (fallbackToLegacy) {
                    Log.w("ITIHASA_USER_DATA", "Falling back to legacy Google Sign-In", e)
                    startLegacyGoogleSignIn()
                } else {
                    isLoading = false
                    errorMessage = "Google Sign-In Error: ${e.localizedMessage}"
                    Log.e("ITIHASA_USER_DATA", "Google Sign-In Exception", e)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Logo
        Icon(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Itihasa Logo",
            tint = Color.Unspecified,
            modifier = Modifier.height(32.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Title
        Text(
            text = if (isLogin) "Welcome Back!" else "Create Account",
            fontSize = 28.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Text(
            text = if (isLogin) "Login to continue your journey." else "Join the community of heritage explorers.",
            fontSize = 16.sp,
            fontFamily = PlusJakartaSans,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Form
        if (!isLogin) {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Full Name", fontFamily = PlusJakartaSans) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = { Text("Email Address", fontFamily = PlusJakartaSans) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("Password", fontFamily = PlusJakartaSans) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        if (isLogin) {
            TextButton(
                onClick = { onForgotPassword() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?", color = Color(0xFFE46419), fontFamily = PlusJakartaSans)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Primary Action Button
        Button(
            onClick = { onManualAuth() },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE46419)),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = if (isLogin) "Login" else "Sign Up",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
            Text(
                text = " OR ",
                modifier = Modifier.padding(horizontal = 8.dp),
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = PlusJakartaSans
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Social Buttons
        OutlinedButton(
            onClick = { onGoogleSignIn() },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Continue with Google", color = Color.Black, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                errorMessage = "Apple sign-in is not configured yet."
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Continue with Apple", color = Color.Black, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium)
            }
        }

        // Toggle Login/Signup
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
        ) {
            Text(
                text = if (isLogin) "Don't have an account? " else "Already have an account? ",
                fontFamily = PlusJakartaSans,
                color = Color.Gray
            )
            TextButton(onClick = { isLogin = !isLogin }) {
                Text(
                    text = if (isLogin) "Sign Up" else "Login",
                    color = Color(0xFFE46419),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
