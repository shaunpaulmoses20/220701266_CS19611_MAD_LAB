package com.example.stef

import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.stef.ui.theme.StefTheme
import android.content.ContentValues
import android.os.Environment
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {

    private lateinit var originalBitmap: Bitmap

    // Register for image picker
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri ->
            val source = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.createSource(contentResolver, uri)
            } else {
                TODO("VERSION.SDK_INT < P")
            }
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(source)
            } else {
                TODO("VERSION.SDK_INT < P")
            }
            originalBitmap = bitmap
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StefTheme {
                // Compose UI Components
                MainContent()
            }
        }
    }

    @Composable
    fun MainContent() {
        var imageUri: Uri? by remember { mutableStateOf(null) }
        var selectedBitmap: Bitmap? by remember { mutableStateOf(null) }
        var messageInput by remember { mutableStateOf("") }

        val context = LocalContext.current

        Column(modifier = Modifier.padding(16.dp)) {

            // Select from preset images
            Text("Or select a sample image:")
            LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                val sampleImages = listOf(
                    R.drawable.images,
                    R.drawable.images2

                )

                items(sampleImages) { resId ->
                    val bitmap = BitmapFactory.decodeResource(context.resources, resId)
                    Image(
                        painter = rememberAsyncImagePainter(resId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(end = 8.dp)
                            .clickable {
                                selectedBitmap = bitmap
                                imageUri = null // clear URI if using preset
                            }
                    )
                }
            }

            // Image Picker Button
            Button(onClick = {
                imagePickerLauncher.launch("image/*")
            }) {
                Text("Pick Image")
            }

            // Display selected image
            when {
                imageUri != null -> {
                    val painter = rememberAsyncImagePainter(imageUri)
                    Image(
                        painter = painter,
                        contentDescription = "Picked Image",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                selectedBitmap != null -> {
                    Image(
                        bitmap = selectedBitmap!!.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            // Message input field
            OutlinedTextField(
                value = messageInput,
                onValueChange = { messageInput = it },
                label = { Text("Enter Message") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            )

            // Encode Button
            Button(onClick = {
                val bitmapToUse = selectedBitmap // use bitmap directly
                if (bitmapToUse != null && messageInput.isNotBlank()) {
                    val encoded = SteganographyUtil.encodeMessage(bitmapToUse, messageInput)
                    selectedBitmap = encoded
                    Toast.makeText(context, "Message encoded!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Select image and enter message", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Encode Message")
            }
            Button(
                onClick = {
                    selectedBitmap?.let {
                        saveBitmapToGallery(it, context)
                    } ?: Toast.makeText(context, "No image to download", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Download Image")
            }
        }
    }
    fun saveBitmapToGallery(bitmap: Bitmap, context: android.content.Context): Boolean {
        val filename = "Encoded_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Stef")
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                val saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
                Toast.makeText(context, if (saved) "Image saved!" else "Failed to save", Toast.LENGTH_SHORT).show()
                return saved
            } else {
                Toast.makeText(context, "Unable to open output stream", Toast.LENGTH_SHORT).show()
            }
        }
        return false
    }
    }



