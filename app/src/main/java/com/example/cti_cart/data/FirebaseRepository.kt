package com.example.cti_cart.data

import android.net.Uri
import android.util.Log
import com.example.cti_cart.data.model.RFQ
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    // -------------------- UPLOAD ANY FILE --------------------

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

        machineType: String,

        xTravel: String,
        yTravel: String,
        zTravel: String,

        spindleTaper: String,
        controlSystem: String,
        axisCount: String,

        // HMC
        palletSize: String,
        numberOfPallets: String,
        bAxis: Boolean,
        bAxisDegree: String,

        // TURNING CENTRE
        maxTurningDiameter: String,
        maxTurningLength: String,
        chuckSize: String,
        spindleBore: String,
        spindleSpeed: String,

        imageUri: Uri,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        uploadImage(
            uri = imageUri,
            onSuccess = { imageUrl ->

                val machineData = hashMapOf(
                    "supplierId" to userId,
                    "name" to name,
                    "hourlyRate" to rate,
                    "utilization" to utilization,

                    "machineType" to machineType,

                    // Common Travel
                    "xTravel" to (xTravel.toIntOrNull() ?: 0),
                    "yTravel" to (yTravel.toIntOrNull() ?: 0),
                    "zTravel" to (zTravel.toIntOrNull() ?: 0),
                    //Common
                    "spindleTaper" to spindleTaper,
                    "controlSystem" to controlSystem,
                    "axisCount" to axisCount,
                    // HMC
                    "palletSize" to (palletSize.toIntOrNull() ?: 0),
                    "numberOfPallets" to (numberOfPallets.toIntOrNull() ?: 0),
                    "bAxis" to bAxis,
                    "bAxisDegree" to bAxisDegree,

                    // TURNING CENTRE
                    "maxTurningDiameter" to (maxTurningDiameter.toIntOrNull() ?: 0),
                    "maxTurningLength" to (maxTurningLength.toIntOrNull() ?: 0),
                    "chuckSize" to (chuckSize.toIntOrNull() ?: 0),
                    "spindleBore" to (spindleBore.toIntOrNull() ?: 0),
                    "spindleSpeed" to (spindleSpeed.toIntOrNull() ?: 0),

                    "imageUrl" to imageUrl,
                    "images" to listOf(imageUrl),

                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("machines")
                    .add(machineData)
                    .addOnSuccessListener { doc ->

                        doc.update("id", doc.id)

                        onSuccess()
                    }
                    .addOnFailureListener {
                        onFailure(it)
                    }
            },
            onFailure = {
                onFailure(it)
            }
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


    // -------------------- SAVE QUOTE --------------------

    fun saveQuote(
        quoteData: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val supplierId = auth.currentUser?.uid

        if (supplierId == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        val docRef = firestore.collection("quotes").document()
        val quote = quoteData.toMutableMap().apply {
            put("id", docRef.id)
            put("supplierId", supplierId)
        }

        docRef.set(quote)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                it.printStackTrace()
                onFailure(it)
            }
    }

    // -------------------- GET QUOTES FOR BUYER RFQ --------------------

    fun getQuotesForRFQ(
        rfqId: String,
        onResult: (List<com.example.cti_cart.data.model.Quote>) -> Unit,
        onFailure: (Exception) -> Unit = { onResult(emptyList()) }
    ) {
        val buyerId = auth.currentUser?.uid

        if (buyerId == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        // Buyer can only query quotes belonging to their own RFQ.
        // This also satisfies the Firestore security rule:
        // buyerId == request.auth.uid
        firestore.collection("quotes")
            .whereEqualTo("rfqId", rfqId)
            .whereEqualTo("buyerId", buyerId)
            .get()
            .addOnSuccessListener { result ->
                val quotes = result.documents.mapNotNull { document ->
                    document.toObject(com.example.cti_cart.data.model.Quote::class.java)?.copy(
                        id = document.id
                    )
                }.sortedByDescending { it.createdAt }

                onResult(quotes)
            }
            .addOnFailureListener {
                it.printStackTrace()
                onFailure(it)
            }
    }

    // -------------------- UPDATE QUOTE STATUS --------------------

    fun updateQuoteStatus(
        quoteId: String,
        status: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("quotes")
            .document(quoteId)
            .update(
                mapOf(
                    "status" to status,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                it.printStackTrace()
                onFailure(it)
            }
    }

    // -------------------- GET MY QUOTES (REAL-TIME) --------------------

    fun listenToMyQuotes(
        onResult: (List<com.example.cti_cart.data.model.Quote>) -> Unit,
        onFailure: (Exception) -> Unit = {}
    ): ListenerRegistration? {
        val supplierId = auth.currentUser?.uid

        if (supplierId == null) {
            onFailure(Exception("User not logged in"))
            onResult(emptyList())
            return null
        }

        return firestore.collection("quotes")
            .whereEqualTo("supplierId", supplierId)
            .addSnapshotListener { result, error ->
                if (error != null) {
                    error.printStackTrace()
                    onFailure(error)
                    return@addSnapshotListener
                }

                val quotes = result?.documents?.mapNotNull { document ->
                    document.toObject(com.example.cti_cart.data.model.Quote::class.java)?.copy(
                        id = document.id
                    )
                }?.sortedByDescending { it.createdAt } ?: emptyList()

                onResult(quotes)
            }
    }

    // -------------------- GET MY RFQs --------------------

    fun getMyRFQs(
        onResult: (List<RFQ>) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        Log.d("USER_CHECK", auth.currentUser?.uid ?: "NULL")
        if (userId == null) {
            onResult(emptyList())
            return
        }

        firestore.collection("rfqs")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(RFQ::class.java)
                Log.d("RFQ_DATA", list.toString())
                onResult(list)
            }
            .addOnFailureListener {
                it.printStackTrace()
                onResult(emptyList())
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
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onResult(emptyList())
            return
        }

        firestore.collection("machines")
            .whereEqualTo("supplierId", userId)
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

    // -------------------- Get All RFQ's --------------------
    fun getAllRFQs(onResult: (List<RFQ>) -> Unit) {

        firestore.collection("rfqs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->

                val rfqs = result.documents.mapNotNull { document ->

                    document.toObject(RFQ::class.java)?.copy(
                        id = document.id
                    )
                }

                onResult(rfqs)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}