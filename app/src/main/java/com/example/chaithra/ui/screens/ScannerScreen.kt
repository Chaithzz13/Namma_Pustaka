package com.example.chaithra.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.chaithra.data.StudentEntity
import com.example.chaithra.viewmodel.LibraryViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun ScannerScreen(viewModel: LibraryViewModel, navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val scope = rememberCoroutineScope()

    // Authentication Guard: Only Librarians (Teachers) should be here
    val isTeacher by viewModel.isTeacherMode.collectAsState()
    if (!isTeacher) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Access Denied: Only the Librarian can issue or return books.")
        }
        return
    }

    // State variables
    var scannedCode by remember { mutableStateOf("") }
    val students by viewModel.allStudents.collectAsState()
    val allBooks by viewModel.allBooks.collectAsState()
    var isProcessing by remember { mutableStateOf(false) }

    // Student Selection State
    var selectedStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var showStudentDropdown by remember { mutableStateOf(false) }

    // Logic: Find the scanned book to check its current status
    val scannedBook = remember(scannedCode, allBooks) {
        allBooks.find { it.bookCode == scannedCode.trim() }
    }
    val isAlreadyIssued = scannedBook?.isIssued ?: false

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!hasCameraPermission) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Camera permission is required for the Library Assistant")
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        } else if (scannedCode.isEmpty()) {
            // CAMERA SCANNING UI
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val preview = Preview.Builder().build()
                        val selector = CameraSelector.DEFAULT_BACK_CAMERA
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                BarcodeScanning.getClient().process(image)
                                    .addOnSuccessListener { barcodes ->
                                        if (barcodes.isNotEmpty()) {
                                            barcodes[0].rawValue?.let { scannedCode = it }
                                        }
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            } else {
                                imageProxy.close()
                            }
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis)
                            preview.setSurfaceProvider(previewView.surfaceProvider)
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Scanner Overlay
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) {
                    Text(
                        "Point at Book QR/Barcode",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        } else {
            // RESULT UI - LIBRARIAN WORKFLOW
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isAlreadyIssued) Icons.Default.Refresh else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isAlreadyIssued) Color(0xFFFFA000) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                )

                Text(
                    text = if (isAlreadyIssued) "Confirm Return" else "Issue Book (15 Days)",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Book Info Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(scannedBook?.title ?: "Unknown Book", fontWeight = FontWeight.Bold)
                        Text("Code: $scannedCode", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // IF ISSUING: Require Student Selection
                if (!isAlreadyIssued) {
                    Text("Assign to Student:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.fillMaxWidth())
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        OutlinedButton(
                            onClick = { showStudentDropdown = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(selectedStudent?.name ?: "Select a Villager/Student")
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                        DropdownMenu(
                            expanded = showStudentDropdown,
                            onDismissRequest = { showStudentDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            students.forEach { student ->
                                DropdownMenuItem(
                                    text = { Text(student.name) },
                                    onClick = {
                                        selectedStudent = student
                                        showStudentDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Final Action Button
                Button(
                    onClick = {
                        if (!isProcessing && (isAlreadyIssued || selectedStudent != null)) {
                            isProcessing = true
                            scope.launch {
                                if (isAlreadyIssued) {
                                    viewModel.returnBookByLibrarian(scannedCode)
                                    Toast.makeText(context, "Book Returned successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    selectedStudent?.let {
                                        viewModel.issueBookByLibrarian(scannedCode, it)
                                        Toast.makeText(context, "Issued to ${it.name} for 15 days", Toast.LENGTH_LONG).show()
                                    }
                                }
                                scannedCode = ""
                                selectedStudent = null
                                isProcessing = false
                                navController.navigate("home")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isProcessing && (isAlreadyIssued || selectedStudent != null),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (isAlreadyIssued) "Confirm Return" else "Confirm 15-Day Issue")
                }

                TextButton(onClick = { scannedCode = ""; selectedStudent = null }) {
                    Text("Cancel and Scan Again")
                }
            }
        }
    }
}