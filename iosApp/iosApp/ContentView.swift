import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    let authProvider: NativeAuthProvider
    let cloudProvider: NativeCloudProvider
    let photoProvider: NativePhotoProvider
    @ObservedObject var systemBars: SystemBarsState

    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.mainViewController(authProvider: authProvider, cloudProvider: cloudProvider, photoProvider: photoProvider, systemBars: systemBars)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    let authProvider: NativeAuthProvider
    let cloudProvider: NativeCloudProvider
    let photoProvider: NativePhotoProvider
    @ObservedObject var systemBars: SystemBarsState

    var body: some View {
        ComposeView(authProvider: authProvider, cloudProvider: cloudProvider, photoProvider: photoProvider, systemBars: systemBars)
            .ignoresSafeArea()
            .statusBarHidden(systemBars.hidden)
            .persistentSystemOverlays(systemBars.hidden ? .hidden : .automatic)
    }
}

final class SystemBarsState: ObservableObject, NativeSystemBarsProvider {
    @Published var hidden = false

    func setHidden(hidden: Bool) {
        if self.hidden != hidden { self.hidden = hidden }
    }
}
