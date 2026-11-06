import FirebaseAuth
import FirebaseStorage
import ImageIO
import PhotosUI
import Shared
import UIKit
import UniformTypeIdentifiers

final class FirebasePhotoProvider: NativePhotoProvider {
    func pick(maxImages: Int32, complete: @escaping ([String]?) -> Void) -> () -> Void {
        let request = PhotoSelection(limit: Int(maxImages), complete: complete)
        request.present()
        return { request.cancel() }
    }

    func load(source: String, complete: @escaping (Data?) -> Void) -> () -> Void {
        var active = true
        let uid = Auth.auth().currentUser?.uid
        let finish: (Data?) -> Void = { data in
            DispatchQueue.global(qos: .userInitiated).async {
                // Bound decoded dimensions as well as downloaded bytes.
                let jpeg = data.flatMap { PhotoSelection.jpeg($0, dimension: 2048) }
                DispatchQueue.main.async {
                    guard active, Auth.auth().currentUser?.uid == uid else { return }
                    complete(jpeg)
                }
            }
        }
        var task: StorageDownloadTask?
        if let url = URL(string: source), url.isFileURL {
            DispatchQueue.global(qos: .userInitiated).async { finish(try? Data(contentsOf: url)) }
        } else if source.hasPrefix("couples/"), uid != nil {
            task = Storage.storage().reference().child(source).getData(maxSize: 5_242_880) { data, _ in finish(data) }
        } else {
            finish(nil)
        }
        return { active = false; task?.cancel() }
    }
}

private final class PhotoSelection: NSObject, PHPickerViewControllerDelegate {
    let limit: Int
    var complete: (([String]?) -> Void)?
    weak var picker: PHPickerViewController?

    init(limit: Int, complete: @escaping ([String]?) -> Void) {
        self.limit = max(1, min(12, limit)); self.complete = complete
    }

    func present() {
        guard let scene = UIApplication.shared.connectedScenes.compactMap({ $0 as? UIWindowScene })
            .first(where: { $0.activationState == .foregroundActive }),
              var host = scene.windows.first(where: \.isKeyWindow)?.rootViewController else {
            complete?(nil); return
        }
        while let presented = host.presentedViewController { host = presented }
        var config = PHPickerConfiguration()
        config.filter = .images
        config.selectionLimit = limit
        config.selection = .ordered
        let picker = PHPickerViewController(configuration: config)
        picker.delegate = self
        self.picker = picker
        host.present(picker, animated: true)
    }

    func cancel() {
        complete = nil
        picker?.dismiss(animated: true)
    }

    func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        picker.dismiss(animated: true)
        guard !results.isEmpty else { finish([]); return }
        guard results.count <= limit else { finish(nil); return }
        let group = DispatchGroup()
        let lock = NSLock()
        var paths = [String?](repeating: nil, count: results.count)
        for (index, result) in results.enumerated() {
            group.enter()
            result.itemProvider.loadFileRepresentation(forTypeIdentifier: UTType.image.identifier) { url, _ in
                defer { group.leave() }
                guard let url, let source = CGImageSourceCreateWithURL(url as CFURL, nil),
                      let data = Self.jpeg(source, dimension: 2048), data.count <= 5_242_880 else { return }
                do {
                    let directory = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask)[0]
                        .appendingPathComponent("wish-images", isDirectory: true)
                    try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
                    let file = directory.appendingPathComponent(UUID().uuidString + ".jpg")
                    try data.write(to: file, options: .atomic)
                    lock.lock(); paths[index] = file.absoluteString; lock.unlock()
                } catch { }
            }
        }
        group.notify(queue: .main) {
            let files = paths.compactMap { $0 }
            guard self.complete != nil, files.count == results.count else {
                files.compactMap(URL.init(string:)).forEach { try? FileManager.default.removeItem(at: $0) }
                self.finish(nil)
                return
            }
            self.finish(files)
        }
    }

    func finish(_ paths: [String]?) {
        let callback = complete
        complete = nil
        callback?(paths)
    }

    static func jpeg(_ data: Data, dimension: Int) -> Data? {
        guard let source = CGImageSourceCreateWithData(data as CFData, nil) else { return nil }
        return jpeg(source, dimension: dimension)
    }

    static func jpeg(_ source: CGImageSource, dimension: Int) -> Data? {
        let options: [CFString: Any] = [kCGImageSourceCreateThumbnailFromImageAlways: true,
                                       kCGImageSourceCreateThumbnailWithTransform: true,
                                       kCGImageSourceThumbnailMaxPixelSize: dimension]
        guard let image = CGImageSourceCreateThumbnailAtIndex(source, 0, options as CFDictionary) else { return nil }
        // Rendering to a new JPEG drops original EXIF/GPS metadata and normalizes orientation.
        return UIImage(cgImage: image).jpegData(compressionQuality: 0.85)
    }
}
