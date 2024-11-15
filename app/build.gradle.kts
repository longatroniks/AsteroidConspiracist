plugins {
    id("com.android.application")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "dte.masteriot.mdp.asteroidconspiracist"
    compileSdk = 34

    defaultConfig {
        applicationId = "dte.masteriot.mdp.asteroidconspiracist"
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
    // ... other configurations ...
    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
            //excludes += "META-INF/androidx.legacy_legacy-support-v4.version"
        }
    }
    buildFeatures {
        viewBinding = true
    }


}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.activity:activity:1.9.3")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.android.support:support-annotations:28.0.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.versionedparcelable:versionedparcelable:1.1.1")
    implementation("androidx.core:core:1.13.0")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation("com.hivemq:hivemq-mqtt-client:1.3.3")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("com.github.tony19:logback-android:2.0.0")



    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("com.hivemq:hivemq-mqtt-client:1.3.3")

}