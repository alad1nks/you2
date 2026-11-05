import SwiftUI
import FirebaseCore

@main
struct iOSApp: App {
    private let authProvider: FirebaseAuthProvider

    init() {
        FirebaseApp.configure()
        authProvider = FirebaseAuthProvider()
    }

    var body: some Scene {
        WindowGroup {
            ContentView(authProvider: authProvider)
        }
    }
}
