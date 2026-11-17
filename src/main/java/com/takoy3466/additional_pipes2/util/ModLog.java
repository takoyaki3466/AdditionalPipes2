package com.takoy3466.additional_pipes2.util;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import com.takoy3466.additional_pipes2.config.APConfig;

/**
 * 旧 {@code mod_AdditionalPipes.log(msg, level)} に相当する、設定の logLevel で出力を絞るログ。
 * 旧版と同じく既定 (logLevel = 0) では何も出力しない。想定外の状態の記録には使わず、
 * それらは常に {@link AdditionalPipes2#LOGGER} へ直接出す。
 */
public final class ModLog {
    private static final int LEVEL_ERROR = 1;
    private static final int LEVEL_WARNING = 2;
    private static final int LEVEL_INFO = 3;

    private ModLog() {
    }

    public static void error(String message, Object... args) {
        if (APConfig.logLevel() >= LEVEL_ERROR) {
            AdditionalPipes2.LOGGER.error(message, args);
        }
    }

    public static void warn(String message, Object... args) {
        if (APConfig.logLevel() >= LEVEL_WARNING) {
            AdditionalPipes2.LOGGER.warn(message, args);
        }
    }

    public static void info(String message, Object... args) {
        if (APConfig.logLevel() >= LEVEL_INFO) {
            AdditionalPipes2.LOGGER.info(message, args);
        }
    }
}
