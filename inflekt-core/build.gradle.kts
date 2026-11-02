import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.kotlinKover)
    alias(libs.plugins.mavenPublish)
}

group = "com.github.dsrees.inflekt"
version = rootProject.file("version.txt").readText().trim()

kotlin {
    jvmToolchain(21)

    android {
        namespace = "com.github.dsrees.inflekt"
        compileSdk = 35
        minSdk = 21

        withHostTest {}

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    val xcf = XCFramework()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "inflekt-core"
            xcf.add(this)
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // put your multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = "com.github.dsrees",
        artifactId = "inflekt",
        version = version.toString(),
    )

    pom {
        name = "infleckt"
        description = "An inflection library for pluralizing Strings."
        url = "https://github.com/dsrees/inflekt"
        inceptionYear = "2025"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/license/MIT"
                distribution = "https://opensource.org/license/MIT"
            }
        }

        scm {
            connection = "scm:git:git://github.com/dsrees/inflekt.git"
            developerConnection = "scm:git:ssh://github.com/dsrees/inflekt.git"
            url = "https://github.com/dsrees/inflekt"
        }

        developers {
            developer {
                name = "Daniel Rees"
                email = "daniel.rees18@gmail.com"
                url = "https://github.com/dsrees"
            }
        }
    }
}
