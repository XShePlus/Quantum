package com.xshe.quantum

data class ApiResponse(
    val code: Int,
    val message: String,
    val data: Any?
)