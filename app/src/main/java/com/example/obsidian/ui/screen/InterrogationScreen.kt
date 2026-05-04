package com.example.obsidian.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colores
val NeonYellow = Color(0xFFFFF05A)
val NeonCyan = Color(0xFF00F0FF)
val PanelColor = Color(0xFF0B0F14)
val AggressiveRed = Color(0xFFFF4B4B)
val NeutralGold = Color(0xFFC0A55D)
val PassiveBlue = Color(0xFF5DADE2)

@Composable
fun InterrogationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF050816), Color(0xFF000000))
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text(
            text = "CASE_FILE_#042: INTERROGATION",
            color = NeonYellow,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Suspect Bio Card
        SuspectStatusCard()

        // Conversation Log
        Box(modifier = Modifier.weight(1f)) {
            ConversationLog()
        }

        // Acciones Tácticas
        Text(
            text = "ACCIONES TÁCTICAS",
            color = Color.Gray,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ApproachButton(
                title = "SUAVE",
                preview = "Mira, solo quiero entender tu versión...",
                color = PassiveBlue,
                icon = Icons.Default.Face
            )
            ApproachButton(
                title = "NEUTRO",
                preview = "Tenemos las grabaciones. Explica esto.",
                color = NeutralGold,
                icon = Icons.Default.Info
            )
            ApproachButton(
                title = "AGRESIVO",
                preview = "Se acabó el juego. ¡Habla ahora!",
                color = AggressiveRed,
                icon = Icons.Default.Warning
            )
        }
    }
}

@Composable
fun SuspectStatusCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = PanelColor),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.DarkGray, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto Placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.Black)
                    .border(1.dp, Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text("#84-B", color = NeonYellow, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SUJETO: DESCONOCIDO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("112 BPM", color = NeonYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Text("ESTADO: HOSTIL", color = AggressiveRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                // Barra de Tensión
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("NIVEL DE TENSIÓN", color = Color.Gray, fontSize = 9.sp)
                        Text("CRÍTICO", color = AggressiveRed, fontSize = 9.sp)
                    }
                    LinearProgressIndicator(
                        progress = 0.85f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = AggressiveRed,
                        trackColor = Color(0xFF1A1A1A)
                    )
                }
            }
        }
    }
}

data class Message(val sender: String, val text: String, val isDetective: Boolean)

@Composable
fun ConversationLog() {
    val messages = listOf(
        Message("DETECTIVE", "Sabemos que estuviste en los muelles esa noche. Las cámaras te ubicaron a las 02:40 AM.", true),
        Message("SUJETO #84-B", "Esos muelles son públicos. Cualquiera pudo estar ahí. No prueban nada.", false),
        Message("DETECTIVE", "...", true)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(messages) { msg ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = if (msg.isDetective) Alignment.Start else Alignment.End
            ) {
                Text(
                    text = if (msg.isDetective) ">> ${msg.sender}" else "${msg.sender} <<",
                    color = if (msg.isDetective) NeonCyan else AggressiveRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(if (msg.isDetective) Color(0xFF101621) else Color(0xFF1A0F0F))
                        .border(
                            1.dp,
                            if (msg.isDetective) NeonCyan.copy(alpha = 0.5f) else AggressiveRed.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(10.dp)
                ) {
                    Text(text = msg.text, color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ApproachButton(title: String, preview: String, color: Color, icon: ImageVector) {
    Button(
        onClick = { /* Acción por realizar */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF101621)),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(
                    text = "\"$preview\"",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}