package com.dataweave.app

/**
 * jpackage entry with no Compose imports so reflection can delay loading Skiko/AWT
 * until after basic JVM startup (see compose-multiplatform#4233).
 */
fun main(args: Array<String>) {
    try {
        val entry =
            Class.forName("com.dataweave.app.DataWeaveAppKt")
                .getDeclaredMethod("dataWeaveMain", Array<String>::class.java)
        entry.invoke(null, args)
    } catch (t: Throwable) {
        val root = (t as? java.lang.reflect.InvocationTargetException)?.targetException ?: t
        EarlyLaunchFailure.log("bootstrap", root)
        throw root
    }
}
