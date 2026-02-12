package com.xshe.quantum

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Values {
    var isCanSelected by mutableStateOf(true)
    var roomName by mutableStateOf("null")
    val messageList = mutableStateListOf<String>()
    var historyHost = ""


    data class RoomNumbers(var present: Int = 0, var max: Int = 0)
    data class ListItem(
        val itemHost: String,
        val itemStatus: String,
        val isSelected: Boolean = false
    )

    fun reset() {
        isCanSelected = true
        roomName = "null"
        messageList.clear()
    }
}