package com.example.techshop.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.techshop.Admin.AdminMainActivity
import com.example.techshop.ui.screen.ForgotPasswordDialog
import com.example.techshop.ui.screen.LoginScreen
import com.example.techshop.ui.screen.RegisterScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class IntroActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cấu hình Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            IntroScreen()
        }

        // Kiểm tra nếu người dùng đã đăng nhập
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            checkUserRoleAndNavigate(user.uid)
        }
    }

    private fun checkUserRoleAndNavigate(userId: String) {
        FirebaseDatabase.getInstance().getReference("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val role = snapshot.child("role").getValue(String::class.java) ?: "USER"
                    if (role == "ADMIN") {
                        startActivity(Intent(this@IntroActivity, AdminMainActivity::class.java))
                    } else {
                        startActivity(Intent(this@IntroActivity, MainActivity::class.java))
                    }
                    finish()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@IntroActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                    // Mặc định điều hướng đến MainActivity nếu có lỗi
                    startActivity(Intent(this@IntroActivity, MainActivity::class.java))
                    finish()
                }
            })
    }

    @Composable
    fun IntroScreen() {
        var showLoginScreen by remember { mutableStateOf(true) }
        var showForgotPasswordDialog by remember { mutableStateOf(false) }

        if (showForgotPasswordDialog) {
            ForgotPasswordDialog(
                onDismiss = { showForgotPasswordDialog = false },
                onResetPassword = { email ->
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            Toast.makeText(this@IntroActivity, "Email đặt lại mật khẩu đã được gửi", Toast.LENGTH_SHORT).show()
                            showForgotPasswordDialog = false
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@IntroActivity, "Gửi email thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            )
        }

        if (showLoginScreen) {
            LoginScreen(
                onLoginSuccess = {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        checkUserRoleAndNavigate(user.uid)
                    }
                },
                onSwitchToRegister = { showLoginScreen = false },
                onForgotPassword = { showForgotPasswordDialog = true },
                onGoogleSignIn = { signInWithGoogle() }
            )
        } else {
            RegisterScreen(
                onRegisterSuccess = {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        checkUserRoleAndNavigate(user.uid)
                    }
                },
                onSwitchToLogin = { showLoginScreen = true }
            )
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Đăng nhập bằng Google thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        // Lưu thông tin người dùng vào Firebase Database nếu chưa tồn tại
                        val userRef = FirebaseDatabase.getInstance().getReference("users").child(user.uid)
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (!snapshot.exists()) {
                                    val userData = mapOf(
                                        "username" to user.displayName,
                                        "email" to user.email,
                                        "phone" to user.phoneNumber,
                                        "role" to "USER" // Gán mặc định role là USER
                                    )
                                    userRef.setValue(userData)
                                }
                                checkUserRoleAndNavigate(user.uid)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@IntroActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                } else {
                    Toast.makeText(this, "Đăng nhập thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}