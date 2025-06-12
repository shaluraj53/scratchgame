package com.scratchgame.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record Probabilities(
        @JsonProperty("standard_symbols") List<CellProbability> standardSymbols,
        @JsonProperty("bonus_symbols") BonusSymbolProbability bonusSymbols) {
}