package com.scratchgame;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scratchgame.engine.GameEngine;
import com.scratchgame.model.Config;
import com.scratchgame.model.GameResult;
import com.scratchgame.utils.GameConstants;

public class App {

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        String configPath = null;
        int bettingAmount = 0;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(GameConstants.CONFIG_ARG))
                configPath = args[++i];
            else if (args[i].equals(GameConstants.BET_AMOUNT_ARG))
                bettingAmount = Integer.parseInt(args[++i]);
        }

        if (configPath == null || bettingAmount <= 0) {
            logger.severe(GameConstants.ERROR_ARGS_MISSING);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        Config config;
        try {
            config = mapper.readValue(new File(configPath), Config.class);
            GameEngine engine = new GameEngine(config);
            GameResult result = engine.play(bettingAmount);
            logger.info(GameConstants.SUCCESS_MSG);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "An error occurred while running the game", exception);
        }
    }
}
