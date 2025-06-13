package com.scratchgame.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Config(
        int rows,
        int columns,
        Map<String, SymbolInfo> symbols,
        @JsonProperty("win_combinations") Map<String, WinCombination> winCombinations,
        @JsonProperty("probabilities") Probabilities probabilities,
        Map<String, Map<String, Map<String, Integer>>> cellOverrides) {

    public Map<String, Integer> getProbabilityForCell(int row, int col) {
        for (CellProbability p : probabilities.standardSymbols()) {
            if (p.row() == row && p.column() == col) {
                return p.symbols();
            }
        }
        // Fallback to the first defined cell if specific one not found
        return probabilities.standardSymbols().isEmpty() ? Map.of() : probabilities.standardSymbols().get(0).symbols();
    }

    public Map<String, Integer> getBonusSymbolProbabilities() {
        return probabilities.bonusSymbols().symbols();
    }
}