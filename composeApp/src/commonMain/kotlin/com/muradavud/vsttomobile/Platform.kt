package com.muradavud.vsttomobile

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform