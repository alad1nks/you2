import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    let authProvider: NativeAuthProvider

    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.mainViewController(authProvider: authProvider)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    let authProvider: NativeAuthProvider

    var body: some View {
        ComposeView(authProvider: authProvider)
            .ignoresSafeArea()
    }
}
