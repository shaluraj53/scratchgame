package com.scratchgame.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SymbolInfo(
        String type,
        @JsonProperty("reward_multiplier") double rewardMultiplier,
        String impact,
        int extra) {
}
