package com.example.obsidian.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.obsidian.R
import com.example.obsidian.ui.theme.*


data class Suspect(
    val id: String,
    val name: String,
    val alias: String,
    val imageRes: Int,
    val tension: Float,
    val status: String,
    val bpm: Int,
    val caseNumber: String,
    val room: String
)

val suspects = listOf(
    Suspect("ALPHA-09", "ELIAS VANCE", "Principal Sospechoso", R.drawable.sus3, 0.74f, "HOSTIL", 112, "#2904", "SALA A - SECTOR 7"),
    Suspect("BETA-12", "MARA SOLIS", "Testigo Clave", R.drawable.susfemale1, 0.45f, "NERVIOSA", 95, "#2905", "SALA B - SECTOR 7"),
    Suspect("GAMMA-05", "VICTOR KANE", "Ex-Convicto", R.drawable.sus1, 0.88f, "AGRESIVO", 125, "#2906", "SALA C - SECTOR 3"),
    Suspect("DELTA-07", "LENA ROSS", "Informante", R.drawable.susfemale2, 0.30f, "COOPERATIVA", 78, "#2907", "SALA D - SECTOR 1")
)

@Composable
fun InterrogationScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    var selectedSuspect by remember { mutableStateOf(suspects[0]) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNoir)
            .padding(bottom = 16.dp)
    ) {
        // Header con Selector de Sospechosos
        SuspectSelector(
            selectedSuspect = selectedSuspect,
            onSuspectSelected = { selectedSuspect = it }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header del Expediente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OBSIDIAN CASE FILE",
                    color = CyanNeon,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )
                Text(
                    text = selectedSuspect.room,
                    color = CyanNeon.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Suspect Portrait and Info
            SuspectPortraitCard(selectedSuspect)

            // Conversation Log
            Box(modifier = Modifier.weight(1f)) {
                ConversationLog(selectedSuspect)
            }

            // Estrategia de Interrogatorio
            Text(
                text = "ESTRATEGIA DE INTERROGATORIO",
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StrategyButton(
                    title = "PASIVO",
                    subtitle = "Empatía / Calma",
                    color = CyanNeon,
                    icon = Icons.Default.Waves,
                    modifier = Modifier.weight(1f)
                )
                StrategyButton(
                    title = "NEUTRAL",
                    subtitle = "Lógica / Hechos",
                    color = Color.Gray,
                    icon = Icons.Default.Balance,
                    modifier = Modifier.weight(1f)
                )
                StrategyButton(
                    title = "AGRESIVO",
                    subtitle = "Presión / Amenaza",
                    color = AggressiveRed,
                    icon = Icons.Default.Bolt,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SuspectSelector(
    selectedSuspect: Suspect,
    onSuspectSelected: (Suspect) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(suspects) { suspect ->
            val isSelected = suspect == selectedSuspect
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSuspectSelected(suspect) }
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) CyanNeon else Color.DarkGray,
                            shape = CircleShape
                        )
                ) {
                    Image(
                        painter = painterResource(id = suspect.imageRes),
                        contentDescription = suspect.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = suspect.name.split(" ").first(),
                    color = if (isSelected) CyanNeon else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun SuspectPortraitCard(suspect: Suspect) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PanelColor),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen de fondo
            Image(
                painter = painterResource(id = suspect.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.6f)
            )

            // Gradiente para legibilidad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 100f
                        )
                    )
            )

            // Info overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = CyanNeon,
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text(
                            text = "SUJETO: ${suspect.id}",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "NIVEL DE TENSIÓN",
                            color = CyanNeon,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = suspect.tension,
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (suspect.tension > 0.8f) AggressiveRed else CyanNeon,
                                trackColor = Color.Black.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${(suspect.tension * 100).toInt()}%",
                                color = if (suspect.tension > 0.8f) AggressiveRed else CyanNeon,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = suspect.name,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${suspect.alias} - CASO ${suspect.caseNumber}",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

data class Message(val sender: String, val text: String, val isDetective: Boolean, val time: String)

@Composable
fun ConversationLog(suspect: Suspect) {
    val messages = remember(suspect) {
        listOf(
            Message("SISTEMA", "INTERROGATORIO INICIADO - 14:02:11", false, ""),
            Message("INVESTIGADOR", "Sabemos que estuviste en los muelles anoche, ${suspect.name.split(" ").first()}. Las cámaras no mienten. ¿Qué estabas buscando realmente?", true, "14:02:45"),
            Message(suspect.id, "Solo estaba tomando aire. No es un crimen caminar cerca del agua, ¿verdad oficial?", false, "14:03:12"),
            Message("INVESTIGADOR", "Nadie toma aire en el Almacén 4 a las tres de la mañana con un inhibidor de señal en el bolsillo. Danos algo útil.", true, "14:04:30")
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(messages) { msg ->
            if (msg.sender == "SISTEMA") {
                Column(modifier = Modifier.fillMaxWidth().alpha(0.6f)) {
                    Text(
                        text = "[${msg.sender}] ${msg.text}",
                        color = CyanNeon,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CyanNeon.copy(alpha = 0.2f)))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (msg.isDetective) Alignment.Start else Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        if (msg.isDetective) {
                            Text(text = "INVESTIGADOR", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = msg.time, color = Color.DarkGray, fontSize = 10.sp)
                        } else {
                            Text(text = msg.time, color = Color.DarkGray, fontSize = 10.sp)
                            Text(text = msg.sender, color = CyanNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(if (msg.isDetective) Color(0xFF1A1A1A) else CyanNeon.copy(alpha = 0.05f))
                            .border(
                                1.dp,
                                if (msg.isDetective) Color.White.copy(alpha = 0.1f) else CyanNeon.copy(alpha = 0.3f),
                                RoundedCornerShape(2.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = if (msg.isDetective) Color.LightGray else Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StrategyButton(title: String, subtitle: String, color: Color, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        onClick = { /* Acción TODO */ },
        modifier = modifier
            .height(68.dp),
        shape = RoundedCornerShape(4.dp),
        color = Color.Black,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title, 
                color = color, 
                fontWeight = FontWeight.Bold, 
                fontSize = 10.sp,
                lineHeight = 10.sp
            )
            Text(
                text = subtitle.uppercase(),
                color = color.copy(alpha = 0.6f),
                fontSize = 7.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                lineHeight = 8.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
