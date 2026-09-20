package com.example.aiwardrobe.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun AddClothingScreen(
    onAnalyze: (Uri) -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var capturedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var imageCapture by remember {
        mutableStateOf<ImageCapture?>(null)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasCameraPermission = granted
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    //image privew

    if (capturedImageUri != null) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Clothing Preview"
            )

            Image(
                painter = rememberAsyncImagePainter(
                    capturedImageUri
                ),
                contentDescription = "Captured clothing",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentScale = ContentScale.Fit
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Button(
                    onClick = {
                        capturedImageUri = null
                    }
                ) {
                    Text("Retake")
                }

                Button(
                    onClick = {
                        capturedImageUri?.let { uri ->
                            onAnalyze(uri)
                        }
                    }
                ) {
                    Text("Analyze")
                }
            }
        }

        return
    }

   // camera screen


    if (hasCameraPermission) {

        val previewView = remember {
            PreviewView(context)
        }

        AndroidView(
            factory = {
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        LaunchedEffect(Unit) {

            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({

                val cameraProvider =
                    cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()

                preview.setSurfaceProvider(
                    previewView.surfaceProvider
                )

                val cameraSelector =
                    CameraSelector.DEFAULT_BACK_CAMERA

                val capture = ImageCapture.Builder()
                    .setCaptureMode(
                        ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                    )
                    .build()

                imageCapture = capture

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    capture
                )

            }, ContextCompat.getMainExecutor(context))

        }

        // Capture button

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            Button(
                onClick = {

                    val capture = imageCapture
                        ?: return@Button

                    val photoFile = File(
                        context.cacheDir,
                        "clothing_${System.currentTimeMillis()}.jpg"
                    )

                    val outputOptions =
                        ImageCapture.OutputFileOptions
                            .Builder(photoFile)
                            .build()

                    capture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object :
                            ImageCapture.OnImageSavedCallback {

                            override fun onImageSaved(
                                outputFileResults:
                                ImageCapture.OutputFileResults
                            ) {

                                capturedImageUri =
                                    Uri.fromFile(photoFile)
                            }

                            override fun onError(
                                exception: ImageCaptureException
                            ) {
                                exception.printStackTrace()
                            }
                        }
                    )
                },

                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
            ) {
                Text("📸 Capture")
            }
        }

    } else {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "Camera permission is required"
            )
        }
    }
}