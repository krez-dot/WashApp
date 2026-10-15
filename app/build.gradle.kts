import java.util.Properties

// Dev quick-login accounts live in the git-ignored local.properties, never in the repo.
val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
fun devField(key: String) = "\"" + (localProps.getProperty(key) ?: "") + "\""

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.washapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.washapp.tsu"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "DEV_CUSTOMER_EMAIL", devField("dev.customer.email"))
            buildConfigField("String", "DEV_CUSTOMER_PASSWORD", devField("dev.customer.password"))
            buildConfigField("String", "DEV_ADMIN_EMAIL", devField("dev.admin.email"))
            buildConfigField("String", "DEV_ADMIN_PASSWORD", devField("dev.admin.password"))
        }
        release {
            buildConfigField("String", "DEV_CUSTOMER_EMAIL", "\"\"")
            buildConfigField("String", "DEV_CUSTOMER_PASSWORD", "\"\"")
            buildConfigField("String", "DEV_ADMIN_EMAIL", "\"\"")
            buildConfigField("String", "DEV_ADMIN_PASSWORD", "\"\"")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
