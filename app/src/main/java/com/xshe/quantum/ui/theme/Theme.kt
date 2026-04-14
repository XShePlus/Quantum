package com.xshe.quantum.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme


@Composable
fun QuantumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MiuixTheme(
        colors = if (darkTheme) {
            darkColorScheme(
                primary = Orange80,
                secondary = Color(0xFF9E9E9E),  // Switch关闭态 - 灰色
                primaryContainer = Coral80
            )
        } else {
            lightColorScheme(
                primary = Orange40,
                secondary = Color(0xFFBDBDBD),  // Switch关闭态 - 浅灰
                primaryContainer = Coral40
            )
        },
        content = content
    )
}