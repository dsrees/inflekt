package dev.codestead.inflekt

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform