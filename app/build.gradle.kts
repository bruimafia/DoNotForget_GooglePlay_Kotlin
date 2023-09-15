plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "ru.bruimafia.donotforget"
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.bruimafia.donotforget"
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
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Android Jetpack: LifeCycle and ViewModel
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // Android Jetpack: Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.2")

    // Android Jetpack: Room Database
    implementation("androidx.room:room-runtime:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2")
    implementation("androidx.room:room-rxjava2:2.5.2") // optional - RxJava2 support for Room
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    // Android Jetpack: WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1") // Kotlin + coroutines
    implementation("androidx.work:work-rxjava2:2.8.1") // optional - RxJava2 support

    // SDP - a scalable size unit / аналог папок dimens
    implementation("com.intuit.sdp:sdp-android:1.1.0")

    // SmoothProgressBar
    implementation("com.github.castorflex.smoothprogressbar:library:1.1.0")

    // Календарь и часы
    implementation("com.github.swnishan:materialdatetimepicker:1.0.0")

    // Окно выбора цвета
    implementation("com.github.Dhaval2404:ColorPicker:2.3")

    // Кнопка с прогресс-баром
    implementation("com.github.leandroborgesferreira:loading-button-android:2.3.0")
}