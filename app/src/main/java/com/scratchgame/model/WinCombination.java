package com.scratchgame.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WinCombination(
                String when,
                int count,
                @JsonProperty("reward_multiplier") double rewardMultiplier,
                @JsonProperty("covered_areas") List<List<String>> coveredAreas) {
}