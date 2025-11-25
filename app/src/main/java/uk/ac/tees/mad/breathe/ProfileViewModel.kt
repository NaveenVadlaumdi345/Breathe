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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
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
        }
    }

    fun uploadProfileImage(bitmap: Bitmap, context: Context) {
//        val uid = auth.currentUser?.uid ?: return
//        val storageRef = FirebaseStorage.getInstance().reference
//            .child("profiles/$uid-${UUID.randomUUID()}.jpg")
//
//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
//        val data = baos.toByteArray()
//
//        storageRef.putBytes(data).addOnSuccessListener {
//            storageRef.downloadUrl.addOnSuccessListener { uri ->
//                saveProfileUrl(uri.toString())
//            }
//        }
    }

    fun uploadProfileImageFromUri(uri: Uri, context: Context) {
//        val uid = auth.currentUser?.uid ?: return
//        val storageRef = FirebaseStorage.getInstance().reference
//            .child("profiles/$uid-${UUID.randomUUID()}.jpg")
//
//        storageRef.putFile(uri).addOnSuccessListener {
//            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
//                saveProfileUrl(downloadUri.toString())
//            }
//        }
    }

    private fun saveProfileUrl(url: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .update("profileUrl", url)
        _ui.value = _ui.value.copy(profileUrl = url)
    }

    fun logout() {
        auth.signOut()
    }
}

data class ProfileUiState(
    val name: String = "",
    val profileUrl: String? = null,
    val isSaving: Boolean = false
)
