package com.example.editprofilescreen.ui.EditProfile
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editprofilescreen.R

@Composable
fun EditProfileScreen() {

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val purple = Color(0xFF9B5FFF)
    val bg = Color(0xFF0B0B23)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {


        TopBarNeon(
            borderColor = purple,
            onBackClick = { /* TODO */ }
        )

        Spacer(modifier = Modifier.height(30.dp))


        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .shadow(
                        elevation = 40.dp,
                        shape = CircleShape,
                        ambientColor = purple,
                        spotColor = purple
                    )
                    .clip(CircleShape)
                    .background(Color.DarkGray) // Placeholder for image
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        //---------------------------------------------------
        // 🔥 TextFields with Icons
        //---------------------------------------------------
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedPurpleField(
                value = username,
                onValueChange = { username = it },
                placeholder = "Username",
                icon = R.drawable.ic_user,
                borderColor = purple
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedPurpleField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                icon = R.drawable.ic_email,
                borderColor = purple,
                keyboard = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedPurpleField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = "Phone Number",
                icon = R.drawable.ic_phone,
                borderColor = purple,
                keyboard = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = { /* TODO */ },
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = purple)
            ) {
                Text(
                    text = "Save Changes",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = { /* TODO */ },
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = purple),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
            ) {
                Text(
                    text = "Logout",
                    fontSize = 18.sp,
                    color = purple
                )
            }
        }
    }
}


// ===================================================================
// 🔥 TOP BAR WITH CENTER TITLE + BACK ICON + NEON GLOW
// ===================================================================

@Composable
fun TopBarNeon(
    borderColor: Color,
    onBackClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {

        // 🔥 Back Icon on the left
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = borderColor
                )
            }
        }

        // 🔥 Center Title with glow
        Text(
            text = "Profile",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.shadow(
                elevation = 25.dp,
                ambientColor = borderColor,
                spotColor = borderColor
            )
        )
    }
}


// ===================================================================
// 🔥 TEXT FIELD WITH ICON
// ===================================================================

@Composable
fun OutlinedPurpleField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: Int,
    borderColor: Color,
    keyboard: KeyboardOptions = KeyboardOptions.Default
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = placeholder,
                tint = borderColor
            )
        },
        placeholder = {
            Text(text = placeholder, color = Color.Gray)
        },
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = keyboard,
        textStyle = LocalTextStyle.current.copy(color = Color.White),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = borderColor
        )
    )
}
}