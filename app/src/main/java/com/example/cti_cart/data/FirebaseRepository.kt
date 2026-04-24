package com.example.cti_cart.data

import android.net.Uri
import com.example.cti_cart.data.model.RFQ
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

object FirebaseRepository {

    // -------------------- INIT --------------------

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // -------------------- GET USER ROLE --------------------

    fun getUserRole(
        uid: String,
        onResult: (String) -> Unit
    ) {
        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener {
                val role = it.getString("role") ?: ""
                onResult(role)
            }
            .addOnFailureListener {
                it.printStackTrace()
                onResult("")
            }
    }

    // -------------------- IMAGE UPLOAD --------------------

    fun uploadImage(
        uri: Uri,
        folder: String = "machines",
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileName = "${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("$folder/$fileName")

        ref.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception ?: Exception("Upload failed")
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUrl ->
                onSuccess(downloadUrl.toString())
            }
            .addOnFailureListener {
                it.printStackTrace()
                onFailure(it)
            }
    }

    // -------------------- UPLOAD ANY FILE (PDF / CAD / IMAGE) --------------------

    fun uploadFile(
        uri: Uri,
        folder: String = "rfq_files",
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileName = UUID.randomUUID().toString()
        val ref = storage.reference.child("$folder/$fileName")

        ref.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception ?: Exception("Upload failed")
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUrl ->
                onSuccess(downloadUrl.toString())
            }
            .addOnFailureListener {
                it.printStackTrace()
                onFailure(it)
            }
    }

    // -------------------- SAVE COMPANY DETAILS --------------------

    fun saveCompanyDetails(
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        firestore.collection("users")
            .document(userId)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                it.printStackTrace()
                onFailure(it)
            }
    }

    // -------------------- ADD MACHINE --------------------

    fun addMachine(
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        val machineData = data.toMutableMap().apply {
            put("supplierId", userId)
            put("createdAt", System.currentTimeMillis())
            put("status", "available")
        }

        firestore.collection("machines")
            .add(machineData)
            .addOnSuccessListener { doc ->
                doc.update("id", doc.id)
                onSuccess()
            }
            .addOnFailureListener {
                it.printStackTrace()
                onFailure(it)
            }
    }
    // -------------------- COMBINED MACHINE UPLOAD --------------------

    fun uploadMachineWithImage(
        name: String,
        rate: String,
        utilization: String,
        imageUri: Uri,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        uploadImage(
            uri = imageUri,
            onSuccess = { imageUrl ->

                val data = mapOf(
                    "name" to name,
                    "hourlyRate" to rate,
                    "utilization" to utilization,
                    "imageUrl" to imageUrl,
                    "images" to listOf(imageUrl)
                )

                addMachine(
                    data = data,
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            },
            onFailure = onFailure
        )
    }

    // -------------------- SAVE RFQ --------------------

    fun saveRFQ(
        rfq: RFQ,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        val docRef = firestore.collection("rfqs").document()

        val data = rfq.copy(
            id = docRef.id,
            userId = userId,
            timestamp = System.currentTimeMillis()
        )

        docRef.set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                it.printStackTrace()
                onFailure(it)
            }
    }

    // -------------------- GET ALL MACHINES --------------------

    fun getAllMachines(
        onResult: (List<Map<String, Any>>) -> Unit
    ) {
        firestore.collection("machines")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { it.data }
                onResult(list)
            }
            .addOnFailureListener {
                it.printStackTrace()
                onResult(emptyList())
            }
    }

    // -------------------- GET SUPPLIER MACHINES --------------------

    fun getMachinesBySupplier(
        onResult: (List<Map<String, Any>>) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("machines")
            .whereEqualTo("supplierId", userId)
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { it.data }
                onResult(list)
            }
            .addOnFailureListener {
                it.printStackTrace()
                onResult(emptyList())
            }
    }

    // -------------------- DELETE MACHINE --------------------

    fun deleteMachine(
        documentId: String,
        imageUrl: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("machines")
            .document(documentId)
            .delete()
            .addOnSuccessListener {

                if (!imageUrl.isNullOrEmpty()) {
                    try {
                        val ref = storage.getReferenceFromUrl(imageUrl)
                        ref.delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                onSuccess()
            }
            .addOnFailureListener {
                it.printStackTrace()
                onFailure(it)
            }
    }
}