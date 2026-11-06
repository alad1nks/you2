import FirebaseAuth
import FirebaseFirestore
import FirebaseStorage
import Foundation
import Shared

/// Firebase SDK objects stay in the Apple host; the shared repository owns UI state and retries.
final class FirebaseCloudProvider: NativeCloudProvider {
    private let db = Firestore.firestore()
    @MainActor private lazy var images = WishImageUploads()

    func doNewId() -> String { UUID().uuidString.replacingOccurrences(of: "-", with: "").lowercased() }

    func observe(userId: String, onChange: @escaping (NativeCloudSnapshot) -> Void) -> () -> Void {
        let session = CloudSession(db: db, uid: userId, emit: onChange)
        session.start()
        Task { @MainActor in await images.cleanup(userId) }
        return { session.close() }
    }

    func write(operation: String, userId: String, coupleId: String?, documentId: String,
               fields: [String: Any], complete: @escaping (NativeCloudFailure?) -> Void) {
        guard Auth.auth().currentUser?.uid == userId else { complete(.access); return }
        let profile = db.collection("users").document(userId)
        let finish: (Error?) -> Void = { complete(Self.failure($0, invitation: operation == "join" || operation == "invite")) }
        if operation == "rename" {
            profile.updateData(["name": fields["name"] ?? ""], completion: finish)
            return
        }
        if operation == "invite" || operation == "join" {
            let invite = db.collection("invites").document(documentId)
            db.runTransaction({ [db] tx, errorPointer in
                do {
                    guard Auth.auth().currentUser?.uid == userId else { throw CloudError.access }
                    let me = try tx.getDocument(profile)
                    if operation == "invite" {
                        guard let pairId = fields["pairId"] as? String,
                              me.get("coupleId") is NSNull || me.get("coupleId") as? String == pairId else {
                            throw CloudError.invite
                        }
                        let pair = db.collection("couples").document(pairId)
                        let existing = try tx.getDocument(pair)
                        if existing.exists {
                            guard existing.get("memberIds") as? [String] == [userId] else { throw CloudError.invite }
                            tx.updateData(["inviteId": documentId], forDocument: pair)
                        } else {
                            tx.setData(["memberIds": [userId], "inviteId": documentId,
                                        "createdAt": FieldValue.serverTimestamp()], forDocument: pair)
                        }
                        tx.updateData(["name": fields["name"] ?? "", "coupleId": pairId], forDocument: profile)
                        tx.setData(["ownerId": userId, "coupleId": pairId,
                                    "createdAt": FieldValue.serverTimestamp(),
                                    "expiresAt": Timestamp(date: Date().addingTimeInterval(24 * 60 * 60)),
                                    "usedBy": NSNull()], forDocument: invite)
                    } else {
                        let invitation = try tx.getDocument(invite)
                        guard me.get("coupleId") is NSNull,
                              let owner = invitation.get("ownerId") as? String, owner != userId,
                              invitation.get("usedBy") is NSNull,
                              let expiry = invitation.get("expiresAt") as? Timestamp, expiry.dateValue() > Date(),
                              let pairId = invitation.get("coupleId") as? String else { throw CloudError.invite }
                        // The invitation is the capability: outsiders cannot read the couple before joining.
                        tx.updateData(["memberIds": FieldValue.arrayUnion([userId])],
                                      forDocument: db.collection("couples").document(pairId))
                        tx.updateData(["name": fields["name"] ?? "", "coupleId": pairId], forDocument: profile)
                        tx.updateData(["usedBy": userId], forDocument: invite)
                    }
                    return nil
                } catch { errorPointer?.pointee = error as NSError; return nil }
            }, completion: { _, error in finish(error) })
            return
        }
        guard let coupleId else { complete(.data); return }
        let wish = db.collection("couples").document(coupleId).collection("wishes").document(documentId)
        if operation == "create" || operation == "edit" || operation == "delete" {
            Task { @MainActor in
                do {
                    if operation == "delete" {
                        try await images.delete(uid: userId, wish: wish)
                    } else {
                        try await images.save(operation: operation, uid: userId, wish: wish, fields: fields)
                    }
                    complete(nil)
                } catch { complete(Self.failure(error)) }
            }
            return
        }
        switch operation {
        case "complete":
            // Concurrent completion preserves the first memory.
            db.runTransaction({ tx, errorPointer in
                do {
                    guard Auth.auth().currentUser?.uid == userId else { throw CloudError.access }
                    let old = try tx.getDocument(wish)
                    guard old.exists else { throw CloudError.data }
                    if old.get("completion") is NSNull {
                        tx.updateData(["completion": ["date": fields["date"] ?? "", "authorId": userId, "memory": ""],
                                       "updatedAt": FieldValue.serverTimestamp()], forDocument: wish)
                    }
                    return nil
                } catch { errorPointer?.pointee = error as NSError; return nil }
            }, completion: { _, error in finish(error) })
        case "restore":
            wish.updateData(["completion": NSNull(), "updatedAt": FieldValue.serverTimestamp()], completion: finish)
        case "memory":
            wish.updateData(["completion.memory": fields["memory"] ?? "", "updatedAt": FieldValue.serverTimestamp()], completion: finish)
        default: complete(.data)
        }
    }

