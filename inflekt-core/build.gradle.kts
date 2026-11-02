import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.kotlinKover)
    alias(libs.plugins.mavenPublish)
}

version = rootProject.file("version.txt").readText().trim()

kotlin {
    jvmToolchain(21)
    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()

    android {
        namespace = "com.github.dsrees.inflekt"
        compileSdk = 37
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

    iosArm64()
    iosSimulatorArm64()
    macosArm64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosArm64()
    watchosSimulatorArm64()

    linuxX64()
    linuxArm64()
    mingwX64()

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

kover {
    reports {
        verify {
            rule {
                minBound(95)
            }
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
        name = "inflekt"
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
