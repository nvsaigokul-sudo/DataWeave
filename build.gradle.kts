plugins {
    kotlin("jvm") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("org.jetbrains.compose") version "1.7.1" apply false
    id("org.springframework.boot") version "3.4.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

/** jpackage/macOS require MAJOR version >= 1 (0.x.y maps to 1.x.y). Keep aligned with desktop/build.gradle.kts. */
fun jpackageVersion(raw: String): String {
    val version = raw.removeSuffix("-SNAPSHOT")
    val parts = version.split(".")
    val major = parts.firstOrNull()?.toIntOrNull() ?: 1
    if (major >= 1) return version
    val rest = parts.drop(1).joinToString(".").ifEmpty { "0" }
    return "1.$rest"
}

allprojects {
    group = "com.dataweave"
    version = "0.3.0-SNAPSHOT"
}

tasks.register("printProductVersion") {
    group = "versioning"
    description = "Print Gradle product version and jpackage package version for CI reporting."
    doLast {
        val raw = version.toString()
        logger.lifecycle("dataweave.gradleVersion=$raw")
        logger.lifecycle("dataweave.jpackageVersion=${jpackageVersion(raw)}")
    }
}

tasks.register("assertProductVersionPrefix") {
    group = "verification"
    description = "Fails when -PrequireProductVersionPrefix is set and project.version does not match."
    val requiredPrefix = providers.gradleProperty("requireProductVersionPrefix")
    doLast {
        val prefix = requiredPrefix.orNull ?: return@doLast
        val productVersion = version.toString()
        check(productVersion.startsWith(prefix)) {
            "Gradle product version '$productVersion' must start with '$prefix'. " +
                "Refusing to treat this ref as the intended release candidate (wrong branch/commit?)."
        }
        logger.lifecycle("Product version gate OK: $productVersion (prefix $prefix)")
    }
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
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
}
