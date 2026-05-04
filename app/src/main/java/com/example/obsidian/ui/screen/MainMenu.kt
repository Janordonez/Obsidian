package com.example.obsidian.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ElevatedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MainMenu(
    onNewInvestigation: () -> Unit,
    onContinueCase: () -> Unit,
    onSettings: () -> Unit
) {
    val darkNavy = Color(0xFF030617)
    val deepBlack = Color(0xFF000000)
    val neonYellow = Color(0xFFFFF05A)
    val mutedText = Color(0xFF7F8EA3)
    val recRed = Color(0xFFEE4B4B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(darkNavy, deepBlack)
                )
            )
            .padding(20.dp)
    ) {
        // Top bar: system info and REC indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "SYS.STATUS: ONLINE",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = mutedText
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "MEM: 04.69A",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = mutedText
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "LAT: 34.0522° N",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = mutedText
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "LNG: 118.2437° W",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = mutedText
                    )
                )
            }

            // REC indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color = recRed, shape = CircleShape)
                        .border(BorderStroke(1.dp, Color(0x33FFFFFF)), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REC",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = recRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Center content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .wrapContentHeight(align = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "OBSIDIAN CASEFILE",
                style = TextStyle(
                    color = neonYellow,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    shadow = Shadow(
                        color = neonYellow.copy(alpha = 0.65f),
                        offset = Offset(0f, 0f),
                        blurRadius = 36f
                    )
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Buttons
            Column(
                modifier = Modifier.fillMaxWidth(0.78f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ElevatedButton(
                    onClick = onNewInvestigation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(12.dp, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0B0F14),
                        contentColor = neonYellow
                    ),
                    border = BorderStroke(1.dp, neonYellow.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "NEW INVESTIGATION",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                ElevatedButton(
                    onClick = onContinueCase,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0B0F14),
                        contentColor = neonYellow
                    ),
                    border = BorderStroke(1.dp, neonYellow.copy(alpha = 0.28f))
                ) {
                    Text(
                        text = "CONTINUE CASE",
                        style = TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                ElevatedButton(
                    onClick = onSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0B0F14),
                        contentColor = neonYellow
                    ),
                    border = BorderStroke(1.dp, neonYellow.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "SYSTEM SETTINGS",
                        style = TextStyle(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    )
                }

            }
        }

        // Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color.Transparent
            ) {
                Text(
                    text = "V1.0 // RESTRICTED ACCESS // SECURED.LN",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = mutedText
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainMenuPreview() {
    MainMenu(onNewInvestigation = {}, onContinueCase = {}, onSettings = {})
}

