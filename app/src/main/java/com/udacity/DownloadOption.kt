package com.udacity

data class DownloadOption(val id: Identifier, val label: String, val url: String) {
    enum class Identifier {
        NONE,
        GLIDE,
        PROJECT_STARTERCODE,
        RETROFIT
    }
}
