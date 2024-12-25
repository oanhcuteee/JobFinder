plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.jobfinder"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.jobfinder"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.5.6")
    implementation(fileTree(mapOf(
        "dir" to "D:\\RSS",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf("")
    )))

    val retrofitVersion = "2.9.0";
    val lifecycleVersion = "2.7.0";
    val glideVersion = "4.16.0";
    val coroutinesVersion = "1.7.3";
    val nav_version = "2.7.7";
    val firebase_ver = "8.0.2";
    val lottieVersion = "3.4.0"
    val room_version = "2.6.1"


    // default
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


    // google service firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-auth:22.3.1")
    // FirebaseUI for Firebase Realtime Database
    implementation ("com.firebaseui:firebase-ui-database:$firebase_ver")
    implementation("com.google.firebase:firebase-database:20.3.1")
    // FirebaseUI for Cloud Firestore
    implementation ("com.firebaseui:firebase-ui-firestore:$firebase_ver")
    // FirebaseUI for Firebase Auth
    implementation ("com.firebaseui:firebase-ui-auth:$firebase_ver")
    // FirebaseUI for Cloud Storage
    implementation ("com.firebaseui:firebase-ui-storage:$firebase_ver")
    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.0.0")


    // Retrofit2
//    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
//    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
//    implementation("com.squareup.retrofit2:adapter-rxjava2:$retrofitVersion")
//    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
//    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.11.0")


    // Android lifecycle
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-common:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")


    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // store local user data
//    implementation("androidx.datastore:datastore-preferences:1.0.0")
//    implementation("androidx.paging:paging-runtime-ktx:3.2.1")

    //Rxjava
//    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")

    // kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")


    // splash screen animation
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.compose.animation:animation-core-android:1.6.6")


    // loading animation lib from LottieFile
    implementation("com.airbnb.android:lottie:$lottieVersion")


    // Glide for retrieving image from a remote source/URL
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")


    // chart - biểu đồ (nếu add ở pj khác nhớ sửa cả file setting.gradle.kts)
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")


    // Vân tay
    implementation ("androidx.biometric:biometric:1.1.0")


    // SQLite room
    implementation ("androidx.room:room-runtime:$room_version")
    implementation ("androidx.room:room-ktx:$room_version")
    ksp ("androidx.room:room-compiler:$room_version")
    // RxJava2 support for Room
    implementation ("androidx.room:room-rxjava2:$room_version")
    //Guava support for Room, including Optional and ListenableFuture
    implementation ("androidx.room:room-guava:$room_version")
    //Test helpers
    testImplementation ("androidx.room:room-testing:$room_version")


    // import lib Zalo pay
    implementation(fileTree(mapOf(
        "dir" to "D:\\DA\\RSS",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf("")
    )))
    implementation("com.squareup.okhttp3:okhttp:4.6.0")
    implementation("commons-codec:commons-codec:1.15")

    //  swipe refresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
}