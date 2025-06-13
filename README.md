# Scratch Game

A command-line scratch card game in Java where players bet an amount, symbols are generated in a matrix based on configurable probabilities, and rewards are calculated based on matching combinations.

## Tech Stack

- Java 21
- Gradle 8+

## Assumptions

- Define matrix size, symbols, probabilities, and winning rules in config.json
- Bonus symbol is applied to reward only if the player wins
- One bonus symbol is randomly injected per matrix

## Build

./gradlew shadowJar

Jar location: build/libs/scratchgame-fat.jar

## Run

- Option1: java -jar build/libs/scratchgame-fat.jar --config config.json --betting-amount 100
- Option2: ./gradlew run --args="--config config.json  --betting-amount 100"

## Features

- Weighted random symbol selection
- Bonus symbol injection (1 per matrix)
- Multiple win combination handling including diagonally matching symbols
- Modular, testable code (uses record classes)
- Logging enabled for traceability

