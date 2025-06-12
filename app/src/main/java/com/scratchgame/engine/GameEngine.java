package com.scratchgame.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.scratchgame.model.Config;
import com.scratchgame.model.GameResult;
import com.scratchgame.model.SymbolInfo;
import com.scratchgame.model.WinCombination;
import com.scratchgame.utils.GameConstants;

public class GameEngine {

    private final Config config;
    private final Random random = new Random();

    public GameEngine(Config config) {
        this.config = config;
    }

    public GameResult play(int betAmount) {
        int rows = config.rows();
        int columns = config.columns();
        String[][] matrix = new String[rows][columns];

        Map<String, Integer> symbolCounts = new HashMap<>(); // non-bonus symbols
        String bonusSymbolInMatrix = "";

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                String symbol = pickStandardSymbol(r, c);
                matrix[r][c] = symbol;
                symbolCounts.put(symbol, symbolCounts.getOrDefault(symbol, 0) + 1);
            }
        }

        bonusSymbolInMatrix = injectSingleBonusSymbol(matrix, symbolCounts);
        return evaluateResult(matrix, betAmount, symbolCounts, bonusSymbolInMatrix);
    }

    // Method added for test case verification
    public GameResult evaluateMatrix(String[][] matrix, int betAmount) {
        Map<String, Integer> symbolCounts = new HashMap<>();
        String bonusSymbolInMatrix = null;
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[0].length; c++) {
                String symbol = matrix[r][c];
                SymbolInfo info = config.symbols().get(symbol);
                if (info.type().equals("bonus")) {
                    bonusSymbolInMatrix = symbol;
                } else {
                    symbolCounts.put(symbol, symbolCounts.getOrDefault(symbol, 0) + 1);
                }
            }
        }
        return evaluateResult(matrix, betAmount, symbolCounts, bonusSymbolInMatrix);
    }

    private GameResult evaluateResult(String[][] matrix, int betAmount, Map<String, Integer> symbolCounts,
            String bonusSymbolInMatrix) {
        Map<String, List<String>> appliedWinCombinations = new HashMap<>();
        double reward = 0;

        for (String symbol : symbolCounts.keySet()) {
            double baseMultiplier = config.symbols().get(symbol).rewardMultiplier();
            double totalSymbolReward = betAmount * baseMultiplier;
            double comboMultiplier = 1.0;
            boolean winCombinationFound = false;
            for (Map.Entry<String, WinCombination> entry : config.winCombinations().entrySet()) {
                String key = entry.getKey();
                WinCombination wc = entry.getValue();
                if (wc.when().equals(GameConstants.CONFIG_KEY_SAME_SYMBOLS) && wc.count() == symbolCounts.get(symbol)) {
                    appliedWinCombinations.computeIfAbsent(symbol, k -> new ArrayList<>()).add(key);
                    comboMultiplier *= wc.rewardMultiplier();
                    winCombinationFound = true;
                } else if (wc.when().equals(GameConstants.CONFIG_KEY_LINEAR_SYMBOLS)) {
                    for (List<String> area : wc.coveredAreas()) {
                        String first = null;
                        boolean match = true;
                        for (String pos : area) {
                            int[] rc = Arrays.stream(pos.split(":")).mapToInt(Integer::parseInt).toArray();
                            String current = matrix[rc[0]][rc[1]];
                            if (!config.symbols().containsKey(current)
                                    || config.symbols().get(current).type()
                                            .equals(GameConstants.CONFIG_KEY_SYMBOL_BONUS)) {
                                match = false;
                                break;
                            }
                            if (first == null)
                                first = current;
                            if (!first.equals(current)) {
                                match = false;
                                break;
                            }
                        }
                        if (match && first.equals(symbol)) {
                            winCombinationFound = true;
                            appliedWinCombinations.computeIfAbsent(symbol, k -> new ArrayList<>()).add(key);
                            comboMultiplier *= wc.rewardMultiplier();
                        }
                    }
                }
            }
            if (winCombinationFound) {
                reward += totalSymbolReward * comboMultiplier;
            }
        }

        String appliedBonusSymbol = null;
        if (reward > 0 && bonusSymbolInMatrix != null && !bonusSymbolInMatrix.isEmpty()) {
            appliedBonusSymbol = bonusSymbolInMatrix;
            SymbolInfo bonus = config.symbols().get(appliedBonusSymbol);
            switch (bonus.impact()) {
                case GameConstants.CONFIG_KEY_BONUS_MULTIPLIER -> reward *= bonus.rewardMultiplier();
                case GameConstants.CONFIG_KEY_BONUS_ADDITION -> reward += bonus.extra();
                default -> appliedBonusSymbol = null;
            }
        }

        return new GameResult(matrix, (int) reward, appliedWinCombinations, appliedBonusSymbol);
    }

    private String pickStandardSymbol(int row, int col) {
        Map<String, Integer> probabilityMap = config.getProbabilityForCell(row, col);
        int total = probabilityMap.values().stream().mapToInt(i -> i).sum();
        int rnd = random.nextInt(total);
        int sum = 0;
        for (Map.Entry<String, Integer> entry : probabilityMap.entrySet()) {
            sum += entry.getValue();
            if (rnd < sum)
                return entry.getKey();
        }
        throw new IllegalStateException("Unable to select a symbol.");
    }

    private String injectSingleBonusSymbol(String[][] matrix, Map<String, Integer> symbolCounts) {
        Map<String, Integer> bonusProbabilities = config.getBonusSymbolProbabilities();
        int total = bonusProbabilities.values().stream().mapToInt(i -> i).sum();
        if (total == 0)
            return "";

        // Pick a random bonus symbol
        int rnd = random.nextInt(total);
        int sum = 0;
        String bonusSymbol = null;
        for (Map.Entry<String, Integer> entry : bonusProbabilities.entrySet()) {
            sum += entry.getValue();
            if (rnd < sum) {
                bonusSymbol = entry.getKey();
                break;
            }
        }
        if (bonusSymbol == null)
            return "";

        // Pick a random cell to inject the bonus symbol
        int r = random.nextInt(matrix.length);
        int c = random.nextInt(matrix[0].length);
        String standardSymbolGettingReplaced = matrix[r][c];

        matrix[r][c] = bonusSymbol;
        int prevCount = symbolCounts.get(standardSymbolGettingReplaced);
        int updatedCount = --prevCount;
        symbolCounts.put(standardSymbolGettingReplaced, updatedCount); // updating sumbols count to reflect addition of
                                                                       // bonus symbol
        return bonusSymbol;
    }
}
