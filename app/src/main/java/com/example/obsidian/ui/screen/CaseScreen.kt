package com.example.obsidian.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.obsidian.navigation.Screen

@Composable
fun CaseScreen(onTabSelected: (Screen) -> Unit) {
    val neonYellow = Color(0xFFFFF05A)
    val neonCyan = Color(0xFF00F0FF)
    val panelColor = Color(0xFF0B0F14)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF050816), Color(0xFF000000))
                )
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "CASE-714: OBSIDIAN FILE",
            color = neonYellow,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
        )

        Text(
            text = "ACTIVE INVESTIGATION",
            color = neonCyan,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = panelColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "CASE PROGRESS", color = neonYellow, style = MaterialTheme.typography.titleMedium)
                Text(text = "Clues Found: 0/10", color = Color.White)
                Text(text = "Suspects: 4", color = Color.White)
                Text(text = "Locations: 3", color = Color.White)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = panelColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "MISSION OBJECTIVE", color = neonYellow, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Find the truth behind the Obsidian Case by collecting clues, interrogating suspects, and connecting evidence.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Text(text = "QUICK ACTIONS", color = neonCyan, style = MaterialTheme.typography.titleMedium)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionCard(
                title = "Review Clues",
                icon = Icons.Default.Search,
                accent = neonCyan,
                onClick = { onTabSelected(Screen.Clue) }
            )
            ActionCard(
                title = "Open Evidence Board",
                icon = Icons.Default.Info,
                accent = neonYellow,
                onClick = { onTabSelected(Screen.Evidence) }
            )
            ActionCard(
                title = "Start Interrogation",
                icon = Icons.Default.Person,
                accent = neonCyan,
                onClick = { onTabSelected(Screen.Interrogation) }
            )
            ActionCard(
                title = "Investigate Map",
                icon = Icons.Default.LocationOn,
                accent = neonYellow,
                onClick = { onTabSelected(Screen.Map) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF101621), contentColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            Text(text = title, fontWeight = FontWeight.SemiBold)
        }
    }
}