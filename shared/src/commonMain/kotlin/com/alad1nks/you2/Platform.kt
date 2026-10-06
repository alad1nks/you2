package com.alad1nks.you2

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
