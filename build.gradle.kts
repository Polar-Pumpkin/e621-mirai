plugins {
    val kotlinVersion = "1.8.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.16.0"
}

group = "me.parrot.mirai"
version = "1.0.3"

mirai {
    jvmTarget = JavaVersion.VERSION_1_8
}

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
}

tasks.compileKotlin {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xcontext-receivers")
    }
}