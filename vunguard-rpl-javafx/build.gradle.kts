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
    version = "21" // Versi JavaFX, sesuaikan jika perlu
    modules = listOf("javafx.controls", "javafx.fxml") // Tambahkan modul lain jika dibutuhkan
}

dependencies {
    // Tambahkan dependensi lain di sini jika diperlukan
}

// Tugas untuk menyalin aset ke direktori build
tasks.register<Copy>("copyAssets") {
    from("src/main/resources/com/vunguard/assets")
    into("build/resources/main/com/vunguard/assets")
}

tasks.register<Copy>("copyFonts") {
    from("src/main/resources/com/vunguard/fonts") // Asumsi font ada di sini setelah disalin dari public
    into("build/resources/main/com/vunguard/fonts")
}

// Pastikan aset dan font disalin sebelum aplikasi dijalankan
tasks.named("processResources") {
    dependsOn("copyAssets", "copyFonts")
} 