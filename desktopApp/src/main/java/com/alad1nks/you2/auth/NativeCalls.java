package com.alad1nks.you2.auth;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.*;

/** Small binding helper built exclusively on the JDK's stable Foreign Function & Memory API. */
final class NativeCalls {
    private final SymbolLookup symbols;
    private static final Linker LINKER = Linker.nativeLinker();

    NativeCalls(String library) {
        symbols = SymbolLookup.libraryLookup(library, Arena.global());
    }

    MemorySegment symbol(String name) {
        return symbols.find(name).orElseThrow(() -> new IllegalStateException("Missing native symbol: " + name));
    }

    Object call(String name, FunctionDescriptor signature, Object... arguments) {
        return invoke(LINKER.downcallHandle(symbol(name), signature), arguments);
    }

    Object variadic(String name, FunctionDescriptor signature, int firstVariadic, Object... arguments) {
        return invoke(LINKER.downcallHandle(symbol(name), signature, Linker.Option.firstVariadicArg(firstVariadic)), arguments);
    }

    WinResult windowsCall(String name, FunctionDescriptor signature, Object... arguments) {
        // Capture GetLastError atomically with the downcall, before JVM activity can overwrite it.
        try (Arena arena = Arena.ofConfined()) {
            var layout = Linker.Option.captureStateLayout();
            var state = arena.allocate(layout);
            Object[] withState = new Object[arguments.length + 1];
            withState[0] = state;
            System.arraycopy(arguments, 0, withState, 1, arguments.length);
            int value = (int) invoke(LINKER.downcallHandle(symbol(name), signature,
                    Linker.Option.captureCallState("GetLastError")), withState);
            int error = state.get(JAVA_INT, layout.byteOffset(MemoryLayout.PathElement.groupElement("GetLastError")));
            return new WinResult(value, error);
        }
    }

    private static Object invoke(MethodHandle function, Object... arguments) {
        try {
            return function.invokeWithArguments(arguments);
        } catch (RuntimeException | Error failure) {
            throw failure;
        } catch (Throwable failure) {
            // Avoid logging native arguments, which may contain credentials.
            throw new IllegalStateException("Native credential call failed");
        }
    }

    record WinResult(int value, int error) {
        boolean foundOrSucceeded() {
            if (value != 0) return true;
            if (error == 1168) return false; // ERROR_NOT_FOUND
            throw new IllegalStateException("Windows credential operation failed (" + error + ")");
        }
        void requireSuccess() {
            if (value == 0) throw new IllegalStateException("Windows credential write failed (" + error + ")");
        }
    }
}
