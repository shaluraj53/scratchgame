package com.scratchgame.model;

import java.util.Map;

public record CellProbability(
                int row,
                int column,
                Map<String, Integer> symbols) {
}
