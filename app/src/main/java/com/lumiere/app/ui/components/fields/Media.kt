package com.lumiere.app.ui.components.fields

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lumiere.app.ui.theme.LumiereGold
import com.lumiere.app.ui.theme.LumiereDarkGray

@Composable
fun LumiereFilePicker(
    onFileSelected: (Uri?) -> Unit,
    buttonText: String = "Anexar Ficheiro"
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> onFileSelected(uri) }

    OutlinedButton(
        onClick = { launcher.launch("*/*") },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = LumiereGold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(buttonText, color = LumiereGold)
    }
}

@Composable
fun LumiereImagePicker(
    onImageSelected: (Uri?) -> Unit,
    buttonText: String = "Escolher Imagem"
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> onImageSelected(uri) }

    OutlinedButton(
        onClick = { launcher.launch("image/*") },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = LumiereGold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(buttonText, color = LumiereGold)
    }
}

@Composable
fun LumiereAudioRecorder(
    isRecording: Boolean,
    onToggleRecording: () -> Unit
) {
    Button(
        onClick = onToggleRecording,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRecording) Color.Red else LumiereGold,
            contentColor = Color.Black
        )
    ) {
        Icon(
            imageVector = if (isRecording) Icons.Default.Refresh else Icons.Default.Add,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (isRecording) "Parar Gravação" else "Gravar Áudio")
    }
}
