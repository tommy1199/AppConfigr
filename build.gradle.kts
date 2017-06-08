import com.jfrog.bintray.gradle.*
import java.util.*

plugins {
    java
    maven
    id("com.jfrog.bintray").version("1.7")
}

repositories {
    mavenCentral()
}

val sourceCompatibility = JavaVersion.VERSION_1_7
val targetCompatibility = JavaVersion.VERSION_1_7

dependencies {
    compile("com.google.guava:guava:19.0")
    compile("com.fasterxml.jackson.core:jackson-databind:2.8.3")
    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.8.3")
    testCompile("junit:junit:4.12")
    testCompile("com.github.stefanbirkner:system-rules:1.16.0")
    testCompile("org.assertj:assertj-core:2.5.0")
}

configure<BintrayExtension> {
    user = if (project.hasProperty("bintrayUser")) project.property("bintrayUser").toString() else System.getenv("BINTRAY_USER")
    key = if (project.hasProperty("bintrayApiKey")) project.property("bintrayApiKey").toString() else System.getenv("BINTRAY_API_KEY")

    setConfigurations("archives")

    pkg(closureOf<BintrayExtension.PackageConfig> {
        repo = "appconfigr"
        name = "AppConfigr"
        setLicenses("MIT")
        vcsUrl = "https://github.com/Tommy1199/AppConfigr.git"
        version(closureOf<BintrayExtension.VersionConfig> {
            name = "1.0"
            released = Date().toString()
        })
    })
}