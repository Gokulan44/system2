package com.systemmonitor.securityscan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.securityscan.analysis.ScanResult
import com.systemmonitor.securityscan.database.entity.FindingEntity
import com.systemmonitor.securityscan.presentation.components.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    result: ScanResult,
    onBack: () -> Unit,
    onViewFinding: (FindingEntity) -> Unit,
    onQuarantine: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Findings", "Signatures", "Permissions")
    val context = LocalContext.current

    // 1. Resolve Package Info Permissions list
    val permissions = remember(result.scanHistory.scanTarget) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(
                result.scanHistory.scanTarget,
                android.content.pm.PackageManager.GET_PERMISSIONS
            )
            packageInfo.requestedPermissions?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 2. Resolve SHA-256 Hash
    val sha256 = remember(result.scanHistory.scanTarget) {
        try {
            val appInfo = context.packageManager.getApplicationInfo(result.scanHistory.scanTarget, 0)
            val file = File(appInfo.sourceDir)
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            FileInputStream(file).use { fis ->
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    // 3. Resolve Certificate details
    val certInfo = remember(result.scanHistory.scanTarget) {
        try {
            val pm = context.packageManager
            val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                android.content.pm.PackageManager.GET_SIGNATURES
            }
            val packageInfo = pm.getPackageInfo(result.scanHistory.scanTarget, flags)
            val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            val signature = signatures?.firstOrNull()
            if (signature != null) {
                val certFactory = CertificateFactory.getInstance("X.509")
                val cert = certFactory.generateCertificate(signature.toByteArray().inputStream()) as X509Certificate
                com.systemmonitor.securityscan.staticscan.CertificateInfo(
                    subject = cert.subjectDN.name,
                    issuer = cert.issuerDN.name,
                    validFrom = cert.notBefore.toString(),
                    validTo = cert.notAfter.toString(),
                    serialNumber = cert.serialNumber.toString(16),
                    sigAlgName = cert.sigAlgName
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // 4. Resolve Hash Result Match
    var hashResult by remember { mutableStateOf<com.systemmonitor.securityscan.hash.HashResult>(com.systemmonitor.securityscan.hash.HashResult.Unknown) }
    val db = remember {
        androidx.room.Room.databaseBuilder(
            context,
            com.systemmonitor.securityscan.database.SecurityScanDatabase::class.java,
            "security_scanner_db"
        ).build()
    }
    LaunchedEffect(sha256) {
        if (sha256.isNotEmpty()) {
            val entity = withContext(Dispatchers.IO) {
                db.knownHashDao().getKnownHash(sha256)
            }
            hashResult = if (entity != null) {
                if (entity.type.uppercase() == "MALWARE") {
                    com.systemmonitor.securityscan.hash.HashResult.Malware(entity.appName, entity.threatName ?: "Generic Threat")
                } else {
                    com.systemmonitor.securityscan.hash.HashResult.Clean(entity.appName)
                }
            } else {
                com.systemmonitor.securityscan.hash.HashResult.Unknown
            }
        }
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF05070F))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit Report", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF080C16))
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.background(bgGradient)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            RiskScoreCard(
                score = result.scanHistory.score,
                verdict = result.scanHistory.verdict
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0F172A),
                contentColor = Color(0xFF00FFCC),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) Color(0xFF00FFCC) else Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content
            when (selectedTab) {
                0 -> { // Findings
                    if (result.findings.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Zero threats found! App signature is clean.",
                                color = Color(0xFF10B981),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            result.findings.forEach { finding ->
                                FindingCard(
                                    finding = finding,
                                    onClick = { onViewFinding(finding) }
                                )
                            }
                        }
                    }
                }
                1 -> { // Signatures
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HashResultCard(
                            sha256 = sha256,
                            hashResult = hashResult
                        )
                        CertificateCard(
                            info = certInfo
                        )
                    }
                }
                2 -> { // Permissions
                    PermissionRiskCard(
                        permissions = permissions
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quarantine Button if app has critical/high findings
            if (result.findings.isNotEmpty()) {
                ScanButton(
                    text = "Quarantine Suspicious Application",
                    onClick = onQuarantine
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = onBack,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Return to Dashboard", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
