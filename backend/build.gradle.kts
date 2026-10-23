import org.gradle.internal.os.OperatingSystem

plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

if (OperatingSystem.current().isWindows) {
    sourceSets["main"].java.srcDir("src/windows/java")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.flywaydb:flyway-core")
    implementation("org.xerial:sqlite-jdbc:3.47.2.0")
    implementation("org.postgresql:postgresql")
    implementation("net.java.dev.jna:jna:5.15.0")
    implementation("net.java.dev.jna:jna-platform:5.15.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.zonky.test:embedded-postgres:2.0.7")
}

tasks.named<Test>("test") {
    systemProperty("dataweave.test.mode", "true")
}