    fileprivate static func failure(_ error: Error?, invitation: Bool = false) -> NativeCloudFailure? {
        guard let error else { return nil }
        if let local = error as? CloudError {
            switch local {
            case .access: return .access
            case .invite: return .invite
            case .data: return .data
            }
        }
        if let error = error as? PhotoWriteError { return error == .access ? .access : .data }
        let ns = error as NSError
        if ns.domain == StorageErrorDomain {
            switch StorageErrorCode(rawValue: ns.code) {
            case .unauthorized, .unauthenticated: return .access
            case .retryLimitExceeded: return .network
            default: return .data
            }
        }
        guard ns.domain == FirestoreErrorDomain else { return .unknown }
        switch FirestoreErrorCode.Code(rawValue: ns.code) {
        case .permissionDenied: return invitation ? .invite : .access
        case .unauthenticated: return .access
        case .unavailable, .deadlineExceeded: return .network
        case .notFound, .failedPrecondition, .invalidArgument: return invitation ? .invite : .data
        default: return .unknown
        }
    }
}

private enum CloudError: Error { case access, invite, data }

/// One subscription per identity. Closing it also invalidates callbacks already queued by Firebase.
private final class CloudSession {
    let db: Firestore
    let uid: String
    let emit: (NativeCloudSnapshot) -> Void
    var active = true
    var listeners: [ListenerRegistration] = []
    var pairListeners: [ListenerRegistration] = []
    var pairId: String?
    var partnerId: String?
    var name = ""
    var partnerName: String?
    var wishes: [NativeCloudDocument] = []
    var loaded = false
    var cache = false
    var pending = false
    var creatingProfile = false
    var phase = NativeCloudPhase.loading
    var invite: String?

    init(db: Firestore, uid: String, emit: @escaping (NativeCloudSnapshot) -> Void) {
        self.db = db; self.uid = uid; self.emit = emit
    }

    var current: Bool { active && Auth.auth().currentUser?.uid == uid }

    func start() {
        let profile = db.collection("users").document(uid)
        listeners.append(profile.addSnapshotListener(includeMetadataChanges: true) { [weak self] snapshot, error in
            guard let self, self.current else { return }
            guard let snapshot, error == nil else { self.fail(error); return }
            if !snapshot.exists {
                // A cache miss does not prove the account is new.
                if !snapshot.metadata.isFromCache && !self.creatingProfile {
                    self.creatingProfile = true
                    self.db.runTransaction({ tx, errorPointer in
                        do {
                            guard Auth.auth().currentUser?.uid == self.uid else { throw CloudError.access }
                            let existing = try tx.getDocument(profile)
                            if !existing.exists {
                                tx.setData(["name": "", "coupleId": NSNull()], forDocument: profile)
                            }
                        } catch { errorPointer?.pointee = error as NSError }
                        return nil
                    }, completion: { _, error in
                        if self.current, let error { self.fail(error) }
                    })
                }
                return
            }
            guard let name = snapshot.get("name") as? String else { self.fail(CloudError.data); return }
            self.name = name
            if let id = snapshot.get("coupleId") as? String {
                if id != self.pairId { self.observePair(id) } else { self.publish() }
            } else if snapshot.get("coupleId") is NSNull {
                self.clearPair()
                self.phase = .unpaired
                self.publish()
            } else { self.fail(CloudError.data) }
        })
    }

