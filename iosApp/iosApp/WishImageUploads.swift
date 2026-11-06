import FirebaseAuth
import FirebaseFirestore
import FirebaseStorage
import Foundation

/// Private Storage paths only. Pending cleanup survives process termination.
@MainActor
final class WishImageUploads {
    private let db = Firestore.firestore()
    private let storage = Storage.storage()
    private var active = Set<String>()
    private var uploaded: [String: String] = [:]

    private func check(_ uid: String) throws {
        guard Auth.auth().currentUser?.uid == uid else { throw PhotoWriteError.access }
    }

    private func queue(_ uid: String) -> Set<String> {
        Set(UserDefaults.standard.stringArray(forKey: "image-cleanup-" + uid) ?? [])
    }

    private func track(_ uid: String, _ paths: Set<String>) {
        UserDefaults.standard.set(Array(queue(uid).union(paths)), forKey: "image-cleanup-" + uid)
    }

    nonisolated static func paths(_ content: [String: Any]) -> Set<String> {
        Set(([content["cover"] as? String].compactMap { $0 } + (content["attachments"] as? [String] ?? []))
            .filter { $0.hasPrefix("couples/") })
    }

    private func transaction(_ work: @escaping (Transaction, NSErrorPointer) -> Void) async throws {
        _ = try await db.runTransaction { transaction, error in
            work(transaction, error)
            return nil
        }
    }

    func save(operation: String, uid: String, wish: DocumentReference, fields: [String: Any]) async throws {
        try check(uid)
        guard var content = fields["content"] as? [String: Any],
              let attachments = content["attachments"] as? [String], attachments.count <= 12 else {
            throw PhotoWriteError.data
        }
        let cover = content["cover"] as? String
        if operation == "create" {
            var initial = content
            initial["cover"] = cover?.hasPrefix("file:") == true ? NSNull() : (cover as Any? ?? NSNull())
            initial["attachments"] = attachments.filter { !$0.hasPrefix("file:") }
            let seed = initial
            try await transaction { tx, errorPointer in
                do {
                    guard Auth.auth().currentUser?.uid == uid else { throw PhotoWriteError.access }
                    if try !tx.getDocument(wish).exists {
                        tx.setData(["authorId": uid, "createdOn": fields["createdOn"] ?? "",
                                    "content": seed, "completion": NSNull(),
                                    "createdAt": FieldValue.serverTimestamp(),
                                    "updatedAt": FieldValue.serverTimestamp()], forDocument: wish)
                    }
                } catch { errorPointer?.pointee = error as NSError }
            }
            // A create without local photos is complete, including retries after an uncertain response.
            if cover?.hasPrefix("file:") != true && !attachments.contains(where: { $0.hasPrefix("file:") }) { return }
        }
        var protected = Set<String>()
        defer { active.subtract(protected) }
        func resolve(_ source: String) async throws -> String {
            guard source.hasPrefix("file:") else { return source }
            try check(uid)
            let key = uid + "/" + wish.path + "/" + source
            if let path = uploaded[key] {
                active.insert(path); protected.insert(path)
                return path
            }
            guard let url = URL(string: source), url.isFileURL,
                  let size = try url.resourceValues(forKeys: [.fileSizeKey]).fileSize,
                  (1...5_242_880).contains(size) else { throw PhotoWriteError.data }
            let path = wish.path + "/" + UUID().uuidString + ".jpg"
            active.insert(path); protected.insert(path)
            track(uid, [path])
            let metadata = StorageMetadata()
            metadata.contentType = "image/jpeg"
            metadata.contentEncoding = "identity"
            metadata.contentDisposition = "inline"
            _ = try await storage.reference().child(path).putFileAsync(from: url, metadata: metadata)
            try check(uid)
            uploaded[key] = path
            return path
        }
        if let cover { content["cover"] = try await resolve(cover) } else { content["cover"] = NSNull() }
        var resolved: [String] = []
        for source in attachments { resolved.append(try await resolve(source)) }
        content["attachments"] = resolved
        try check(uid)
        let finalContent = content
        let newPaths = protected
        // Track old paths before the transaction. Cleanup checks fresh server references before deletion.
        let old = try await wish.getDocument(source: .server)
        track(uid, Self.paths(old.get("content") as? [String: Any] ?? [:]))
        try await transaction { tx, errorPointer in
            do {
                guard Auth.auth().currentUser?.uid == uid else { throw PhotoWriteError.access }
                let snapshot = try tx.getDocument(wish)
                guard snapshot.exists else { throw PhotoWriteError.data }
                let oldPaths = Self.paths(snapshot.get("content") as? [String: Any] ?? [:])
                guard Self.paths(finalContent).isSubset(of: oldPaths.union(newPaths)) else { throw PhotoWriteError.data }
                tx.updateData(["content": finalContent, "updatedAt": FieldValue.serverTimestamp()], forDocument: wish)
            } catch { errorPointer?.pointee = error as NSError }
        }
        active.subtract(protected)
        await cleanup(uid)
    }

    func delete(uid: String, wish: DocumentReference) async throws {
        try check(uid)
        let snapshot = try await wish.getDocument(source: .server)
        track(uid, Self.paths(snapshot.get("content") as? [String: Any] ?? [:]))
        try check(uid)
        try await wish.delete()
        await cleanup(uid)
    }

    func cleanup(_ uid: String) async {
        for path in queue(uid) {
            do {
                try check(uid)
                if active.contains(path) { continue }
                let document = db.document(String(path.prefix(upTo: path.lastIndex(of: "/")!)))
                let snapshot = try await document.getDocument(source: .server)
                try check(uid)
                if active.contains(path) { continue }
                if !Self.paths(snapshot.get("content") as? [String: Any] ?? [:]).contains(path) {
                    do { try await storage.reference().child(path).delete() }
                    catch {
                        guard (error as NSError).code == StorageErrorCode.objectNotFound.rawValue else { throw error }
                    }
                }
                UserDefaults.standard.set(Array(queue(uid).subtracting([path])), forKey: "image-cleanup-" + uid)
                uploaded = uploaded.filter { $0.value != path }
            } catch { /* Keep failed cleanup for the next save or session. */ }
        }
    }
}

enum PhotoWriteError: Error { case access, data }
