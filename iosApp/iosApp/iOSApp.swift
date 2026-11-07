import SwiftUI
import FirebaseCore
import GoogleSignIn

@main
struct iOSApp: App {
    private let authProvider: FirebaseAuthProvider
    private let cloudProvider: FirebaseCloudProvider
    @StateObject private var systemBars = SystemBarsState()
    private let photoProvider: FirebasePhotoProvider

    init() {
        FirebaseApp.configure()
        authProvider = FirebaseAuthProvider()
        cloudProvider = FirebaseCloudProvider()
        photoProvider = FirebasePhotoProvider()
    }

    var body: some Scene {
        WindowGroup {
            ContentView(authProvider: authProvider, cloudProvider: cloudProvider, photoProvider: photoProvider, systemBars: systemBars)
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
