package com.dailystudio.vibecoding.driftopen.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailystudio.vibecoding.driftopen.game.CheatManager
import com.dailystudio.vibecoding.driftopen.game.models.CheatType

class CheatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    CheatConsole(onExit = { finish() })
                }
            }
        }
    }
}

@Composable
fun CheatConsole(onExit: () -> Unit) {
    var cheatCodeInput by remember { mutableStateOf("") }
    var cheatMessage by remember { mutableStateOf("ENTER CODE") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SECURITY CONSOLE",
            color = Color.Red,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = cheatMessage,
            color = Color.Yellow,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Masked input display
        Text(
            text = if (cheatCodeInput.isEmpty()) "" else "* ".repeat(cheatCodeInput.length),
            color = Color.Cyan,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(bottom = 24.dp).height(48.dp)
        )
        
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "X")
        )
        
        keys.forEach { row ->
            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                row.forEach { key ->
                    if (key.isNotEmpty()) {
                        Button(
                            onClick = { 
                                val newInput = cheatCodeInput + key
                                val validation = CheatManager.validateCode(newInput)
                                
                                if (validation != null) {
                                    val (cheatType, extra) = validation
                                    if (cheatType == CheatType.LEVEL_SELECT && extra != null) {
                                        CheatManager.selectLevel(extra)
                                    } else {
                                        CheatManager.activateCheat(cheatType)
                                    }
                                    cheatCodeInput = ""
                                    cheatMessage = "ACCESS GRANTED"
                                } else {
                                    val message = when {
                                        newInput.startsWith("X") && newInput.endsWith("X") && newInput.length > 1 -> {
                                            if (newInput.length == 4) "INVALID LEVEL" else "INVALID CODE"
                                        }
                                        newInput.length >= 5 && newInput.startsWith("X") -> "INVALID CODE"
                                        newInput.length >= 5 -> "INVALID CODE"
                                        else -> "TYPING..."
                                    }
                                    
                                    if (message != "TYPING...") {
                                        cheatCodeInput = ""
                                        cheatMessage = message
                                    } else {
                                        cheatCodeInput = if (newInput.length > 10) newInput.takeLast(10) else newInput
                                        cheatMessage = message
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(80.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray.copy(alpha = 0.3f)
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(key, fontSize = 32.sp, color = Color.White)
                        }
                    } else {
                        Box(modifier = Modifier.padding(horizontal = 8.dp).size(80.dp))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { 
                    cheatCodeInput = ""
                    cheatMessage = "CLEARED"
                },
                modifier = Modifier.padding(horizontal = 8.dp).width(120.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("CLEAR")
            }
            Button(
                onClick = { onExit() },
                modifier = Modifier.padding(horizontal = 8.dp).width(120.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000))
            ) {
                Text("EXIT")
            }
        }
    }
}
