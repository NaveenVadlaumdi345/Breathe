package uk.ac.tees.mad.breathe

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUiState())
    val ui = _ui.asStateFlow()

    var showPickerDialog by mutableStateOf(false)

    init {
        loadProfile()
    }

    val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", "dn8ycjojw",
            "api_key", "281678982458183",
            "api_secret", "77nO2JN3hkGXB-YgGZuJOqXcA4Q"
        )
    )

    private suspend fun uploadImageToCloudinary(
        context: Context,
        cloudinary: Cloudinary,
        imageBitmap: Bitmap? = null,
        imageUri: Uri? = null
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val uploadResult: Map<*, *>

                when {
                    imageBitmap != null -> {
                        val stream = ByteArrayOutputStream()
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                        val imageBytes = stream.toByteArray()
                        uploadResult = cloudinary.uploader().upload(imageBytes, ObjectUtils.emptyMap())
                    }
                    imageUri != null -> {
                        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                        uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.emptyMap())
                        inputStream?.close()
                    }
                    else -> return@withContext null
                }

                uploadResult["secure_url"] as? String
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun uploadProfileImage(imageBitmap: Bitmap, context: Context) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isUploading = true)
            val url = uploadImageToCloudinary(context, cloudinary, imageBitmap = imageBitmap)
            url?.let { saveProfileUrl(it) }
            _ui.value = _ui.value.copy(isUploading = false)
        }
    }

    fun uploadProfileImageFromUri(imageUri: Uri, context: Context) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isUploading = true)
            val url = uploadImageToCloudinary(context, cloudinary, imageUri = imageUri)
            url?.let { saveProfileUrl(it) }
            _ui.value = _ui.value.copy(isUploading = false)
        }
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get().addOnSuccessListener {
            val name = it.getString("name") ?: "User"
            val profileUrl = it.getString("profileUrl")
            _ui.value = _ui.value.copy(name = name, profileUrl = profileUrl)
        }
    }

    fun onNameChange(newName: String) {
        _ui.value = _ui.value.copy(name = newName)
    }

    fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isSaving = true)
            firestore.collection("users").document(uid)
                .update("name", _ui.value.name)
                .addOnSuccessListener {
                    _ui.value = _ui.value.copy(isSaving = false)
                }
                .addOnFailureListener {
                    _ui.value = _ui.value.copy(isSaving = false)
                }
        }
    }

    private fun saveProfileUrl(url: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .update("profileUrl", url)
            .addOnSuccessListener {
                _ui.value = _ui.value.copy(profileUrl = url)
            }
    }
}

data class ProfileUiState(
    val name: String = "",
    val profileUrl: String? = null,
    val isSaving: Boolean = false,
    val isUploading: Boolean = false
)