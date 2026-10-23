import org.gradle.internal.os.OperatingSystem

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
        mainClass = "com.dataweave.app.DataWeaveAppKt"
        nativeDistributions {
            packageName = "DataWeave"
            packageVersion = project.version.toString().removeSuffix("-SNAPSHOT")
            description = "Offline-first PostgreSQL data workbench"
            vendor = "DataWeave"
            val os = OperatingSystem.current()
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
}
