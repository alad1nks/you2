import FirebaseAuth
import FirebaseCore
import GoogleSignIn
import UIKit
import Foundation
import Shared

/// Firebase owns persisted sessions; Google credentials are held only during token delivery.
final class FirebaseAuthProvider: NativeAuthProvider {
    private let auth: Auth
    private var googleCredential: (idToken: String, credential: AuthCredential)?

    init(auth: Auth = Auth.auth()) {
        self.auth = auth
    }

    var currentUser: NativeAuthUser? {
        Self.identity(auth.currentUser)
    }

    func observeUser(onChange: @escaping (NativeAuthUser?) -> Void) -> () -> Void {
        var active = true
        let handle = auth.addStateDidChangeListener { _, user in
            guard active else { return }
            onChange(Self.identity(user))
        }
        return { [auth] in
            if active {
                active = false
                auth.removeStateDidChangeListener(handle)
            }
        }
    }

    func signIn(email: String, password: String, complete: @escaping (NativeAuthFailure?) -> Void) {
        auth.signIn(withEmail: email, password: password) { _, error in
            complete(Self.failure(error))
        }
    }

    func launchGoogleSignIn(complete: @escaping (String?, NativeGoogleSignInFailure?) -> Void) {
        googleCredential = nil
        guard let clientID = FirebaseApp.app()?.options.clientID, !clientID.isEmpty,
              let presenter = Self.presentingViewController() else {
            complete(nil, .failed)
            return
        }
        // A mismatched callback scheme makes the SDK raise an Objective-C exception.
        let scheme = clientID.split(separator: ".").reversed().joined(separator: ".")
        let urlTypes = Bundle.main.object(forInfoDictionaryKey: "CFBundleURLTypes") as? [[String: Any]]
        let schemes = urlTypes?.flatMap { $0["CFBundleURLSchemes"] as? [String] ?? [] } ?? []
        guard schemes.contains(scheme) else {
            complete(nil, .failed)
            return
        }
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.signIn(withPresenting: presenter) { [self] result, error in
            if let error = error as NSError? {
                let cancelled = error.domain == kGIDSignInErrorDomain &&
                    error.code == GIDSignInError.Code.canceled.rawValue
                complete(nil, cancelled ? .cancelled : .failed)
                return
            }
            guard let user = result?.user, let token = user.idToken?.tokenString, !token.isEmpty else {
                complete(nil, .failed)
                return
            }
            googleCredential = (
                token,
                GoogleAuthProvider.credential(withIDToken: token, accessToken: user.accessToken.tokenString)
            )
            complete(token, nil)
            // The shared flow consumes the credential synchronously, or ignores a stale result.
            googleCredential = nil
        }
    }

    func signInWithGoogle(idToken: String, complete: @escaping (NativeAuthFailure?) -> Void) {
        guard let pending = googleCredential, pending.idToken == idToken else {
            complete(.unknown)
            return
        }
        googleCredential = nil
        auth.signIn(with: pending.credential) { _, error in
            complete(Self.failure(error))
        }
    }

    private static func presentingViewController() -> UIViewController? {
        let scene = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive && $0.windows.contains(where: \.isKeyWindow) }
        var controller = scene?.windows.first(where: \.isKeyWindow)?.rootViewController
        while let presented = controller?.presentedViewController {
            controller = presented
        }
        return controller
    }

    func register(email: String, password: String, complete: @escaping (NativeAuthFailure?) -> Void) {
        auth.createUser(withEmail: email, password: password) { _, error in
            complete(Self.failure(error))
        }
    }

    func resetPassword(email: String, complete: @escaping (NativeAuthFailure?) -> Void) {
        auth.sendPasswordReset(withEmail: email) { error in
            // Match Android: do not reveal whether an email address has an account.
            if let error = error as NSError?,
               error.domain == AuthErrorDomain,
               AuthErrorCode(rawValue: error.code) == .userNotFound {
                complete(nil)
            } else {
                complete(Self.failure(error))
            }
        }
    }

    func signOut() -> NativeAuthFailure? {
        do {
            try auth.signOut()
            googleCredential = nil
            GIDSignIn.sharedInstance.signOut()
            return nil
        } catch {
            return Self.failure(error)
        }
    }

    private static func identity(_ user: FirebaseAuth.User?) -> NativeAuthUser? {
        guard let user else { return nil }
        return NativeAuthUser(id: user.uid, email: user.email ?? "")
    }

    private static func failure(_ error: Error?) -> NativeAuthFailure? {
        guard let error = error as NSError? else { return nil }
        guard error.domain == AuthErrorDomain else { return .unknown }
        switch AuthErrorCode(rawValue: error.code) {
        case .invalidEmail: return .email
        case .wrongPassword, .userNotFound, .invalidCredential, .userDisabled,
             .userTokenExpired, .invalidUserToken: return .credentials
        case .weakPassword: return .weakPassword
        case .emailAlreadyInUse, .accountExistsWithDifferentCredential: return .emailInUse
        case .networkError: return .network
        case .tooManyRequests: return .tooManyRequests
        default: return .unknown
        }
    }
}
