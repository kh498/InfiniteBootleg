package no.elg.infiniteBootleg.console;

import com.badlogic.gdx.ApplicationLogger;
import com.strongjoshua.console.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author Elg
 */
public interface ConsoleLogger extends ApplicationLogger {

    /**
     * @param level
     *     The level to log at
     * @param msg
     *     The message to log
     */
    void log(@NotNull LogLevel level, @NotNull String msg);

    /**
     * Log a level with {@link LogLevel#DEFAULT} loglevel
     *
     * @param msg
     *     The message to log
     * @param objs
     *     The object to format
     *
     * @see String#format(String, Object...)
     */
    default void logf(@NotNull String msg, @NotNull Object... objs) {
        logf(LogLevel.DEFAULT, msg, objs);
    }

    /**
     * @param level
     *     The level to log at
     * @param msg
     *     The message to log
     * @param objs
     *     The object to format
     *
     * @see String#format(String, Object...)
     */
    default void logf(LogLevel level, @NotNull String msg, @NotNull Object... objs) {
        log(level, String.format(msg, objs));
    }

    /**
     * Log a level with {@link LogLevel#DEFAULT} loglevel
     *
     * @param msg
     *     The message to log
     */
    default void log(@NotNull String msg) {
        log(LogLevel.DEFAULT, msg);
    }

    @Override
    default void log(@NotNull String tag, @NotNull String message) {
        log("[" + tag + "] " + message);
    }

    @Override
    default void log(@NotNull String tag, @NotNull String message, @Nullable Throwable exception) {
        log(tag, message);
        if (exception != null) {
            exception.printStackTrace(System.out);
        }
    }

    default void success(@NotNull String msg) {
        log(LogLevel.SUCCESS, msg);
    }

    default void success(@NotNull String msg, @NotNull Object... objs) {
        logf(LogLevel.SUCCESS, msg, objs);
    }

    default void warn(@NotNull String message) {
        error("WARN", message);
    }

    default void warn(@NotNull String message, @NotNull Object... objs) {
        error("WARN", message, objs);
    }

    default void warn(@NotNull String message, @Nullable Throwable exception) {
        error("WARN", message, exception);
    }

    @Override
    default void error(@NotNull String tag, @NotNull String message) {
        log(LogLevel.ERROR, "[" + tag + "] " + message);
    }

    default void error(@NotNull String tag, @NotNull String message, @NotNull Object... objs) {
        logf(LogLevel.ERROR, "[" + tag + "] " + message, objs);
    }

    @Override
    default void error(@NotNull String tag, @NotNull String message, @Nullable Throwable exception) {
        error(tag, message);
        if (exception != null) {
            exception.printStackTrace(System.err);
        }
    }

    @Override
    default void debug(@NotNull String tag, @NotNull String message) {
        log(LogLevel.DEFAULT, "DBG [" + tag + "] " + message);
    }

    @Override
    default void debug(@NotNull String tag, @NotNull String message, @Nullable Throwable exception) {
        debug(tag, message);
        if (exception != null) {
            exception.printStackTrace(System.out);
        }
    }
}
