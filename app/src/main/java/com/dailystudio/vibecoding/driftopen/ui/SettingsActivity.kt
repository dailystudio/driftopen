package com.dailystudio.vibecoding.driftopen.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailystudio.vibecoding.driftopen.game.ScoreManager
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val scoreManager = remember { ScoreManager(context) }
            val currentDifficulty by scoreManager.difficultyFlow.collectAsState(initial = "easy")
            val scope = rememberCoroutineScope()

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    SettingsScreen(
                        currentDifficulty = currentDifficulty,
                        onDifficultyChange = { difficulty ->
                            scope.launch {
                                scoreManager.saveDifficulty(difficulty)
                            }
                        },
                        onExit = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    currentDifficulty: String,
    onDifficultyChange: (String) -> Unit,
    onExit: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SYSTEM CONFIG",
            color = Color.Cyan,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.Cyan)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "COMBAT DIFFICULTY",
            color = Color.Yellow,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        DifficultyOption(
            label = "EASY",
            description = "Lower threat density and slower projectiles. Standard score yields.",
            isSelected = currentDifficulty == "easy",
            color = Color.Green,
            onClick = { onDifficultyChange("easy") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        DifficultyOption(
            label = "NORMAL",
            description = "Balanced combat experience. 120% score bonus.",
            isSelected = currentDifficulty == "normal",
            color = Color.Cyan,
            onClick = { onDifficultyChange("normal") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        DifficultyOption(
            label = "HARD",
            description = "Intense hostile activity and rapid-fire threats. 150% score bonus.",
            isSelected = currentDifficulty == "hard",
            color = Color.Red,
            onClick = { onDifficultyChange("hard") }
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onExit,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(56.dp)
                .border(2.dp, Color.Cyan, MaterialTheme.shapes.medium),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("BACK TO BASE", color = Color.Cyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultyOption(
    label: String,
    description: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) color else color.copy(alpha = 0.2f)
    val backgroundColor = if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = color,
                        unselectedColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    color = color,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = description,
                color = if (isSelected) Color.White else Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 48.dp)
            )
        }
    }
}
