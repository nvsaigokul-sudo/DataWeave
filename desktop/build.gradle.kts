import org.gradle.internal.os.OperatingSystem

/** jpackage/macOS require MAJOR version >= 1 (0.2.0 is invalid). */
fun jpackageVersion(raw: String): String {
    val version = raw.removeSuffix("-SNAPSHOT")
    val parts = version.split(".")
    val major = parts.firstOrNull()?.toIntOrNull() ?: 1
    if (major >= 1) return version
    val rest = parts.drop(1).joinToString(".").ifEmpty { "0" }
    return "1.$rest"
}

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("io.spring.dependency-management")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.1")
    }
}

dependencies {
    implementation(project(":backend"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("io.zonky.test:embedded-postgres:2.0.7")

    testImplementation(kotlin("test"))
}

compose.desktop {
    application {
        mainClass = "com.dataweave.app.DataWeaveBootstrapKt"
        nativeDistributions {
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            packageName = "DataWeave"
            val pkgVersion = jpackageVersion(project.version.toString())
            packageVersion = pkgVersion
            description = "Offline-first PostgreSQL data workbench"
            vendor = "DataWeave"
            val os = OperatingSystem.current()
            // Explicit jlink set: Gradle suggestRuntimeModules omits java.desktop (Compose/Skiko need it).
            // jdk.accessibility: Access Bridge / %USERPROFILE%\.accessibility.properties on Windows.
            // jdk.crypto.ec: Spring TLS when users connect with SSL.
            modules(
                "java.compiler",
                "java.desktop",
                "java.instrument",
                "java.logging",
                "java.management",
                "java.naming",
                "java.net.http",
                "java.prefs",
                "java.scripting",
                "java.security.jgss",
                "java.sql",
                "java.sql.rowset",
                "java.xml",
                "jdk.crypto.ec",
                "jdk.jfr",
                "jdk.localedata",
                "jdk.unsupported",
                "jdk.accessibility",
            )
            if (os.isMacOsX) {
                macOS {
                    dmgPackageVersion = pkgVersion
                    pkgPackageVersion = pkgVersion
                }
            }
            if (os.isWindows) {
                windows {
                    exePackageVersion = pkgVersion
                    msiPackageVersion = pkgVersion
                    menuGroup = "DataWeave"
                    upgradeUuid = "18189999-4338-4F3C-8424-4A0B2E720508"
                    menu = true
                    shortcut = true
                }
            }
            targetFormats(
                *when {
                    os.isWindows -> arrayOf(
                        org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                        org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                    )
                    os.isMacOsX -> arrayOf(
                        org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                        org.jetbrains.compose.desktop.application.dsl.TargetFormat.Pkg,
                    )
                    os.isLinux -> arrayOf(
                        org.jetbrains.compose.desktop.application.dsl.TargetFormat.AppImage,
                        org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                    )
                    else -> arrayOf(org.jetbrains.compose.desktop.application.dsl.TargetFormat.AppImage)
                },
            )
        }
    }
}

tasks.withType<JavaExec>().configureEach {
    if (OperatingSystem.current().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }
}

tasks.register<JavaExec>("runHeadlessSmoke") {
    group = "verification"
    description = "Headless smoke: Spring Boot + SQLite + optional embedded PG."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dataweave.app.HeadlessSmokeKt")
    systemProperty("dataweave.test.mode", "true")
}
