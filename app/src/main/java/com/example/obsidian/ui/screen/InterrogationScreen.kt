package com.example.obsidian.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.obsidian.R
import com.example.obsidian.data.model.*
import com.example.obsidian.ui.theme.*
import com.example.obsidian.ui.viewmodel.GameViewModel

@Composable
fun InterrogationScreen(
    navController: NavHostController, 
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val allMessages by viewModel.messages.collectAsStateWithLifecycle()
    val isGenerating = viewModel.isGenerating
    
    val suspects = gameState.currentCase?.suspects ?: emptyList()
    
    if (isGenerating) {
        Box(modifier = modifier.fillMaxSize().background(BackgroundNoir), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = CyanNeon)
                Spacer(modifier = Modifier.height(16.dp))
                Text("GENERANDO EXPEDIENTE...", color = CyanNeon, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    if (suspects.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay expediente activo", color = Color.Gray)
        }
        return
    }

    if (gameState.explorationCount < 1) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundNoir)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = AggressiveRed,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SALA DE INTERROGATORIOS BLOQUEADA",
                    color = AggressiveRed,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Primero debes investigar el Puerto y encontrar al menos una pista antes de interrogar a los sospechosos.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { navController.navigate(com.example.obsidian.navigation.Screen.Map.route) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("IR AL MAPA SCANNER", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    var selectedSuspectId by remember(suspects) { mutableStateOf(suspects.firstOrNull()?.id ?: "") }
    val selectedSuspect = suspects.find { it.id == selectedSuspectId }
    val currentMessages = allMessages[selectedSuspectId] ?: emptyList()
    var userText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNoir)
    ) {
        // Selector de Sospechosos
        SuspectSelectorRow(suspects, selectedSuspectId) { selectedSuspectId = it }

        selectedSuspect?.let { suspect ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InterrogationHeader(suspect)

                // Portrait y Estado
                SuspectStatusCard(suspect)

                // Registro de Conversación
                Box(modifier = Modifier.weight(1f)) {
                    ConversationHistory(currentMessages, suspect.name)
                }

                // NUEVA MECÁNICA: Preguntas Sugeridas
                Text(
                    text = "LÍNEAS DE INTERROGATORIO",
                    color = CyanNeon,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                ) {
                    items(suspect.availableQuestions) { question ->
                        QuestionChip(question) {
                            viewModel.onQuestionSelected(suspect, question)
                        }
                    }
                }

                // Input Manual
                ManualInputRow(userText, { userText = it }) {
                    if (userText.isNotBlank()) {
                        viewModel.sendMessage(suspect, userText)
                        userText = ""
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun QuestionChip(question: Question, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Black,
        border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = question.text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun InterrogationHeader(suspect: Suspect) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = suspect.name,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
        )
        Column(horizontalAlignment = Alignment.End) {
            Text("CONFIANZA", color = CyanNeon, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
                progress = { suspect.trustLevel / 100f },
                modifier = Modifier.width(80.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = if (suspect.trustLevel < 30) AggressiveRed else CyanNeon,
                trackColor = Color.Black.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SuspectStatusCard(suspect: Suspect) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PanelColor),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().height(100.dp).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = if (suspect.imageId != 0) suspect.imageId else R.drawable.sus1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.4f)
            )
            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center) {
                Text(text = "RASGOS: ${suspect.personality}", color = CyanNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "COARTADA: ${suspect.alibi}", color = Color.LightGray, fontSize = 11.sp, lineHeight = 14.sp)
            }
        }
    }
}

@Composable
fun ConversationHistory(messages: List<InterrogationMessage>, suspectName: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(messages) { msg ->
            val isDetective = msg.isDetective
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = if (isDetective) Alignment.Start else Alignment.End
            ) {
                Surface(
                    color = if (isDetective) Color(0xFF1A1A1A) else CyanNeon.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, if (isDetective) Color.White.copy(alpha = 0.1f) else CyanNeon.copy(alpha = 0.3f))
                ) {
                    Text(text = msg.text, color = Color.White, modifier = Modifier.padding(8.dp), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun SuspectSelectorRow(suspects: List<Suspect>, selectedId: String, onSelect: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.5f)).padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(suspects) { suspect ->
            val isSelected = suspect.id == selectedId
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSelect(suspect.id) }) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).border(if (isSelected) 2.dp else 1.dp, if (isSelected) CyanNeon else Color.DarkGray, CircleShape)) {
                    Image(painter = painterResource(id = if (suspect.imageId != 0) suspect.imageId else R.drawable.sus1), contentDescription = null, contentScale = ContentScale.Crop)
                }
                Text(text = suspect.name.split(" ").first(), color = if (isSelected) CyanNeon else Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun ManualInputRow(text: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Escribir pregunta libre...", color = Color.Gray, fontSize = 13.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Black,
                unfocusedContainerColor = Color.Black,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            modifier = Modifier.background(CyanNeon, RoundedCornerShape(4.dp))
        ) {
            Icon(Icons.Default.Waves, contentDescription = "Enviar", tint = Color.Black)
        }
    }
}
