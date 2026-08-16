package com.systemmonitor.vault.presentation

import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.systemmonitor.vault.database.VaultAuditEntity
import com.systemmonitor.vault.model.VaultFile
import com.systemmonitor.vault.model.VaultFileType
import com.systemmonitor.vault.model.VaultFolder
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    onBackClick: () -> Unit,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    // Real Lock-on-Background lifecycle observer
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.lockVault()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Observe and display errors
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF060B15),
                        Color(0xFF0B132B),
                        Color(0xFF04070F)
                    )
                )
            )
    ) {
        when {
            !state.isSetup -> {
                VaultSetupScreen(
                    onSetup = { pin -> viewModel.setupVault(pin) },
                    onBackClick = onBackClick
                )
            }
            state.isLocked -> {
                VaultLockScreen(
                    onUnlock = { pin -> viewModel.unlockVault(pin) },
                    onReset = { viewModel.resetVault() },
                    onBackClick = onBackClick
                )
            }
            else -> {
                VaultHomeScreen(
                    state = state,
                    viewModel = viewModel,
                    onBackClick = onBackClick
                )
            }
        }

        // Secure file viewer overlay
        if (state.viewingFile != null && state.tempViewingFile != null) {
            SecureFileViewerDialog(
                file = state.viewingFile!!,
                tempFile = state.tempViewingFile!!,
                onClose = { viewModel.closeFileViewer() }
            )
        }
    }
}

