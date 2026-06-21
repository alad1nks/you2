package com.alad1nks.startkmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform