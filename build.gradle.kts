plugins {
    kotlin("jvm") version "2.0.20"
}

group = "net.andrecarbajal"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib"))
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.2")
    implementation("org.telegram:telegrambots:6.9.7.1")
    implementation("org.jsoup:jsoup:1.18.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}