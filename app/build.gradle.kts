plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id ("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false
    id("kotlin-kapt")
    id("kotlin-parcelize")
    // Firebase
    id("com.google.gms.google-services")
    //DI
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.bookreader"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bookreader"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.cardview)
    val nav_version = "2.7.7"

    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    val room_version = "2.6.1"
    //room
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    //coroutines
    implementation("androidx.room:room-ktx:$room_version")
    //material design
    implementation("com.google.android.material:material:1.12.0")
    val retrofitVersion = "2.11.0"
    //retrofit
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    //moshi
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
//    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")
//    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    //glide
    implementation("com.github.bumptech.glide:glide:4.13.0")
    implementation ("jp.wasabeef:glide-transformations:4.3.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3") // Or latest version
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    val lifecycleVersion = "2.8.4"
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")

    //expandable view
    implementation("com.ms-square:expandableTextView:0.1.4")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-analytics")

    // Firebase Auth
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-auth")
    // Firebase Firestore
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-firestore")

    //Di
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    // Splash Screen Core
    implementation("androidx.core:core-splashscreen:1.0.0")

    implementation ("de.hdodenhof:circleimageview:3.1.0")
}
kapt {
    correctErrorTypes = true

    /*
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    */
}