    func observePair(_ id: String) {
        clearPair()
        pairId = id
        phase = .loading
        publish()
        pairListeners.append(db.collection("couples").document(id).addSnapshotListener { [weak self] snapshot, error in
            guard let self, self.current, self.pairId == id else { return }
            guard let snapshot, error == nil else { self.fail(error); return }
            guard let members = snapshot.get("memberIds") as? [String], members.contains(self.uid),
                  Set(members).count == members.count, (1...2).contains(members.count) else {
                self.fail(CloudError.data); return
            }
            if members.count == 1 {
                guard let code = snapshot.get("inviteId") as? String else { self.fail(CloudError.data); return }
                self.invite = code
                self.phase = .unpaired
                self.publish()
            } else if self.partnerId == nil, let other = members.first(where: { $0 != self.uid }) {
                self.partnerId = other
                self.invite = nil
                self.observeContent(id, partner: other)
            }
        })
    }

    func observeContent(_ id: String, partner: String) {
        pairListeners.append(db.collection("users").document(partner).addSnapshotListener { [weak self] snapshot, error in
            guard let self, self.current, self.pairId == id else { return }
            guard error == nil, let name = snapshot?.get("name") as? String, !name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
                self.fail(error ?? CloudError.data); return
            }
            self.partnerName = name
            self.publish()
        })
        pairListeners.append(db.collection("couples").document(id).collection("wishes")
            .order(by: "createdAt", descending: true)
            .addSnapshotListener(includeMetadataChanges: true) { [weak self] snapshot, error in
                guard let self, self.current, self.pairId == id else { return }
                guard let snapshot, error == nil else { self.fail(error); return }
                self.wishes = snapshot.documents.map {
                    // Timestamps are SDK-only; WishDocument uses the calendar date fields.
                    var fields = $0.data()
                    fields.removeValue(forKey: "createdAt")
                    fields.removeValue(forKey: "updatedAt")
                    return NativeCloudDocument(id: $0.documentID, fields: Self.withoutNulls(fields))
                }
                self.loaded = true
                self.cache = snapshot.metadata.isFromCache
                self.pending = snapshot.metadata.hasPendingWrites
                self.publish()
            })
    }

    static func withoutNulls(_ fields: [String: Any]) -> [String: Any] {
        fields.compactMapValues { value in
            if value is NSNull { return nil }
            if let nested = value as? [String: Any] { return withoutNulls(nested) }
            return value
        }
    }

    func publish(_ failure: NativeCloudFailure? = nil) {
        guard current else { return }
        if phase != .error, partnerName != nil, loaded { phase = .ready }
        emit(NativeCloudSnapshot(phase: phase, displayName: name, coupleId: pairId,
                                 inviteCode: invite, partnerId: partnerId, partnerName: partnerName,
                                 wishes: wishes, fromCache: cache, pendingWrites: pending, failure: failure))
    }

    func fail(_ error: Error?) {
        phase = .error
        publish(FirebaseCloudProvider.failure(error) ?? .unknown)
        close()
    }

    func clearPair() {
        pairListeners.forEach { $0.remove() }
        pairListeners.removeAll()
        pairId = nil; partnerId = nil; partnerName = nil; invite = nil
        wishes = []; loaded = false; cache = false; pending = false
    }

    func close() {
        active = false
        listeners.forEach { $0.remove() }
        listeners.removeAll()
        clearPair()
    }
}
