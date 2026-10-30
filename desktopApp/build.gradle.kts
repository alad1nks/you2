import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.alad1nks.you2.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.alad1nks.you2"
            packageVersion = "1.0.0"
            windows { iconFile.set(project.file("icons/app-icon.ico")) }
            macOS { iconFile.set(project.file("icons/app-icon.icns")) }
            linux { iconFile.set(project.file("icons/app-icon.png")) }
        }
    }
}
