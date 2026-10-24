package com.dataweave.app.preferences

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
    ;

    companion object {
        fun fromStored(value: String?): ThemeMode =
            when (value?.uppercase()) {
                "DARK" -> DARK
                "LIGHT" -> LIGHT
                else -> SYSTEM
            }
    }
}
