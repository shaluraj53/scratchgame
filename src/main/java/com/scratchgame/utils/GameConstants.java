package com.scratchgame.utils;

public final class GameConstants {

    private GameConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String CONFIG_ARG = "--config";
    public static final String BET_AMOUNT_ARG = "--betting-amount";
    public static final String ERROR_ARGS_MISSING = "ERROR:: You have to provide config file path and betting amount";
    public static final String SUCCESS_MSG = "Game completed. Result:";

    public static final String CONFIG_KEY_SAME_SYMBOLS = "same_symbols";
    public static final String CONFIG_KEY_LINEAR_SYMBOLS = "linear_symbols";
    public static final String CONFIG_KEY_BONUS_MULTIPLIER = "multiply_reward";
    public static final String CONFIG_KEY_BONUS_ADDITION = "extra_bonus";
    public static final String CONFIG_KEY_SYMBOL_BONUS = "bonus";
}
