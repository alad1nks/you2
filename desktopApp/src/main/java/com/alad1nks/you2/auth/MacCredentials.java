package com.alad1nks.you2.auth;

import java.lang.foreign.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Function;

import static java.lang.foreign.ValueLayout.*;

/** Apple's SecItem API and CoreFoundation, called through the JDK. */
final class MacCredentials implements DesktopSessionStore {
    private final NativeCalls security = new NativeCalls("/System/Library/Frameworks/Security.framework/Security");
    private final NativeCalls cf = new NativeCalls("/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation");
    private final String service;
    private final String account;

    MacCredentials(String service, String account) { this.service = service; this.account = account; }

    @Override public String read() {
        return query(query -> {
            set(query, "kSecReturnData", cf.symbol("kCFBooleanTrue").reinterpret(ADDRESS.byteSize()).get(ADDRESS, 0));
            try (Arena arena = Arena.ofConfined()) {
                var result = arena.allocate(ADDRESS);
                int status = (int) security.call("SecItemCopyMatching", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS), query, result);
                if (status == -25300) return null;
                checkStatus(status);
                var data = result.get(ADDRESS, 0);
                try {
                    long size = (long) cf.call("CFDataGetLength", FunctionDescriptor.of(JAVA_LONG, ADDRESS), data);
                    if (size == 0) return null;
                    if (size < 0 || size > 16384) throw new IllegalStateException("Invalid credential size");
                    var pointer = (MemorySegment) cf.call("CFDataGetBytePtr", FunctionDescriptor.of(ADDRESS, ADDRESS), data);
                    byte[] bytes = pointer.reinterpret(size).toArray(JAVA_BYTE);
                    try { return new String(bytes, StandardCharsets.UTF_8); }
                    finally { Arrays.fill(bytes, (byte) 0); }
                } finally { release(data); }
            }
        });
    }

    @Override public void write(String token) {
        try (Arena arena = Arena.ofConfined()) {
            var bytes = arena.allocateFrom(token);
            try {
                var data = (MemorySegment) cf.call("CFDataCreate", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, JAVA_LONG),
                        MemorySegment.NULL, bytes, bytes.byteSize() - 1);
                requirePointer(data);
                try {
                    query(query -> {
                        var changes = dictionary();
                        try {
                            set(changes, "kSecValueData", data);
                            int status = (int) security.call("SecItemUpdate", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS), query, changes);
                            if (status == -25300) {
                                set(query, "kSecValueData", data);
                                checkStatus((int) security.call("SecItemAdd", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS), query, MemorySegment.NULL));
                            } else checkStatus(status);
                        } finally { release(changes); }
                        return null;
                    });
                } finally { release(data); }
            } finally { bytes.fill((byte) 0); }
        }
    }

    @Override public void clear() {
        query(query -> {
            int status = (int) security.call("SecItemDelete", FunctionDescriptor.of(JAVA_INT, ADDRESS), query);
            if (status != -25300) checkStatus(status);
            return null;
        });
    }

    private <T> T query(Function<MemorySegment, T> operation) {
        var query = dictionary();
        try {
            set(query, "kSecClass", symbol("kSecClassGenericPassword"));
            setString(query, "kSecAttrService", service);
            setString(query, "kSecAttrAccount", account);
            return operation.apply(query);
        } finally { release(query); }
    }

    private MemorySegment dictionary() {
        var value = (MemorySegment) cf.call("CFDictionaryCreateMutable",
                FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_LONG, ADDRESS, ADDRESS), MemorySegment.NULL, 0L,
                cf.symbol("kCFTypeDictionaryKeyCallBacks"), cf.symbol("kCFTypeDictionaryValueCallBacks"));
        requirePointer(value);
        return value;
    }

    private void setString(MemorySegment dictionary, String key, String value) {
        try (Arena arena = Arena.ofConfined()) {
            var string = (MemorySegment) cf.call("CFStringCreateWithCString", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, JAVA_INT),
                    MemorySegment.NULL, arena.allocateFrom(value), 0x08000100);
            requirePointer(string);
            try { set(dictionary, key, string); }
            finally { release(string); }
        }
    }

    private void set(MemorySegment dictionary, String key, MemorySegment value) {
        cf.call("CFDictionarySetValue", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS), dictionary, symbol(key), value);
    }

    private MemorySegment symbol(String name) { return security.symbol(name).reinterpret(ADDRESS.byteSize()).get(ADDRESS, 0); }
    private void release(MemorySegment value) { cf.call("CFRelease", FunctionDescriptor.ofVoid(ADDRESS), value); }
    private static void requirePointer(MemorySegment value) {
        if (value.equals(MemorySegment.NULL)) throw new IllegalStateException("Cannot allocate credential query");
    }
    private static void checkStatus(int status) {
        if (status != 0) throw new IllegalStateException("macOS credential operation failed (" + status + ")");
    }
}
