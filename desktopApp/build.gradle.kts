import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractCheckNativeDistributionRuntime
import org.jetbrains.compose.desktop.application.tasks.AbstractJvmToolOperationTask
import org.jetbrains.compose.desktop.application.tasks.AbstractProguardTask
import org.jetbrains.compose.desktop.application.tasks.AbstractSuggestModulesTask

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.AMAZON)
    }
}

val desktopJava =
    javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.AMAZON)
    }

tasks.withType<Test>().configureEach {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)

    implementation(libs.compose.uiToolingPreview)
    testImplementation(libs.kotlin.test)
}

// Desktop OAuth clients are public installed-app credentials, bundled for packaged launches too.
// Keep the downloaded source JSON local, as with the Android and Apple Firebase configuration.
tasks.processResources {
    from(projectDir) {
        include("client_secret_*.json")
        rename { "you2-google-desktop-client.json" }
    }
    duplicatesStrategy = DuplicatesStrategy.FAIL
}

compose.desktop {
    application {
        mainClass = "com.alad1nks.you2.MainKt"
        jvmArgs += "--enable-native-access=ALL-UNNAMED"

        nativeDistributions {
            modules("java.net.http", "jdk.httpserver", "jdk.unsupported", "java.security.jgss")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.alad1nks.you2"
            packageVersion = "1.0.0"
            windows { iconFile.set(project.file("icons/app-icon.ico")) }
            macOS { iconFile.set(project.file("icons/app-icon.icns")) }
            linux { iconFile.set(project.file("icons/app-icon.png")) }
        }
    }
}

// Compose's application.javaHome accepts only an eager String. Bind its task properties instead.
// Compose registers these tasks after evaluation, so apply our lazy providers after its defaults.
afterEvaluate {
    val desktopJavaHome = desktopJava.map { it.metadata.installationPath.asFile.absolutePath }
    tasks.withType<AbstractCheckNativeDistributionRuntime>().configureEach {
        jdkHome.set(desktopJavaHome)
    }
    tasks.withType<AbstractJvmToolOperationTask>().configureEach {
        javaHome.set(desktopJavaHome)
    }
    tasks.withType<AbstractProguardTask>().configureEach {
        javaHome.set(desktopJavaHome)
    }
    tasks.withType<AbstractSuggestModulesTask>().configureEach {
        javaHome.set(desktopJavaHome)
    }
    tasks.withType<JavaExec>().configureEach {
        javaLauncher.set(desktopJava)
        // Compose explicitly sets the daemon-JDK executable; replace it only when executing.
        doFirst {
            setExecutable(javaLauncher.get().executablePath.asFile.absolutePath)
        }
    }
}
