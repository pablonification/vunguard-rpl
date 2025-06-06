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
    // Database dependencies
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.0.1")
    
    // JSON processing
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
    
    // Password hashing
    implementation("org.mindrot:jbcrypt:0.4")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    
    // Configuration
    implementation("org.yaml:snakeyaml:2.0")
    
    // HTTP Client (if needed for external APIs)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.mockito:mockito-core:5.4.0")
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