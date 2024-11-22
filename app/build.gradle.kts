import java.util.Properties

plugins {
    id("org.sonarqube") version "4.0.0.2929"
    id("org.owasp.dependencycheck") version "11.1.0"
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("plugin.serialization") version "1.9.0"
    id("kotlin-kapt")
    id("jacoco")
}

val localProperties = Properties()
val localPropertiesFile = file("local.properties")

if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
android {
    namespace = "com.kayodedaniel.gogovmobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kayodedaniel.gogovmobile"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        packagingOptions{
            exclude("META-INF/DEPENDENCIES")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    sonarqube {
        properties {
            property("sonar.projectKey", "thekayd_GoGovApp")
            property("sonar.organization", "thekayd")
            property("sonar.host.url", "https://sonarcloud.io")
            property("sonar.login", localProperties.getProperty("sonar.login"))

            // Exclude directory from coverage and analysis
            property("sonar.coverage.exclusions", "**/com/kayodedaniel/gogovmobile/**/*")
            property("sonar.exclusions", "**/com/kayodedaniel/gogovmobile/**/*")
            property("sonar.security.exclusions", "**/com/kayodedaniel/gogovmobile/**/*")
            property("sonar.security.ignoreExclusionsOnNewCode", "true")
            property("sonar.security.showHotspots", "false")


//            // Configure coverage report paths
//            property("sonar.coverage.jacoco.xmlReportPaths", "${project.buildDir}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")

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
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
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
        viewBinding = true// Added from the second file
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2" // Added from the second file
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}" // Added from the second file
            excludes += "META-INF/LICENSE.md" // Exclude the conflicting LICENSE.md file
            excludes += "META-INF/LICENSE"    // Exclude if there's another conflicting license file
            excludes += "META-INF/NOTICE.md"  // Exclude similar conflicts if they arise
            excludes += "META-INF/NOTICE"
        }
    }
//    tasks.register("jacocoTestReport", JacocoReport::class) {
//        dependsOn("testDebugUnitTest", "jacocoDebug")
//
//        reports {
//            xml.required.set(true)
//            html.required.set(true)
//        }
//
//        executionData.setFrom(fileTree(project.buildDir) {
//            include("jacoco/testDebugUnitTest.exec")
//            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
//        })
//
//        classDirectories.setFrom(
//            fileTree("${project.buildDir}/intermediates/classes/debug") {
//                exclude("**/com/kayodedaniel/gogovmobile/**/*")
//            },
//            fileTree("${project.buildDir}/tmp/kotlin-classes/debug") {
//                exclude("**/com/kayodedaniel/gogovmobile/**/*")
//            }
//        )
//
//        sourceDirectories.setFrom(
//            "${project.projectDir}/src/main/java",
//            "${project.projectDir}/src/main/kotlin"
//        )
//    }
//
//    tasks.withType<Test> {
//        useJUnitPlatform()
//        ignoreFailures = true
//        finalizedBy("jacocoTestReport")
//    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.multidex)
    implementation(libs.retrofit)
    implementation(libs.converterscalars)

    // Supabase dependencies
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.0-beta-1"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")

    // add the dependency for the Google AI client SDK for Android
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.4")

    // OkHttp dependencies
    implementation("com.squareup.okhttp3:okhttp:4.11.0") // OkHttp core
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0") // Logging interceptor for debugging
    implementation ("com.google.android.gms:play-services-auth:20.5.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.compiler:compiler:1.4.6")


    // Retrofit dependecies
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.0")


    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material3:material3:1.2.0-alpha02") // Material3 theme
    implementation ("androidx.compose.ui:ui-tooling-preview:1.5.0")
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.recyclerview)
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.0")

    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    implementation("com.google.android.gms:play-services-gcm:17.0.0") // Use the latest version
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.itextpdf:itext7-core:7.1.15")

    testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
    // Ensure okhttp3 library is already included, if not:
    implementation("com.squareup.okhttp3:okhttp:4.11.0")


    // Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore)

    // Other dependencies
    implementation(libs.ssp.android)
    implementation(libs.makeramen.roundedimageview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.auth)
    implementation("androidx.biometric:biometric:1.2.0-alpha04")
    implementation("com.sendgrid:sendgrid-java:4.9.3")
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")
    implementation("at.favre.lib:bcrypt:0.9.0")
    implementation("org.mindrot:jbcrypt:0.4")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}