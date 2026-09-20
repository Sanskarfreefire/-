package com.tools.systeminfo7

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tools.systeminfo7.service.OverlayService
import com.tools.systeminfo7.ui.theme.Theme

class MainActivity : ComponentActivity() {

    companion object {
        init {
            System.loadLibrary("sr_mods_engine")
        }
    }

    external fun stringFromJNI(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        jniStatus = stringFromJNI(),
                        onRequestOverlayPermission = {
                            if (!Settings.canDrawOverlays(this)) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:$packageName")
                                )
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "Overlay Permission Already Granted", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onStartService = {
                            if (Settings.canDrawOverlays(this)) {
                                val intent = Intent(this, OverlayService::class.java)
                                startService(intent)
                                Toast.makeText(this, "𝐒𝐀𝐍𝐒𝐊𝐀𝐑 𝐌𝐎𝐃𝐒 Service Started", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Please Grant Overlay Permission First", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    jniStatus: String,
    onRequestOverlayPermission: () -> Unit,
    onStartService: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "𝐒𝐀𝐍𝐒𝐊𝐀𝐑 𝐌𝐎𝐃𝐒",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEF4444)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Non-Root ESP & Aimbot Panel",
            fontSize = 16.sp,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Engine Status:",
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = jniStatus,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestOverlayPermission,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
        ) {
            Text("1. Grant Overlay Permission")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onStartService,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
        ) {
            Text("2. Start 𝐒𝐀𝐍𝐒𝐊𝐀𝐑 𝐌𝐎𝐃𝐒 Overlay")
        }
    }
}
