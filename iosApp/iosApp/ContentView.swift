import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    let authProvider: NativeAuthProvider
    let cloudProvider: NativeCloudProvider

    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.mainViewController(authProvider: authProvider, cloudProvider: cloudProvider)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    let authProvider: NativeAuthProvider
    let cloudProvider: NativeCloudProvider

    var body: some View {
        ComposeView(authProvider: authProvider, cloudProvider: cloudProvider)
            .ignoresSafeArea()
    }
}
