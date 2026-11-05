import FirebaseAuth
import Foundation
import Shared

/// Firebase owns persisted credentials. Only identity and categorized errors cross into Kotlin.
final class FirebaseAuthProvider: NativeAuthProvider {
    private let auth: Auth

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
