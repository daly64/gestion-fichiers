plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.gestionfichiers"
    compileSdk = 35

    defaultConfig {
        // Identifiant définitif (« com.example » est réservé aux exemples et mal vu par Google)
        applicationId = "io.github.daly64.gestionfichiers"
        minSdk = 26
        targetSdk = 35
        versionCode = 9
        versionName = "2.2"
        resValue("string", "app_name", "Gestion Fichiers")
    }

    // Clé de publication : fournie par GitHub Actions (secrets), jamais stockée dans le dépôt.
    val releaseKeystore = System.getenv("SIGNING_KEYSTORE_FILE")?.let { file(it) }?.takeIf { it.exists() }

    signingConfigs {
        // Clé de test fixe : chaque version de test s'installe par-dessus la précédente
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        if (releaseKeystore != null) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = System.getenv("SIGNING_PASSWORD")
                keyAlias = "gestionfichiers"
                keyPassword = System.getenv("SIGNING_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            // Installable à côté de la version publiée
            applicationIdSuffix = ".test"
            versionNameSuffix = "-test"
            resValue("string", "app_name", "Gestion Fichiers (test)")
        }
        release {
            isDebuggable = false
            if (releaseKeystore != null) signingConfig = signingConfigs.getByName("release")
            // Réduit fortement la taille de l'APK (icônes et code inutilisés retirés)
            isMinifyEnabled = true
            isShrinkResources = true
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
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.core:core-ktx:1.15.0")
    // Miniatures des images et des vidéos
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-video:2.7.0")
    // Archives : ZIP avec mot de passe (AES), 7Z (+ compression LZMA), RAR
    implementation("net.lingala.zip4j:zip4j:2.11.5")
    implementation("org.apache.commons:commons-compress:1.27.1")
    implementation("org.tukaani:xz:1.10")
    implementation("com.github.junrar:junrar:7.5.5")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
