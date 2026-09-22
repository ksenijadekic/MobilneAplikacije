package com.example.rmas_projekat.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rmas_projekat.ui.theme.OrangeDark
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun ImagePickers(
    onImagePicked: (Uri) -> Unit,
    onError: (String) -> Unit = {}
) {
    val ctx = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Galerija
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) onImagePicked(uri) else onError("Nije izabrana slika.")
    }

    // Kamera (potreban Uri)
    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) onImagePicked(tempPhotoUri!!)
        else onError("Snimanje nije uspelo.")
    }

    // Dozvole
    val reqPerms = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val ok = granted.values.any { it }
        if (!ok) onError("Nisu odobrene dozvole.")
    }

    fun ensurePermissions(then: () -> Unit) {
        val need = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= 33) {
            need += Manifest.permission.READ_MEDIA_IMAGES
        } else {
            need += Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val missing = need.filter {
            ContextCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) reqPerms.launch(missing.toTypedArray()) else then()
    }

    fun newImageUri(): Uri {
        val imagesDir = File(ctx.cacheDir, "images").apply { mkdirs() }
        val file = File.createTempFile("photo_", ".jpg", imagesDir)
        return FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = { ensurePermissions { pickImage.launch("image/*") } },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, OrangeDark),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeDark),
            modifier = Modifier.weight(1f).height(48.dp)
        ) {
            Text("Galerija", fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = {
                ensurePermissions {
                    val uri = newImageUri()
                    tempPhotoUri = uri
                    takePicture.launch(uri)
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeDark, contentColor = Color.White),
            modifier = Modifier.weight(1f).height(48.dp)
        ) {
            Text("Kamera", fontWeight = FontWeight.Bold)
        }
    }
}
