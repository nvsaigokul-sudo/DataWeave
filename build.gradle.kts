plugins {
    kotlin("jvm") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("org.jetbrains.compose") version "1.7.1" apply false
}

allprojects {
    group = "com.dataweave"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xjvm-default=all")
        }
    }
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
