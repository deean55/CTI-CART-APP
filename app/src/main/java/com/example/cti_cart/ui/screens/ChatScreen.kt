package com.cti.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.cti_cart.data.FirebaseRepository
import com.google.firebase.firestore.FieldValue

data class ChatMessage(
    val senderId: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)

@Composable
fun ChatScreen(

        chatId: String
) {
    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }

    val currentUserId =
        FirebaseRepository.auth.currentUser?.uid ?: ""


    LaunchedEffect(chatId) {

        FirebaseRepository.firestore
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { value, _ ->

                messages =
                    value?.documents?.mapNotNull {
                        it.toObject(ChatMessage::class.java)
                    } ?: emptyList()
            }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = "Chat",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {

            items(messages) { chatMessage ->

                val isMine =
                    chatMessage.senderId == currentUserId

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        if (isMine)
                            Arrangement.End
                        else
                            Arrangement.Start
                ) {

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color =
                            if (isMine)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .padding(4.dp)
                    ) {

                        Text(
                            text = chatMessage.message,
                            modifier = Modifier.padding(12.dp),
                            color =
                                if (isMine)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Type a message")
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {

                    if (message.isBlank()) return@Button

                    val chatMessage = hashMapOf(
                        "senderId" to currentUserId,
                        "message" to message.trim(),
                        "timestamp" to FieldValue.serverTimestamp()
                    )

                    FirebaseRepository.firestore
                        .collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .add(chatMessage)

                    message = ""
                }
            ) {
                Text("Send")
            }
        }
    }
}