package com.dailystudio.vibecoding.driftopen.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dailystudio.vibecoding.driftopen.game.CollectionManager
import com.dailystudio.vibecoding.driftopen.game.models.AlienType

data class AlienEntry(
    val type: AlienType,
    val skinId: String,
    val name: String,
    val color: Color,
    val description: String
)

class CollectionActivity : ComponentActivity() {
    
    private val alienCollection = listOf(
        // Basic Units
        AlienEntry(
            AlienType.NORMAL, "normal", "Zyrion Drone", Color.Red,
            "The backbone of the DriftOpen fleet. These mass-produced drones overwhelm planetary defenses through relentless swarming tactics and sheer numbers."
        ),
        AlienEntry(
            AlienType.FAST, "fast", "Solar Dart", Color.Yellow,
            "Elite interceptors built for speed. Their diamond hulls allow them to perform extreme maneuvers, making them the most elusive targets in the fleet."
        ),

        // 10 Mini-Bosses (The Gauntlet Gatekeepers)
        AlienEntry(AlienType.BOSS, "boss_0", "The Crab of Lerna", Color.Magenta, "A giant, heavily armored crab sent to distract. It has massive defense but is exposed if flanked correctly."),
        AlienEntry(AlienType.BOSS, "boss_1", "Nessus", Color.Magenta, "A treacherous, fast, hit-and-run centaur archer who fires from a distance. Requires perfect timing to close the gap."),
        AlienEntry(AlienType.BOSS, "boss_2", "Eurytion", Color.Magenta, "A brutal, giant herdsman wielding a massive club. Slow but deals devastating area-of-effect shockwave damage."),
        AlienEntry(AlienType.BOSS, "boss_3", "Aella", Color.Magenta, "The vanguard of the Amazons. She wields a massive double-sided battleaxe and spins like a tornado, forcing you to master dodging."),
        AlienEntry(AlienType.BOSS, "boss_4", "Busiris", Color.Magenta, "A mad pharaoh protected by a shield of dark magic. You must break his magical defenses before dealing physical damage."),
        AlienEntry(AlienType.BOSS, "boss_5", "Eryx", Color.Magenta, "The iron-fisted king and champion boxer. A pure test of weaving, dodging, and counter-punching in close combat."),
        AlienEntry(AlienType.BOSS, "boss_6", "Syleus", Color.Magenta, "A cruel taskmaster who attacks with a massive magical hoe, uprooting the earth and creating hazardous terrain."),
        AlienEntry(AlienType.BOSS, "boss_7", "Lityerses", Color.Magenta, "The demonic reaper. A brutal swordsman who dual-wields curved sickles and executes incredibly fast combo attacks."),
        AlienEntry(AlienType.BOSS, "boss_8", "The Cercopes Duo", Color.Magenta, "Two mischievous trickster spirits that attack in tandem. A chaotic encounter requiring high situational awareness."),
        AlienEntry(AlienType.BOSS, "boss_9", "The Alpha Harpy", Color.Magenta, "A screeching scavenger that disorients targets with sonic screams, temporarily scrambling their combat logic."),

        // 20 Main Bosses (The Epic Encounters - Superbosses)
        AlienEntry(AlienType.SUPERBOSS, "superboss_0", "The Nemean Lion", Color.White, "A colossal beast whose golden hide is impervious to mortal weapons. Requires overwhelming blunt force to defeat."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_1", "The Lernaean Hydra", Color.White, "A massive serpentine terror. Severing a head causes more to sprout unless the wounds are immediately cauterized."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_2", "The Erymanthian Boar", Color.White, "A gigantic, frenzied boar that charges with the force of a battering ram, devastating everything in its path."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_3", "The Ceryneian Hind", Color.White, "A sacred stag with golden antlers, capable of outrunning lasers. An endurance boss demanding precise trap-setting."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_4", "The Stymphalian Matriarch", Color.White, "Queen of the bronze sky. She fires razor-sharp metallic feathers like a machine gun and dominates the airspace."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_5", "The Cretan Bull", Color.White, "A towering, muscular beast exhaling scorching flames. Forces the player into a brutal, close-quarters matador-style fight."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_6", "King Diomedes & The Mares", Color.White, "A chaotic multi-target encounter against a cruel king and his four demonic, blood-thirsty horses."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_7", "Hippolyta", Color.White, "Queen of the Amazons and a martial master. She possesses perfect combat techniques and seamlessly switches weapons."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_8", "Geryon", Color.White, "A terrifying three-bodied warlord wielding three different weapon types simultaneously, requiring you to read complex overlapping attacks."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_9", "Ladon", Color.White, "The hundred-headed sentinel dragon. Colossal in size, it spews different elemental breaths from its many maws."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_10", "Cerberus", Color.White, "The hound of Hades. A three-headed demonic watchdog that utilizes soul-draining mechanics in a pitch-black arena."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_11", "Antaeus", Color.White, "The Son of Earth. A colossal giant who is completely invincible as long as he remains grounded."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_12", "Achelous", Color.White, "The shapeshifting river god. Fluid and unpredictable, seamlessly transforming between forms during combat."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_13", "The Caucasian Eagle", Color.White, "A gargantuan demonic bird made of storm clouds. Utilizes heavy windbox mechanics to blow you away while dive-bombing."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_14", "Cetus of Troy", Color.White, "The abyssal leviathan. A gargantuan sea monster whose weak point is hidden deep within its armored shell."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_15", "Cacus", Color.White, "The fire-breathing thief. A hideous giant who breathes thick smoke that obscures vision, turning the fight into a deadly game of hide-and-seek."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_16", "Alcyoneus", Color.White, "The immortal Goliath and king of the Giants. Towering and seemingly unkillable, requiring specialized tactics to stagger."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_17", "Porphyrion", Color.White, "The Anti-Zeus. A massive giant radiating dark lightning, designed to kill the gods themselves."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_18", "Orthrus", Color.White, "The two-headed shadow. A hyper-aggressive hound that splits into two separate entities, flanking relentlessly."),
        AlienEntry(AlienType.SUPERBOSS, "superboss_19", "Echidna", Color.White, "The Mother of All Monsters. The ultimate culmination of the game, casting ancient magic and summoning lesser horrors.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val collectionManager = remember { CollectionManager(context) }
            val unlockedAliens by collectionManager.unlockedAliensFlow.collectAsState(initial = emptySet())

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    CollectionScreen(
                        collection = alienCollection,
                        unlockedAliens = unlockedAliens,
                        onExit = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun CollectionScreen(
    collection: List<AlienEntry>,
    unlockedAliens: Set<String>,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "ALIEN ARCHIVES",
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(collection) { alien ->
                val isUnlocked = unlockedAliens.contains(alien.skinId)
                AlienCollectionItem(alien, isUnlocked)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AlienCollectionItem(alien: AlienEntry, isUnlocked: Boolean) {
    val displayColor = if (isUnlocked) alien.color else Color.Gray
    val displayName = if (isUnlocked) alien.name else "???"
    val displayDesc = if (isUnlocked) alien.description else "Intelligence indicates a hostile entity of unknown origin detected in this sector. Proceed with caution to gather tactical data."
    val displayClassification = if (isUnlocked) alien.type.name else "UNKNOWN"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = if (isUnlocked) Color(0xFF1A1A1A) else Color(0xFF0D0D0D)),
        border = androidx.compose.foundation.BorderStroke(1.dp, displayColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Alien Portrait
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Black, MaterialTheme.shapes.small)
                    .border(1.dp, if (isUnlocked) Color.Gray else Color.DarkGray, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    CollectionRetroGraphic(
                        type = alien.type,
                        skinId = alien.skinId,
                        color = alien.color,
                        modifier = Modifier.size(60.dp)
                    )
                } else {
                    Text(
                        text = "?",
                        color = Color.DarkGray,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = displayName,
                    color = displayColor,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Classification: $displayClassification",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = displayDesc,
                    color = if (isUnlocked) Color.White else Color.DarkGray,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun CollectionRetroGraphic(
    type: AlienType,
    skinId: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w == 0f || h == 0f) return@Canvas
        
        val skins = GamePaths.alienSkins[skinId] ?: GamePaths.alienSkins["default"]!!
        val path = skins[type] ?: skins[AlienType.NORMAL]!!
        
        translate(w / 2f, h / 2f) {
            scale(w * 0.8f, h * 0.8f, pivot = Offset.Zero) {
                // Draw Body
                drawPath(path, color = color)
                if (type != AlienType.SUPERBOSS) {
                    drawPath(path, color = Color.White.copy(alpha = 0.5f), style = Stroke(width = 2f / (w * 0.8f)))
                }
                
                // Eyes (Inside the same transformation to match getAlienBitmap)
                val eyeOffset = 0.15f
                val eyeSize = 0.1f
                val eyeY = -0.1f
                
                drawCircle(Color.White, radius = eyeSize, center = Offset(-eyeOffset, eyeY))
                drawCircle(Color.White, radius = eyeSize, center = Offset(eyeOffset, eyeY))
                drawCircle(Color.Black, radius = eyeSize * 0.5f, center = Offset(-eyeOffset, eyeY))
                drawCircle(Color.Black, radius = eyeSize * 0.5f, center = Offset(eyeOffset, eyeY))
            }
        }
    }
}