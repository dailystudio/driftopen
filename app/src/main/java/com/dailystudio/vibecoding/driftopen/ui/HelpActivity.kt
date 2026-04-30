package com.dailystudio.vibecoding.driftopen.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailystudio.vibecoding.driftopen.game.models.AlienType
import com.dailystudio.vibecoding.driftopen.game.models.PowerUpType

class HelpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    HelpScreen(onExit = { finish() })
                }
            }
        }
    }
}

@Composable
fun HelpScreen(onExit: () -> Unit) {
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
            text = "COMMAND BRIEFING",
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        HelpSection(title = "OBJECTIVE", color = Color.Yellow) {
            Text(
                text = "Defend the galaxy against the invading DriftOpen fleet! Destroy all aliens to advance to the next sector.",
                color = Color.White,
                fontSize = 18.sp,
                lineHeight = 24.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        HelpSection(title = "CONTROLS & COMBAT", color = Color.Green) {
            Column {
                ControlItem("DRAG", "Move your ship. It follows slightly above your finger.")
                ControlItem("AUTO-FIRE", "Your ship fires automatically when enemies are present.")
                ControlItem("DOUBLE TAP", "Deploy a BOMB (if available) to clear the area.")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        HelpSection(title = "RESOURCES", color = Color.Cyan) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "EXTRA LIVES", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Earned every 10,000 points.", color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💣", fontSize = 24.sp, modifier = Modifier.size(30.dp), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "BOMBS", color = Color(0xFFFF4500), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Earned every 50,000 points. Destroys all visible enemies.", color = Color.White, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        HelpSection(title = "THREAT LEVEL", color = Color.Red) {
            Text(
                text = "Aliens will break formation to dive-bomb your ship.",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "• NORMAL (Red): 100 PTS",
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = "• FAST (Yellow): 100 PTS",
                color = Color.Yellow,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = "• BOSS (Magenta): 500 PTS",
                color = Color.Magenta,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = "• SUPERBOSS (Purple): 10,000 PTS",
                color = Color(0xFFAA00FF),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        HelpSection(title = "POWER-UPS", color = Color.Magenta) {
            Text(
                text = "Capture falling energy cores for temporary upgrades.",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            PowerUpItem(PowerUpType.SHIELD, "SHIELD", "Temporary protection.", Color.Cyan)
            PowerUpItem(PowerUpType.INVINCIBILITY, "INVINCIBILITY", "Absolute god mode.", Color(0xFFFFD700))
            PowerUpItem(PowerUpType.DOUBLE_FIRE, "DOUBLE", "Double fire power.", Color.Green)
            PowerUpItem(PowerUpType.RAPID_FIRE, "RAPID", "Increased fire rate.", Color.Yellow)
            PowerUpItem(PowerUpType.SPREAD_SHOT, "SPREAD", "Triple fire pattern.", Color.Magenta)
            PowerUpItem(PowerUpType.LASER_BEAM, "LASER", "Piercing continuous beam.", Color.Blue)
            PowerUpItem(PowerUpType.HOMING_MISSILES, "HOMING", "Missiles track targets.", Color.White)
            PowerUpItem(PowerUpType.EXPLOSIVE_BOMBS, "EXPLOSIVE", "Explosive rounds.", Color(0xFFFF4500))
        }
        
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
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun HelpSection(title: String, color: Color, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, color.copy(alpha = 0.5f), MaterialTheme.shapes.small)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = color,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
fun ControlItem(action: String, description: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$action:",
            color = Color.Green,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(100.dp),
            fontSize = 14.sp
        )
        Text(
            text = description,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
fun PowerUpItem(type: PowerUpType, name: String, desc: String, color: Color) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(30.dp)) {
            val r = size.width / 2
            drawCircle(color, radius = r)
            drawCircle(Color.White, radius = r * 0.7f, style = Stroke(width = 2f))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = name, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
        Text(text = desc, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
    }
}


