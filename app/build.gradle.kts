plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

android {
    namespace = "ru.bruimafia.donotforget"
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.bruimafia.donotforget"
        minSdk = 24
        targetSdk = 34
        versionCode = 24
        versionName = "2.2"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        setProperty("archivesBaseName", "pixabay_lite-$versionName-code$versionCode")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Android Jetpack: LifeCycle and ViewModel
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // Android Jetpack: Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")

    // Android Jetpack: Room Database
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2")

    // Android Jetpack: WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1") // Kotlin + coroutines

    // SDP - a scalable size unit / аналог папок dimens
    implementation("com.intuit.sdp:sdp-android:1.1.0")

    // SDP - a scalable size unit for text / аналог папок dimens
    implementation("com.intuit.ssp:ssp-android:1.1.0")

    // Календарь и часы
    implementation("com.github.swnishan:materialdatetimepicker:1.0.0")

    // Окно выбора цвета
    implementation("com.github.Dhaval2404:ColorPicker:2.3")

    // Кнопка с прогресс-баром
    implementation("com.github.leandroborgesferreira:loading-button-android:2.3.0")

    // Yandex AppMetrica SDK
    implementation("io.appmetrica.analytics:analytics:6.0.0")

    // Yandex Mobile Ads SDK
    implementation("com.yandex.android:mobileads:6.0.0")
    implementation("com.yandex.ads.mediation:mobileads-google:22.2.0.0") // Yandex адаптер мобильной медиации AdMob
    //implementation("com.yandex.ads.mediation:mobileads-admob:22.2.0.0")
    //implementation("com.google.android.gms:play-services-ads:22.1.0") // Google Mobile Ads

    // OneSignal App SDK
    implementation("com.onesignal:OneSignal:5.0.2")

    // Google In-App Billing
    implementation("com.android.billingclient:billing-ktx:6.0.1")

    // Google Play In-App Updates
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // Google Play In-App Review
    implementation("com.google.android.play:review-ktx:2.0.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
}