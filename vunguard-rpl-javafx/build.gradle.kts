plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("com.vunguard.Main")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    // Dependensi untuk koneksi ke PostgreSQL
    implementation("org.postgresql:postgresql:42.7.3")

    // Dependensi untuk hashing password (bcrypt)
    implementation("org.mindrot:jbcrypt:0.4")
}

// Tugas untuk menyalin aset ke direktori build
tasks.register<Copy>("copyAssets") {
    from("src/main/resources/com/vunguard/assets")
    into("build/resources/main/com/vunguard/assets")
}

tasks.register<Copy>("copyFonts") {
    from("src/main/resources/com/vunguard/fonts")
    into("build/resources/main/com/vunguard/fonts")
}

tasks.named("processResources") {
    dependsOn("copyAssets", "copyFonts")
}