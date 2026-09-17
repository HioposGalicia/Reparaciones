plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "icg.es.reparacionesjl"
    compileSdk = 36

    defaultConfig {
        applicationId = "icg.es.reparacionesjl"
        minSdk = 29
        targetSdk = 36
        versionCode = 9
        versionName = "9.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_KEY_PRO", "\"95476171f6ee8e112011a9e272c588410064fe14b8a0c7c0ecaefccfdd5ec9f5\"")
            buildConfigField("String", "API_URL_PRO", "\"https://intranet.joseluisjoyerias.net\"")
            buildConfigField("String", "API_KEY_PRE", "\"57b6fae459410be97bf120412f6e9f63422ad38f5a5266c29efa944783ccb512\"")
            buildConfigField("String", "API_URL_PRE", "\"https://preintranet.joseluisjoyerias.net\"")
        }
        release {
            buildConfigField("String", "API_KEY_PRO", "\"95476171f6ee8e112011a9e272c588410064fe14b8a0c7c0ecaefccfdd5ec9f5\"")
            buildConfigField("String", "API_URL_PRO", "\"https://intranet.joseluisjoyerias.net\"")
            buildConfigField("String", "API_KEY_PRE", "\"57b6fae459410be97bf120412f6e9f63422ad38f5a5266c29efa944783ccb512\"")
            buildConfigField("String", "API_URL_PRE", "\"https://preintranet.joseluisjoyerias.net\"")
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.gson)
}