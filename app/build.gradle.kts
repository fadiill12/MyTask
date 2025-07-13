plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.mytask"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mytask"
        minSdk = 26
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation ("com.koushikdutta.ion:ion:3.1.0")// buat upload HTTP multipart

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    // Cloudinary Android SDK
    implementation("com.cloudinary:cloudinary-android:2.5.0")
    implementation("com.cloudinary:cloudinary-android-download:3.0.2")
    implementation("com.cloudinary:cloudinary-android-preprocess:3.0.2")
    // Optional: For better HTTP handling
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation("org.json:json:20210307")
    implementation("org.json:json:20230227")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.cloudinary:cloudinary-android:2.8.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}