@Composable
fun VaultSetupScreen(
    onSetup: (String) -> Boolean,
    onBackClick: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirmStage by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFEC4899).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFFEC4899),
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Setup Secure Vault",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isConfirmStage) "Confirm your 4-digit PIN" else "Create a 4-digit PIN to secure your vault",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Display Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val length = if (isConfirmStage) confirmPin.length else pin.length
            for (i in 1..4) {
                val filled = i <= length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (filled) Color(0xFFEC4899) else Color(0xFF334155))
                        .border(
                            1.dp,
                            if (filled) Color(0xFFEC4899) else Color(0xFF475569),
                            CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Grid PIN Pad
        PinPad(
            onDigitClick = { digit ->
                if (!isConfirmStage) {
                    if (pin.length < 4) pin += digit
                    if (pin.length == 4) {
                        isConfirmStage = true
                    }
                } else {
                    if (confirmPin.length < 4) confirmPin += digit
                    if (confirmPin.length == 4) {
                        if (pin == confirmPin) {
                            val success = onSetup(pin)
                            if (!success) {
                                Toast.makeText(context, "Failed to setup vault. PIN must be 4 digits.", Toast.LENGTH_SHORT).show()
                                pin = ""
                                confirmPin = ""
                                isConfirmStage = false
                            }
                        } else {
                            Toast.makeText(context, "PINs do not match. Start over.", Toast.LENGTH_SHORT).show()
                            pin = ""
                            confirmPin = ""
                            isConfirmStage = false
                        }
                    }
                }
            },
            onDeleteClick = {
                if (!isConfirmStage) {
                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                } else {
                    if (confirmPin.isNotEmpty()) {
                        confirmPin = confirmPin.dropLast(1)
                    } else {
                        isConfirmStage = false
                        pin = pin.dropLast(1)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onBackClick) {
            Text("Cancel", color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun VaultLockScreen(
    onUnlock: (String) -> Boolean,
    onReset: () -> Unit,
    onBackClick: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Vault Locked",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter your 4-digit PIN to access encrypted files",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..4) {
                val filled = i <= pin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (filled) Color(0xFF00E5FF) else Color(0xFF334155))
                        .border(
                            1.dp,
                            if (filled) Color(0xFF00E5FF) else Color(0xFF475569),
                            CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Grid PIN Pad
        PinPad(
            onDigitClick = { digit ->
                if (pin.length < 4) pin += digit
                if (pin.length == 4) {
                    val unlocked = onUnlock(pin)
                    if (!unlocked) {
                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                        pin = ""
                    }
                }
            },
            onDeleteClick = {
                if (pin.isNotEmpty()) pin = pin.dropLast(1)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            TextButton(onClick = onBackClick) {
                Text("Exit", color = Color(0xFF94A3B8))
            }
            TextButton(onClick = { showResetDialog = true }) {
                Text("Reset Vault", color = Color(0xFFEF4444))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Secure Vault?", fontWeight = FontWeight.Bold) },
            text = { Text("WARNING: Resetting the vault will erase the master key and you will lose access to all encrypted files forever. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onReset()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PinPad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    val digits = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        digits.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                row.forEach { char ->
                    if (char.isEmpty()) {
                        Spacer(modifier = Modifier.size(64.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                                .border(1.dp, Color(0xFF334155), CircleShape)
                                .clickable {
                                    if (char == "DEL") onDeleteClick() else onDigitClick(char)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (char == "DEL") {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = char,
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultHomeScreen(
    state: VaultUiState,
    viewModel: VaultViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("ALL") }

    // Dialog sheets state
    var activeFolderMenu by remember { mutableStateOf<VaultFolder?>(null) }
    var activeFileMenu by remember { mutableStateOf<VaultFile?>(null) }
    
    var renameTargetFolder by remember { mutableStateOf<VaultFolder?>(null) }
    var renameTargetFile by remember { mutableStateOf<VaultFile?>(null) }

    // Activity result launcher to import file
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val contentResolver = context.contentResolver
            var name = "Imported_File"
            var mime = "application/octet-stream"
            contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    name = cursor.getString(nameIndex)
                }
            }
            contentResolver.getType(it)?.let { type -> mime = type }
            viewModel.importFile(it)
        }
    }

    // Activity result launcher to export file
    var exportFileId by remember { mutableStateOf<String?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri: Uri? ->
        if (uri != null && exportFileId != null) {
            viewModel.exportFile(
                fileId = exportFileId!!,
                outputUri = uri,
                onSuccess = {
                    Toast.makeText(context, "File exported successfully", Toast.LENGTH_SHORT).show()
                },
                onFailure = { err ->
                    Toast.makeText(context, "Export failed: $err", Toast.LENGTH_LONG).show()
                }
            )
        }
        exportFileId = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Secure Vault", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.lockVault() }) {
                        Icon(Icons.Default.LockOpen, contentDescription = "Lock", tint = Color(0xFF00E5FF))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (selectedCategory != "LOGS") {
                FloatingActionButton(
                    onClick = { /* Expandable FAB trigger */ },
                    containerColor = Color(0xFFEC4899),
                    contentColor = Color.White
                ) {
                    var fabExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { fabExpanded = !fabExpanded }) {
                        Icon(
                            imageVector = if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Actions"
                        )
                    }

                    DropdownMenu(
                        expanded = fabExpanded,
                        onDismissRequest = { fabExpanded = false },
                        modifier = Modifier.background(Color(0xFF0F172A))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Import File", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.UploadFile, contentDescription = null, tint = Color(0xFF00E5FF)) },
                            onClick = {
                                fabExpanded = false
                                importLauncher.launch("*/*")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("New Folder", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = Color(0xFF10B981)) },
                            onClick = {
                                fabExpanded = false
                                showCreateFolderDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Run Integrity Scan", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFFF59E0B)) },
                            onClick = {
                                fabExpanded = false
                                viewModel.runIntegrityCheck()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Backup Vault Archive", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.Backup, contentDescription = null, tint = Color(0xFF8B5CF6)) },
                            onClick = {
                                fabExpanded = false
                                viewModel.createVaultBackup()
                            }
                        )
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Category Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf("ALL", "IMAGES", "VIDEOS", "DOCUMENTS", "LOGS")
                categories.forEach { cat ->
                    val selected = selectedCategory == cat
                    val bg = if (selected) Color(0xFFEC4899) else Color(0xFF1E293B).copy(alpha = 0.5f)
                    val fg = if (selected) Color.White else Color(0xFF94A3B8)
                    
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .clickable { selectedCategory = cat }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            color = fg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Breadcrumbs (only show if not logs)
            if (selectedCategory != "LOGS") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Home",
                        color = if (state.currentFolderId == null) Color.White else Color(0xFFEC4899),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { viewModel.navigateToFolder(null) }
                    )
                    state.breadcrumbs.forEach { crumb ->
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = crumb.name,
                            color = if (state.currentFolderId == crumb.id) Color.White else Color(0xFFEC4899),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { viewModel.navigateToFolder(crumb.id) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.isImporting) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFEC4899))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Encrypting & importing file...", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            // View rendering logic
            if (selectedCategory == "LOGS") {
                val audits by viewModel.auditLogs.collectAsState()
                AuditLogsView(audits = audits)
            } else {
                val filteredFiles = remember(state.files, selectedCategory) {
                    when (selectedCategory) {
                        "IMAGES" -> state.files.filter { it.fileType == VaultFileType.IMAGE }
                        "VIDEOS" -> state.files.filter { it.fileType == VaultFileType.VIDEO }
                        "DOCUMENTS" -> state.files.filter { it.fileType == VaultFileType.DOCUMENT }
                        else -> state.files
                    }
                }

                if (state.folders.isEmpty() && filteredFiles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "This folder is empty",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // Folders
                        items(state.folders) { folder ->
                            FolderGridCard(
                                folder = folder,
                                onClick = { viewModel.navigateToFolder(folder.id) },
                                onMenuClick = { activeFolderMenu = folder }
                            )
                        }

                        // Files
                        items(filteredFiles) { file ->
                            FileGridCard(
                                file = file,
                                onClick = { viewModel.openFileViewer(file) },
                                onMenuClick = { activeFileMenu = file }
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        var folderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Create Folder", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (folderName.isNotEmpty()) {
                            viewModel.createFolder(folderName)
                            showCreateFolderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Folder Actions Menu
    activeFolderMenu?.let { folder ->
        AlertDialog(
            onDismissRequest = { activeFolderMenu = null },
            title = { Text(folder.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            activeFolderMenu = null
                            renameTargetFolder = folder
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Rename Folder", color = Color.White)
                        }
                    }
                    TextButton(
                        onClick = {
                            activeFolderMenu = null
                            viewModel.deleteFolder(folder.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete Folder", color = Color(0xFFEF4444))
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // File Actions Menu
    activeFileMenu?.let { file ->
        AlertDialog(
            onDismissRequest = { activeFileMenu = null },
            title = { Text(file.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            activeFileMenu = null
                            exportFileId = file.id
                            exportLauncher.launch(file.name)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Export (Decrypt) File", color = Color.White)
                        }
                    }
                    TextButton(
                        onClick = {
                            activeFileMenu = null
                            renameTargetFile = file
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Rename File", color = Color.White)
                        }
                    }
                    TextButton(
                        onClick = {
                            activeFileMenu = null
                            viewModel.deleteFile(file.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete File", color = Color(0xFFEF4444))
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Folder Rename Dialog
    renameTargetFolder?.let { folder ->
        var tempName by remember { mutableStateOf(folder.name) }
        AlertDialog(
            onDismissRequest = { renameTargetFolder = null },
            title = { Text("Rename Folder") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotEmpty()) {
                            viewModel.renameFolder(folder.id, tempName)
                            renameTargetFolder = null
                        }
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTargetFolder = null }) { Text("Cancel") }
            }
        )
    }

    // File Rename Dialog
    renameTargetFile?.let { file ->
        var tempName by remember { mutableStateOf(file.name) }
        AlertDialog(
            onDismissRequest = { renameTargetFile = null },
            title = { Text("Rename File") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotEmpty()) {
                            viewModel.renameFile(file.id, tempName)
                            renameTargetFile = null
                        }
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTargetFile = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun FolderGridCard(
    folder: VaultFolder,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = folder.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Folder",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color(0xFF64748B))
            }
        }
    }
}

@Composable
fun FileGridCard(
    file: VaultFile,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val (icon, color) = when (file.fileType) {
        VaultFileType.IMAGE -> Pair(Icons.Default.Image, Color(0xFF10B981))
        VaultFileType.VIDEO -> Pair(Icons.Default.VideoFile, Color(0xFF8B5CF6))
        VaultFileType.AUDIO -> Pair(Icons.Default.AudioFile, Color(0xFFF59E0B))
        VaultFileType.DOCUMENT -> Pair(Icons.Default.Description, Color(0xFF0284C7))
        VaultFileType.ARCHIVE -> Pair(Icons.Default.FolderZip, Color(0xFFEC4899))
        VaultFileType.APK -> Pair(Icons.Default.Android, Color(0xFF22C55E))
        VaultFileType.OTHER -> Pair(Icons.Default.InsertDriveFile, Color(0xFF64748B))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = file.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val displaySize = remember(file.sizeBytes) {
                    val kb = file.sizeBytes / 1024
                    if (kb > 1024) "${kb / 1024} MB" else "$kb KB"
                }
                Text(
                    text = displaySize,
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color(0xFF64748B))
            }
        }
    }
}

@Composable
fun AuditLogsView(audits: List<VaultAuditEntity>) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Vault Lock/Unlock Security Audit Analysis",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        audits.forEach { audit ->
            val icon = when (audit.action) {
                "LOCK" -> Icons.Default.Lock
                "UNLOCK" -> Icons.Default.LockOpen
                "UNLOCK_FAILED" -> Icons.Default.Warning
                "SETUP" -> Icons.Default.Verified
                "FOLDER_CREATE" -> Icons.Default.CreateNewFolder
                "FILE_IMPORT" -> Icons.Default.UploadFile
                "FILE_EXPORT" -> Icons.Default.Download
                "FILE_DELETE", "FOLDER_DELETE" -> Icons.Default.Delete
                else -> Icons.Default.Security
            }
            
            val iconColor = when (audit.action) {
                "UNLOCK" -> Color(0xFF10B981) // Green
                "UNLOCK_FAILED" -> Color(0xFFEF4444) // Red
                "LOCK" -> Color(0xFF00E5FF) // Cyan
                "SETUP" -> Color(0xFF8B5CF6) // Purple
                "FILE_DELETE", "FOLDER_DELETE" -> Color(0xFFEF4444)
                else -> Color(0xFF94A3B8)
            }

            val formattedTime = remember(audit.timestamp) {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                sdf.format(java.util.Date(audit.timestamp))
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = audit.action,
                                color = iconColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = formattedTime,
                                color = Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = audit.details,
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureFileViewerDialog(
    file: VaultFile,
    tempFile: File,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(file.name, color = Color.White, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF030712))
                )
            },
            containerColor = Color(0xFF030712)
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                when (file.fileType) {
                    VaultFileType.IMAGE -> {
                        val bitmap = remember(tempFile) {
                            try {
                                BitmapFactory.decodeFile(tempFile.absolutePath)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Decrypted Image",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("Failed to decode decrypted image", color = Color.White)
                        }
                    }
                    VaultFileType.DOCUMENT -> {
                        var textContent by remember { mutableStateOf<String?>(null) }
                        LaunchedEffect(tempFile) {
                            try {
                                textContent = tempFile.readText(Charsets.UTF_8)
                            } catch (e: Exception) {
                                textContent = "[Unable to read document as text: binary file or encoding mismatch]"
                            }
                        }
                        
                        if (textContent != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = textContent!!,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            CircularProgressIndicator(color = Color(0xFFEC4899))
                        }
                    }
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Secure File Preview",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Preview is not supported for ${file.fileType} in the sandboxed viewer. Please export the file to view or execute it.",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
