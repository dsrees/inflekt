import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinKover)
    alias(libs.plugins.mavenPublish)
}

group = "com.github.dsrees.inflekt"
version = "0.1.0"

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
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

android {
    namespace = "com.github.dsrees.inflekt"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
