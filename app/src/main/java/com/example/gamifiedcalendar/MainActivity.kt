package com.example.gamifiedcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// 1. 화면 경로(Route) 정의
object Destinations {
    const val LOGIN = "login"
    const val MAIN = "main"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    AppNavigation() // 내비게이션 시작
                }
            }
        }
    }
}

// 2. 전체 내비게이션 구조 (AppNavigation)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.LOGIN
    ) {
        // 로그인 화면
        composable(Destinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // 로그인 성공 시 메인으로 이동하며 로그인 화면을 백스택에서 제거
                    navController.navigate(Destinations.MAIN) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        // 메인 화면
        composable(Destinations.MAIN) {
            MainScreen()
        }
    }
}

// --- 화면 1: 로그인 화면 ---
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Gamified Calendar",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(32.dp))

        AvatarGroup()

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "계정 만들기", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "앱을 시작하려면 이메일을 입력하세요",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        CustomTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "email@domain.com"
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomButton(
            text = "계속",
            backgroundColor = Color.Black,
            contentColor = Color.White,
            onClick = onLoginSuccess // 클릭 시 메인 화면으로 이동
        )

        Spacer(modifier = Modifier.height(24.dp))

        DividerWithText()

        Spacer(modifier = Modifier.height(24.dp))

        SocialLoginButton(
            text = "Google 계정으로 계속하기",
            iconRes = android.R.drawable.ic_menu_compass,
            onClick = onLoginSuccess
        )

        Spacer(modifier = Modifier.height(12.dp))

        SocialLoginButton(
            text = "Apple 계정으로 계속하기",
            iconRes = android.R.drawable.ic_menu_info_details,
            onClick = onLoginSuccess
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "계속을 클릭하면 당사의 서비스 이용 약관 및\n개인정보 처리방침에 동의하는 것으로 간주됩니다.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

// --- 화면 2: 메인 화면 (Bottom Navigation 예시) ---
@Composable
fun MainScreen() {
    val items = listOf(
        BottomNavItem("홈", Icons.Default.Home),
        BottomNavItem("일정", Icons.Default.DateRange),
        BottomNavItem("프로필", Icons.Default.Person)
    )
    var selectedItem by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "${items[selectedItem].title} 화면입니다.", fontSize = 20.sp)
        }
    }
}

data class BottomNavItem(val title: String, val icon: ImageVector)

// --- 공통 컴포넌트 (Common Components) ---

@Composable
fun CustomTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.LightGray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.LightGray,
            cursorColor = Color.Black
        ),
        singleLine = true
    )
}

@Composable
fun CustomButton(text: String, backgroundColor: Color, contentColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor, contentColor = contentColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SocialLoginButton(text: String, iconRes: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AvatarGroup() {
    Row(horizontalArrangement = Arrangement.Center) {
        val overlap = (-15).dp
        listOf(Color.LightGray, Color.Gray, Color.DarkGray).forEachIndexed { index, color ->
            Box(
                modifier = Modifier
                    .offset(x = if (index > 0) overlap * index else 0.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
fun DividerWithText() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
        Text("또는", modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray, fontSize = 13.sp)
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    LoginScreen(onLoginSuccess = {})
}
