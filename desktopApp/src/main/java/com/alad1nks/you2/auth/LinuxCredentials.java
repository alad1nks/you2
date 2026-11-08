package com.alad1nks.you2.auth;

import java.lang.foreign.*;
import java.util.function.Function;

import static java.lang.foreign.ValueLayout.*;

/** GNOME's libsecret API, called through the JDK. Requires a running Secret Service provider. */
final class LinuxCredentials implements DesktopSessionStore {
    private final NativeCalls secret = new NativeCalls("libsecret-1.so.0");
    private final NativeCalls glib = new NativeCalls("libglib-2.0.so.0");
    private final String service;
    private final String account;

    LinuxCredentials(String service, String account) { this.service = service; this.account = account; }

    @Override public String read() {
        return withSchema(context -> {
            var password = (MemorySegment) secret.variadic("secret_password_lookup_sync",
                    FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS),
                    3, context.schema, MemorySegment.NULL, context.error, context.serviceKey, context.serviceValue,
                    context.accountKey, context.accountValue, MemorySegment.NULL);
            try {
                context.checkError();
                return password.equals(MemorySegment.NULL) ? null : password.reinterpret(16385).getString(0);
            } finally {
                if (!password.equals(MemorySegment.NULL))
                    secret.call("secret_password_free", FunctionDescriptor.ofVoid(ADDRESS), password);
            }
        });
    }

    @Override public void write(String token) {
        withSchema(context -> {
            var password = context.arena.allocateFrom(token);
            try {
                int saved = (int) secret.variadic("secret_password_store_sync",
                        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS,
                                ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS),
                        6, context.schema, MemorySegment.NULL, context.arena.allocateFrom("You2 Firebase session"),
                        password, MemorySegment.NULL, context.error, context.serviceKey, context.serviceValue,
                        context.accountKey, context.accountValue, MemorySegment.NULL);
                context.checkError();
                if (saved == 0) throw new IllegalStateException("Linux credential write failed");
            } finally { password.fill((byte) 0); }
            return null;
        });
    }

    @Override public void clear() {
        withSchema(context -> {
            secret.variadic("secret_password_clear_sync",
                    FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS),
                    3, context.schema, MemorySegment.NULL, context.error, context.serviceKey, context.serviceValue,
                    context.accountKey, context.accountValue, MemorySegment.NULL);
            context.checkError(); // False without a GError means the entry was already absent.
            return null;
        });
    }

    private <T> T withSchema(Function<Context, T> operation) {
        try (Arena arena = Arena.ofConfined()) {
            var serviceKey = arena.allocateFrom("service");
            var accountKey = arena.allocateFrom("account");
            var schema = (MemorySegment) secret.variadic("secret_schema_new",
                    FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, ADDRESS),
                    2, arena.allocateFrom("com.alad1nks.you2.firebase"), 0, serviceKey, 0, accountKey, 0, MemorySegment.NULL);
            if (schema.equals(MemorySegment.NULL)) throw new IllegalStateException("Cannot allocate credential schema");
            var error = arena.allocate(ADDRESS);
            try {
                return operation.apply(new Context(arena, schema, error, serviceKey, arena.allocateFrom(service),
                        accountKey, arena.allocateFrom(account)));
            } finally {
                var failure = error.get(ADDRESS, 0);
                if (!failure.equals(MemorySegment.NULL)) glib.call("g_error_free", FunctionDescriptor.ofVoid(ADDRESS), failure);
                secret.call("secret_schema_unref", FunctionDescriptor.ofVoid(ADDRESS), schema);
            }
        }
    }

    private record Context(Arena arena, MemorySegment schema, MemorySegment error, MemorySegment serviceKey,
                           MemorySegment serviceValue, MemorySegment accountKey, MemorySegment accountValue) {
        void checkError() {
            if (!error.get(ADDRESS, 0).equals(MemorySegment.NULL))
                throw new IllegalStateException("Linux credential operation failed");
        }
    }
}
