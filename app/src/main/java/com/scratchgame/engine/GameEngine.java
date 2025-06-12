package com.scratchgame.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.scratchgame.model.Config;
import com.scratchgame.model.GameResult;
import com.scratchgame.model.SymbolInfo;
import com.scratchgame.model.WinCombination;

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
        Set<String> bonusSymbolsInMatrix = new HashSet<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                String symbol = pickSymbol(r, c);
                matrix[r][c] = symbol;
                SymbolInfo info = config.symbols().get(symbol);
                if (info.type().equals("bonus")) {
                    bonusSymbolsInMatrix.add(symbol);
                } else {
                    symbolCounts.put(symbol, symbolCounts.getOrDefault(symbol, 0) + 1);
                }
            }
        }

        Map<String, List<String>> appliedWinCombinations = new HashMap<>();
        double reward = 0;

        for (String symbol : symbolCounts.keySet()) {
            double baseMultiplier = config.symbols().get(symbol).rewardMultiplier();
            double totalSymbolReward = betAmount * baseMultiplier;
            double comboMultiplier = 1.0;
            for (Map.Entry<String, WinCombination> entry : config.winCombinations().entrySet()) {
                String key = entry.getKey();
                WinCombination wc = entry.getValue();
                if (wc.when().equals("same_symbols") && wc.count() == symbolCounts.get(symbol)) {
                    appliedWinCombinations.computeIfAbsent(symbol, k -> new ArrayList<>()).add(key);
                    comboMultiplier *= wc.rewardMultiplier();
                } else if (wc.when().equals("linear_symbols")) {
                    for (List<String> area : wc.coveredAreas()) {
                        String first = null;
                        boolean match = true;
                        for (String pos : area) {
                            int[] rc = Arrays.stream(pos.split(":")).mapToInt(Integer::parseInt).toArray();
                            String current = matrix[rc[0]][rc[1]];
                            if (!config.symbols().containsKey(current)
                                    || config.symbols().get(current).type().equals("bonus")) {
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
                            appliedWinCombinations.computeIfAbsent(symbol, k -> new ArrayList<>()).add(key);
                            comboMultiplier *= wc.rewardMultiplier();
                        }
                    }
                }
            }
            reward += totalSymbolReward * comboMultiplier;
        }

        String appliedBonusSymbol = null;
        if (reward > 0 && !bonusSymbolsInMatrix.isEmpty()) {
            List<String> bonusList = new ArrayList<>(bonusSymbolsInMatrix);
            Collections.shuffle(bonusList);
            appliedBonusSymbol = bonusList.get(0);
            SymbolInfo bonus = config.symbols().get(appliedBonusSymbol);
            switch (bonus.impact()) {
                case "multiply_reward" -> reward *= bonus.rewardMultiplier();
                case "extra_bonus" -> reward += bonus.extra();
            }
        }

        return new GameResult(matrix, (int) reward, appliedWinCombinations, appliedBonusSymbol);
    }

    private String pickSymbol(int row, int col) {
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
}
