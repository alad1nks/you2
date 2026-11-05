import SwiftUI
import FirebaseCore

@main
struct iOSApp: App {
    private let authProvider: FirebaseAuthProvider
    private let cloudProvider: FirebaseCloudProvider

    init() {
        FirebaseApp.configure()
        authProvider = FirebaseAuthProvider()
        cloudProvider = FirebaseCloudProvider()
    }

    var body: some Scene {
        WindowGroup {
            ContentView(authProvider: authProvider, cloudProvider: cloudProvider)
        }
    }
}
