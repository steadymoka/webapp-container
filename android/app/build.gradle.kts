plugins {
    id("com.android.application")
    id("kotlin-android")
//    id("com.google.gms.google-services")
}

android {
    compileSdk = 30
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "com.moka.webappandroid"
        minSdk = 26
        targetSdk = 30
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["compose_version"] as String
    }

    flavorDimensions.add("flavor")

    productFlavors {
        create("prod") {
            dimension = "flavor"
            addManifestPlaceholders(mutableMapOf(
                "appLabel" to "@string/app_name",
                "appIcon" to "@mipmap/ic_launcher",
                "roundIcon" to "@mipmap/ic_launcher_round"
            ))

            copy {
                from("src/prod/")
                include("*.json")
                into(".")
            }
        }

        create("dev") {
            dimension = "flavor"
            addManifestPlaceholders(mutableMapOf(
                "appLabel" to "webapp-dev",
                "appIcon" to "@mipmap/ic_launcher_dev",
                "roundIcon" to "@mipmap/ic_launcher_dev_round"
            ))

            applicationIdSuffix = ".dev"
            versionNameSuffix = "-DEV"

            copy {
                from("src/dev/")
                include("*.json")
                into(".")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    // region androidx
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0-alpha01")
    implementation("androidx.activity:activity-compose:1.3.0-alpha07")

    implementation("androidx.compose.ui:ui:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.ui:ui-tooling:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.material:material:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.runtime:runtime-livedata:${rootProject.extra["compose_version"]}")
    // endregion

    // region Firebase
    implementation(platform("com.google.firebase:firebase-bom:27.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    // endregion

    implementation("com.google.android.material:material:1.3.0")
    implementation("com.google.accompanist:accompanist-insets:0.8.1")
    implementation("com.google.code.gson:gson:2.8.6")

    implementation("com.airbnb.android:lottie-compose:1.0.0-beta03-1")

    // region Mokaroid
    implementation("com.github.moka-a.mokaroid:imagehelper:0.6.4")
    // endregion

    // region test
    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${rootProject.extra["compose_version"]}")
    // endregion
}