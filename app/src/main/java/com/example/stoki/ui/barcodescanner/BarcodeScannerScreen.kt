package com.example.stoki.ui.barcodescanner

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val barcodeAnalyser = remember {
        BarcodeAnalyser { barcode ->
            // Garante que a navegação aconteça na thread principal
            scope.launch { // <-- ENVOLVA COM O LAUNCH
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("barcode_result", barcode)
                navController.popBackStack()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Câmera Preview
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { factoryContext ->
                val previewView = PreviewView(factoryContext)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(factoryContext)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(Executors.newSingleThreadExecutor(), barcodeAnalyser)
                        }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageAnalysis
                        )
                    } catch (e: Exception) {
                        // Lidar com erros
                    }
                }, ContextCompat.getMainExecutor(factoryContext))
                previewView
            }
        )

        // Overlay (Sobreposição) com a "janela" do scanner
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rectSize = size * 0.7f // A janela será 70% do tamanho da tela
            val topLeft = Offset((size.width - rectSize.width) / 2, (size.height - rectSize.height) / 2)

            // Desenha a sombra escura em volta
            drawRect(color = Color.Black.copy(alpha = 0.5f))

            // "Corta" um retângulo transparente no meio
            drawRoundRect(
                topLeft = topLeft,
                size = rectSize,
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                color = Color.Transparent,
                blendMode = BlendMode.Clear
            )
            // Desenha a borda branca em volta do corte
            drawRoundRect(
                topLeft = topLeft,
                size = rectSize,
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                color = Color.White,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Texto de instrução e botão de voltar
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                }
            }
            Text(
                text = "Aponte a câmera para o código de barras",
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 64.dp)
            )
        }
    }
}