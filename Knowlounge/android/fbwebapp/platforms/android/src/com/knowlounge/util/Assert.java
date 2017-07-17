package com.knowlounge.util;

import android.os.Looper;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by Minsu on 2016-05-27.
 */
public final class Assert {
    public static @interface RunsOnMainThread {}
    public static @interface DoesNotRunOnMainThread {}
    public static @interface RunsOnAnyThread {}

    private static final String TEST_THREAD_SUBSTRING = "test";

    private static boolean sIsEngBuild;
    private static boolean sShouldCrash;

    // Private constructor so no one creates this class.
    private Assert() {
    }

    // The proguard rules will strip this method out on user/userdebug builds.
    // If you change the method signature you MUST edit proguard-release.flags.
    private static void setIfEngBuild() {
        sShouldCrash = sIsEngBuild = true;
    }

    // Static initializer block to find out if we're running an eng or
    // release build.
    static {
        setIfEngBuild();
    }

    /**
     * Halt execution if this is not an eng build.
     * <p>Intended for use in code paths that should be run only for tests and never on
     * a real build.
     * <p>Note that this will crash on a user build even though asserts don't normally
     * crash on a user build.
     */
    public static void isEngBuild() {
        isTrueReleaseCheck(sIsEngBuild);
    }

    /**
     * Halt execution if this isn't the case.
     */
    public static void isTrue(final boolean condition) {
        if (!condition) {
            fail("Expected condition to be true", false);
        }
    }

    /**
     * Halt execution if this isn't the case.
     */
    public static void isFalse(final boolean condition) {
        if (condition) {
            fail("Expected condition to be false", false);
        }
    }

    /**
     * Halt execution even in release builds if this isn't the case.
     */
    public static void isTrueReleaseCheck(final boolean condition) {
        if (!condition) {
            fail("Expected condition to be true", true);
        }
    }

    public static void equals(final int expected, final int actual) {
        if (expected != actual) {
            fail("Expected " + expected + " but got " + actual, false);
        }
    }

    public static void equals(final long expected, final long actual) {
        if (expected != actual) {
            fail("Expected " + expected + " but got " + actual, false);
        }
    }

    public static void equals(final Object expected, final Object actual) {
        if (expected != actual
                && (expected == null || actual == null || !expected.equals(actual))) {
            fail("Expected " + expected + " but got " + actual, false);
        }
    }

    public static void oneOf(final int actual, final int ...expected) {
        for (int value : expected) {
            if (actual == value) {
                return;
            }
        }
        fail("Expected value to be one of " + Arrays.toString(expected) + " but was " + actual);
    }

    public static void inRange(
            final int val, final int rangeMinInclusive, final int rangeMaxInclusive) {
        if (val < rangeMinInclusive || val > rangeMaxInclusive) {
            fail("Expected value in range [" + rangeMinInclusive + ", " +
                    rangeMaxInclusive + "], but was " + val, false);
        }
    }

    public static void inRange(
            final long val, final long rangeMinInclusive, final long rangeMaxInclusive) {
        if (val < rangeMinInclusive || val > rangeMaxInclusive) {
            fail("Expected value in range [" + rangeMinInclusive + ", " +
                    rangeMaxInclusive + "], but was " + val, false);
        }
    }

    public static void isMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()
                && !Thread.currentThread().getName().contains(TEST_THREAD_SUBSTRING)) {
            fail("Expected to run on main thread", false);
        }
    }

    public static void isNotMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()
                && !Thread.currentThread().getName().contains(TEST_THREAD_SUBSTRING)) {
            fail("Not expected to run on main thread", false);
        }
    }

    /**
     * Halt execution if the value passed in is not null
     * @param obj The object to check
     */
    public static void isNull(final Object obj) {
        if (obj != null) {
            fail("Expected object to be null", false);
        }
    }

    /**
     * Halt execution if the value passed in is not null
     * @param obj The object to check
     * @param failureMessage message to print when halting execution
     */
    public static void isNull(final Object obj, final String failureMessage) {
        if (obj != null) {
            fail(failureMessage, false);
        }
    }

    /**
     * Halt execution if the value passed in is null
     * @param obj The object to check
     */
    public static void notNull(final Object obj) {
        if (obj == null) {
            fail("Expected value to be non-null", false);
        }
    }

    public static void fail(final String message) {
        fail("Assert.fail() called: " + message, false);
    }

    private static void fail(final String message, final boolean crashRelease) {
        Log.e("Assert", message);
        if (crashRelease || sShouldCrash) {
            throw new AssertionError(message);
        } else {
            // Find the method whose assertion failed. We're using a depth of 2, because all public
            // Assert methods delegate to this one (see javadoc on getCaller() for details).
            StackTraceElement caller = getCaller(2);
            if (caller != null) {
                // This log message can be de-obfuscated by the Proguard retrace tool, just like a
                // full stack trace from a crash.
                Log.e("Assert", "\tat " + caller.toString());
            }
        }
    }

    /**
     * Returns info about the calling method. The {@code depth} parameter controls how far back to
     * go. For example, if foo() calls bar(), and bar() calls getCaller(0), it returns info about
     * bar(). If bar() instead called getCaller(1), it would return info about foo(). And so on.
     * <p>
     * NOTE: This method retrieves the current thread's stack trace, which adds runtime overhead.
     * It should only be used in production where necessary to gather context about an error or
     * unexpected event (e.g. the {@link Assert} class uses it).
     *
     * @return stack frame information for the caller (if found); otherwise {@code null}.
     */
    public static StackTraceElement getCaller(int depth) {
        // If the signature of this method is changed, proguard.flags must be updated!
        if (depth < 0) {
            throw new IllegalArgumentException("depth cannot be negative");
        }
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (trace == null || trace.length < (depth + 2)) {
            return null;
        }
        // The stack trace includes some methods we don't care about (e.g. this method).
        // Walk down until we find this method, and then back up to the caller we're looking for.
        for (int i = 0; i < trace.length - 1; i++) {
            String methodName = trace[i].getMethodName();
            if ("getCaller".equals(methodName)) {
                return trace[i + depth + 1];
            }
        }
        // Never found ourself in the stack?!
        return null;
    }

}
