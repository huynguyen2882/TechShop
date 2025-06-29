package com.example.techshop.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.techshop.Helper.ManagmentCart
import com.example.techshop.Model.ItemsModel
import com.example.techshop.Model.OrderItemModel
import com.example.techshop.Model.OrderModel
import com.example.techshop.R
import com.example.techshop.utils.toVND
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cartItems = intent.getParcelableArrayListExtra<ItemsModel>("cartItems") ?: arrayListOf()
        val total = intent.getDoubleExtra("total", 0.0)
        setContent {
            PaymentScreen(cartItems, total)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(cartItems: ArrayList<ItemsModel>, total: Double) {
    val name = remember { mutableStateOf(TextFieldValue()) }
    val address = remember { mutableStateOf(TextFieldValue()) }
    val phone = remember { mutableStateOf(TextFieldValue()) }
    val selectedMethod = remember { mutableStateOf("Thanh toán khi nhận hàng") }
    val methods = listOf("Thanh toán khi nhận hàng", "Chuyển khoản ngân hàng", "Momo")
    var expanded by remember { mutableStateOf(false) }
    var showQRDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var nameError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val managmentCart = remember { ManagmentCart(context) }
    val auth = FirebaseAuth.getInstance()

    // QR Dialog
    if (showQRDialog) {
        QRCodeDialog(
            paymentMethod = selectedMethod.value,
            amount = total,
            onDismiss = { showQRDialog = false },
            onConfirmPayment = {
                showQRDialog = false
                // Xử lý thanh toán sau khi hiển thị QR
                processPayment(
                    cartItems = cartItems,
                    total = total,
                    name = name.value.text,
                    address = address.value.text,
                    phone = phone.value.text,
                    paymentMethod = selectedMethod.value,
                    context = context,
                    managmentCart = managmentCart,
                    auth = auth,
                    scope = scope,
                    snackbarHostState = snackbarHostState
                )
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? AppCompatActivity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.purple)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDEDFE3))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Thông tin đơn hàng",
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color(0xFF4A548D)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tổng tiền: ${total.toVND()}",
                        fontSize = 18.sp,
                        color = Color(0xFFC72216)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDEDFE3))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Thông tin người nhận",
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color(0xFF4A548D)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = name.value,
                        onValueChange = {
                            name.value = it
                            nameError = if (it.text.isBlank()) "Tên không được để trống" else null
                        },
                        label = { Text("Tên người nhận") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A548D),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    nameError?.let {
                        Text(it, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = address.value,
                        onValueChange = {
                            address.value = it
                            addressError = if (it.text.isBlank()) "Địa chỉ không được để trống" else null
                        },
                        label = { Text("Địa chỉ") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = addressError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A548D),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    addressError?.let {
                        Text(it, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone.value,
                        onValueChange = {
                            phone.value = it
                            phoneError = if (phone.value.text.isBlank()) {
                                "Số điện thoại không được để trống"
                            } else if (!it.text.matches(Regex("^[0-9]{10,11}$"))) {
                                "Số điện thoại không hợp lệ"
                            } else null
                        },
                        label = { Text("Số điện thoại") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = phoneError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A548D),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    phoneError?.let {
                        Text(it, color = Color.Red, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDEDFE3))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Phương thức thanh toán",
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color(0xFF4A548D)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box {
                        OutlinedTextField(
                            value = selectedMethod.value,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        tint = Color(0xFF4A548D)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4A548D),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            methods.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method) },
                                    onClick = {
                                        selectedMethod.value = method
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    nameError = if (name.value.text.isBlank()) "Tên không được để trống" else null
                    addressError = if (address.value.text.isBlank()) "Địa chỉ không được để trống" else null
                    phoneError = if (phone.value.text.isBlank()) {
                        "Số điện thoại không được để trống"
                    } else if (!phone.value.text.matches(Regex("^[0-9]{10,11}$"))) {
                        "Số điện thoại không hợp lệ"
                    } else null

                    if (nameError == null && addressError == null && phoneError == null) {
                        // Kiểm tra phương thức thanh toán
                        when (selectedMethod.value) {
                            "Chuyển khoản ngân hàng", "Momo" -> {
                                showQRDialog = true
                            }
                            else -> {
                                // Thanh toán khi nhận hàng
                                processPayment(
                                    cartItems = cartItems,
                                    total = total,
                                    name = name.value.text,
                                    address = address.value.text,
                                    phone = phone.value.text,
                                    paymentMethod = selectedMethod.value,
                                    context = context,
                                    managmentCart = managmentCart,
                                    auth = auth,
                                    scope = scope,
                                    snackbarHostState = snackbarHostState
                                )
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.purple)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Xác nhận thanh toán", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun QRCodeDialog(
    paymentMethod: String,
    amount: Double,
    onDismiss: () -> Unit,
    onConfirmPayment: () -> Unit
) {
    val context = LocalContext.current

    // Kiểm tra resource có tồn tại không
    val qrResource = when (paymentMethod) {
        "Chuyển khoản ngân hàng" -> R.drawable.qr_bank
        "Momo" -> R.drawable.qr_momo
        else -> R.drawable.qr_bank // fallback
    }

    // Kiểm tra xem resource có tồn tại không
    val resourceExists = remember(qrResource) {
        try {
            context.resources.getResourceName(qrResource)
            true
        } catch (e: Exception) {
            false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header với nút đóng
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thanh toán $paymentMethod",
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color(0xFF4A548D)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // QR Code thực tế hoặc placeholder
                if (resourceExists) {
                    Image(
                        painter = painterResource(id = qrResource),
                        contentDescription = "QR Code $paymentMethod",
                        modifier = Modifier
                            .size(200.dp)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(8.dp)
                    )
                } else {
                    // Fallback nếu không có file QR
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(
                                color = Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "QR CODE",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = paymentMethod,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Thông tin thanh toán
                Text(
                    text = "Số tiền: ${amount.toVND()}",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color(0xFFC72216)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (paymentMethod) {
                        "Chuyển khoản ngân hàng" -> "Ngân hàng: TechBank\nSố tài khoản: 1234567890\nChủ tài khoản: TechShop"
                        "Momo" -> "Số điện thoại: 0123456789\nChủ tài khoản: TechShop"
                        else -> ""
                    },
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Hướng dẫn
                Text(
                    text = "Vui lòng quét mã QR để thanh toán",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Nút xác nhận
                Button(
                    onClick = onConfirmPayment,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.purple)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Đã thanh toán xong", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

fun processPayment(
    cartItems: ArrayList<ItemsModel>,
    total: Double,
    name: String,
    address: String,
    phone: String,
    paymentMethod: String,
    context: android.content.Context,
    managmentCart: ManagmentCart,
    auth: FirebaseAuth,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val currentUser = auth.currentUser
    if (currentUser == null) {
        scope.launch {
            snackbarHostState.showSnackbar("Vui lòng đăng nhập để đặt hàng.")
        }
        return
    }

    val userId = currentUser.uid
    val database = FirebaseDatabase.getInstance().reference
    val orderRef = database.child("orders").push()
    val orderId = orderRef.key ?: return

    val orderItems = cartItems.map { item ->
        OrderItemModel(
            title = item.title,
            price = item.price,
            numberInCart = item.numberInCart
        )
    }

    val order = OrderModel(
        orderId = orderId,
        userId = userId,
        name = name,
        address = address,
        phone = phone,
        paymentMethod = paymentMethod,
        total = total,
        items = orderItems,
        timestamp = System.currentTimeMillis(),
        status = "Pending"
    )

    orderRef.setValue(order)
        .addOnSuccessListener {
            managmentCart.clearCart()
            scope.launch {
                snackbarHostState.showSnackbar("Đặt hàng thành công!")
                // Chuyển sang OrdersActivity
                val ordersIntent = Intent(context, OrdersActivity::class.java)
                context.startActivity(ordersIntent)
                // Kết thúc PaymentActivity
                (context as? AppCompatActivity)?.finish()
            }
        }
        .addOnFailureListener { e ->
            scope.launch {
                snackbarHostState.showSnackbar("Đặt hàng thất bại: ${e.message}")
            }
        }
}