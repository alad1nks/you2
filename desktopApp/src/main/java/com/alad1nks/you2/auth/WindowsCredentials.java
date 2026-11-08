package com.alad1nks.you2.auth;

import java.lang.foreign.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static java.lang.foreign.ValueLayout.*;
import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

/** Official WinCred API, preserving the previous target name and UTF-16LE token encoding. */
final class WindowsCredentials implements DesktopSessionStore {
    private final NativeCalls api = new NativeCalls("Advapi32.dll");
    private final String target;
    private final String account;
    private static final StructLayout CREDENTIAL = MemoryLayout.structLayout(
            JAVA_INT.withName("flags"), JAVA_INT.withName("type"), ADDRESS.withName("target"),
            ADDRESS.withName("comment"), JAVA_INT.withName("timeLow"), JAVA_INT.withName("timeHigh"),
            JAVA_INT.withName("size"), MemoryLayout.paddingLayout(4), ADDRESS.withName("blob"),
            JAVA_INT.withName("persist"), JAVA_INT.withName("count"), ADDRESS.withName("attributes"),
            ADDRESS.withName("alias"), ADDRESS.withName("user"));

    WindowsCredentials(String service, String account) {
        if (ADDRESS.byteSize() != 8) throw new IllegalStateException("Desktop requires a 64-bit runtime");
        this.target = service + "|" + account;
        this.account = account;
    }

    private static long offset(String field) { return CREDENTIAL.byteOffset(groupElement(field)); }

    @Override public String read() {
        try (Arena arena = Arena.ofConfined()) {
            var result = arena.allocate(ADDRESS);
            var status = api.windowsCall("CredReadW", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, ADDRESS),
                    arena.allocateFrom(target, StandardCharsets.UTF_16LE), 1, 0, result);
            if (!status.foundOrSucceeded()) return null;
            var pointer = result.get(ADDRESS, 0);
            try {
                var credential = pointer.reinterpret(CREDENTIAL.byteSize());
                int size = credential.get(JAVA_INT, offset("size"));
                if (size == 0) return null;
                if (size < 0 || size > 2560 || size % 2 != 0) throw new IllegalStateException("Invalid credential size");
                byte[] bytes = credential.get(ADDRESS, offset("blob")).reinterpret(size).toArray(JAVA_BYTE);
                try { return new String(bytes, StandardCharsets.UTF_16LE); }
                finally { Arrays.fill(bytes, (byte) 0); }
            } finally {
                api.call("CredFree", FunctionDescriptor.ofVoid(ADDRESS), pointer);
            }
        }
    }

    @Override public void write(String refreshToken) {
        byte[] bytes = refreshToken.getBytes(StandardCharsets.UTF_16LE);
        try (Arena arena = Arena.ofConfined()) {
            if (bytes.length == 0 || bytes.length > 2560) throw new IllegalArgumentException("Invalid credential size");
            var blob = arena.allocateFrom(JAVA_BYTE, bytes);
            try {
                var credential = arena.allocate(CREDENTIAL);
                credential.set(JAVA_INT, offset("type"), 1);
                credential.set(ADDRESS, offset("target"), arena.allocateFrom(target, StandardCharsets.UTF_16LE));
                credential.set(JAVA_INT, offset("size"), bytes.length);
                credential.set(ADDRESS, offset("blob"), blob);
                credential.set(JAVA_INT, offset("persist"), 2);
                credential.set(ADDRESS, offset("user"), arena.allocateFrom(account, StandardCharsets.UTF_16LE));
                api.windowsCall("CredWriteW", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT), credential, 0)
                        .requireSuccess();
            } finally { blob.fill((byte) 0); }
        } finally { Arrays.fill(bytes, (byte) 0); }
    }

    @Override public void clear() {
        try (Arena arena = Arena.ofConfined()) {
            api.windowsCall("CredDeleteW", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT),
                    arena.allocateFrom(target, StandardCharsets.UTF_16LE), 1, 0).foundOrSucceeded();
        }
    }
}
