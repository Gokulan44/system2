package com.systemmonitor.share

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VaultShareActivity : ComponentActivity() {

    private val viewModel: ShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val parser = ShareIntentParser()
        val uris = parser.parseShareIntent(intent)
        if (uris.isEmpty()) {
            Toast.makeText(this, "No media found in share intent.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.initUris(uris)

        setContent {
            MaterialTheme {
                ShareScreenContent(
                    viewModel = viewModel,
                    onClose = { finish() }
                )
            }
        }
    }
}

@Composable
fun ShareScreenContent(
    viewModel: ShareViewModel,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF020617),
            Color(0xFF0F172A),
            Color(0xFF020617)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Share to Vault",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            when {
                uiState.importFinished -> {
                    Text(
                        text = "Import Finished",
                        color = Color(0xFF00E5FF),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.importSummary,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Text("Done", color = Color.White)
                    }
                }
                uiState.isImporting -> {
                    CircularProgressIndicator(color = Color(0xFFEC4899))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.importProgress,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                uiState.isAuthenticated -> {
                    CircularProgressIndicator(color = Color(0xFFEC4899))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Authenticating success. Initializing Import...", color = Color.White)
                }
                else -> {
                    var enteredPin by remember { mutableStateOf("") }
                    
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = Color(0xFFEC4899),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Vault is Locked",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Enter your Vault PIN to securely import ${uiState.filesToImport.size} files.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        for (i in 0 until 4) {
                            val dotColor = if (i < enteredPin.length) Color(0xFFEC4899) else Color(0xFF334155)
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(dotColor, shape = RoundedCornerShape(8.dp))
                            )
                        }
                    }

                    if (uiState.authError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.authError!!, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val numbers = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("", "0", "delete")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        numbers.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                row.forEach { buttonText ->
                                    if (buttonText.isEmpty()) {
                                        Box(modifier = Modifier.size(64.dp))
                                    } else {
                                        Surface(
                                            onClick = {
                                                when (buttonText) {
                                                    "delete" -> {
                                                        if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                                    }
                                                    else -> {
                                                        if (enteredPin.length < 4) {
                                                            enteredPin += buttonText
                                                            if (enteredPin.length == 4) {
                                                                viewModel.authenticate(enteredPin)
                                                                enteredPin = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .size(64.dp)
                                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(32.dp)),
                                            shape = RoundedCornerShape(32.dp),
                                            color = Color(0xFF1E293B).copy(alpha = 0.5f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (buttonText == "delete") {
                                                    Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.White)
                                                } else {
                                                    Text(buttonText, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
