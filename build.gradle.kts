plugins {
    id("net.fabricmc.fabric-loom") version "1.6-0"
    `maven-publish`
}

base {
    archivesName.set("InstaKillAddon")
    group = "com.example"
    version = "1.0.0"
}

repositories {
    maven("https://maven.meteordev.org/releases")
    maven("https://maven.meteordev.org/snapshots")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.4")
    mappings("net.fabricmc:yarn:1.21.4+build.1:v2")
    modImplementation("net.fabricmc:fabric-loader:0.15.7")
    modImplementation("meteordevelopment:meteor-client:1.21.4")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
