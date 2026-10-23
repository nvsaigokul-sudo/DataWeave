plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)

    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.flywaydb:flyway-core:10.21.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.21.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")

    testImplementation(kotlin("test"))
    implementation("io.zonky.test:embedded-postgres:2.0.7")
    testImplementation("io.zonky.test:embedded-postgres:2.0.7")
}

compose.desktop {
    application {
        mainClass = "com.dataweave.app.DataWeaveAppKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi)
            packageName = "DataWeave"
            packageVersion = "0.1.0"
            description = "Offline-first data integration workbench"
            vendor = "DataWeave"
        }
    }
}

tasks.register<JavaExec>("runHeadlessSmoke") {
    group = "verification"
    description = "Starts the app briefly without a GUI (dev embedded database)."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dataweave.app.HeadlessSmokeKt")
}
