package com.example.techshop.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.techshop.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.TimeUnit

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, IntroActivity::class.java))
                    finishAffinity()
                },
                onBackClick = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit, onBackClick: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var changePasswordStep by remember { mutableStateOf("select_method") } // select_method, enter_otp, enter_password
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance().getReference("users")

    // Tải dữ liệu từ Realtime Database
    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            database.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    username = snapshot.child("username").getValue(String::class.java) ?: ""
                    phone = snapshot.child("phone").getValue(String::class.java) ?: ""
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Lỗi tải dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hồ sơ cá nhân",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.purple),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ảnh đại diện
            AsyncImage(
                model = user?.photoUrl ?: "https://via.placeholder.com/150",
                contentDescription = "Ảnh đại diện",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.lightGrey))
                    .clickable {
                        Toast.makeText(context, "Chức năng chọn ảnh chưa được triển khai", Toast.LENGTH_SHORT).show()
                    },
                placeholder = painterResource(R.drawable.ic_launcher_background)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tên đăng nhập
            if (isEditing) {
                BasicTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(
                            colorResource(R.color.lightGrey),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                )
            } else {
                Text(
                    text = username.ifEmpty { "Chưa đặt tên" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Số điện thoại
            if (isEditing) {
                BasicTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(
                            colorResource(R.color.lightGrey),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                )
            } else {
                Text(
                    text = phone.ifEmpty { "Chưa có số điện thoại" },
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút chỉnh sửa/lưu
            Button(
                onClick = {
                    if (isEditing) {
                        user?.uid?.let { uid ->
                            val userData = mapOf(
                                "username" to username,
                                "phone" to phone,
                                "email" to user?.email
                            )
                            database.child(uid).setValue(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show()
                                    isEditing = false
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Cập nhật thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        isEditing = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.purple),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isEditing) "Lưu" else "Chỉnh sửa",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút đổi mật khẩu
            Button(
                onClick = { showChangePasswordDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.purple),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Đổi mật khẩu",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút đăng xuất
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Đăng xuất",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Dialog đổi mật khẩu
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showChangePasswordDialog = false
                changePasswordStep = "select_method"
                otp = ""
                newPassword = ""
                confirmPassword = ""
                verificationId = ""
            },
            title = { Text("Đổi mật khẩu", fontWeight = FontWeight.Bold) },
            text = {
                when (changePasswordStep) {
                    "select_method" -> {
                        Column {
                            Text("Chọn phương thức xác minh:")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (phone.isNotEmpty()) {
                                        // Định dạng số điện thoại
                                        val formattedPhone = if (phone.startsWith("+")) phone else "+84${phone.removePrefix("0")}"
                                        // Gửi OTP qua số điện thoại
                                        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                                            .setPhoneNumber(formattedPhone)
                                            .setTimeout(60L, TimeUnit.SECONDS)
                                            .setActivity(context as ComponentActivity)
                                            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                                    // Auto-verification (hiếm gặp)
                                                }

                                                override fun onVerificationFailed(exception: com.google.firebase.FirebaseException) {
                                                    Toast.makeText(context, "Gửi OTP thất bại: ${exception.message}", Toast.LENGTH_SHORT).show()
                                                }

                                                override fun onCodeSent(verId: String, token: PhoneAuthProvider.ForceResendingToken) {
                                                    verificationId = verId
                                                    changePasswordStep = "enter_otp"
                                                }
                                            })
                                            .build()
                                        PhoneAuthProvider.verifyPhoneNumber(options)
                                    } else {
                                        Toast.makeText(context, "Vui lòng cập nhật số điện thoại trước", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(R.color.purple),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Gửi OTP qua số điện thoại")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    user?.email?.let { email ->
                                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Email đặt lại mật khẩu đã được gửi", Toast.LENGTH_SHORT).show()
                                                showChangePasswordDialog = false
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Gửi email thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    } ?: Toast.makeText(context, "Không tìm thấy email", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(R.color.purple),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Gửi email đặt lại mật khẩu")
                            }
                        }
                    }
                    "enter_otp" -> {
                        Column {
                            Text("Nhập mã OTP (6 chữ số):")
                            Spacer(modifier = Modifier.height(8.dp))
                            BasicTextField(
                                value = otp,
                                onValueChange = { if (it.length <= 6) otp = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        colorResource(R.color.lightGrey),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(16.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color.Black,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }
                    "enter_password" -> {
                        Column {
                            Text("Nhập mật khẩu mới:")
                            Spacer(modifier = Modifier.height(8.dp))
                            BasicTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        colorResource(R.color.lightGrey),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(16.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color.Black,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Xác nhận mật khẩu:")
                            Spacer(modifier = Modifier.height(8.dp))
                            BasicTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        colorResource(R.color.lightGrey),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(16.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color.Black,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (changePasswordStep != "select_method") {
                    Button(
                        onClick = {
                            when (changePasswordStep) {
                                "enter_otp" -> {
                                    if (otp.length == 6) {
                                        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                                        FirebaseAuth.getInstance().signInWithCredential(credential)
                                            .addOnSuccessListener {
                                                changePasswordStep = "enter_password"
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "OTP không đúng: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(context, "Vui lòng nhập OTP 6 chữ số", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                "enter_password" -> {
                                    if (newPassword.isNotEmpty() && newPassword == confirmPassword) {
                                        user?.updatePassword(newPassword)
                                            ?.addOnSuccessListener {
                                                Toast.makeText(context, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                                                showChangePasswordDialog = false
                                                changePasswordStep = "select_method"
                                                otp = ""
                                                newPassword = ""
                                                confirmPassword = ""
                                                verificationId = ""
                                            }
                                            ?.addOnFailureListener { e ->
                                                Toast.makeText(context, "Đổi mật khẩu thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(context, "Mật khẩu không khớp hoặc rỗng", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.purple),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Xác nhận")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showChangePasswordDialog = false
                    changePasswordStep = "select_method"
                    otp = ""
                    newPassword = ""
                    confirmPassword = ""
                    verificationId = ""
                }) {
                    Text("Hủy")
                }
            }
        )
    }
}