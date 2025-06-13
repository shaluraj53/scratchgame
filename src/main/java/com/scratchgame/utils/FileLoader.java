package com.scratchgame.utils;

import java.io.File;
import java.net.URL;

public interface FileLoader {

    public static File loadFileFromResources(String fileName) {
        ClassLoader classLoader = FileLoader.class.getClassLoader();
        File file = null;
        URL resource = classLoader.getResource(fileName);
        if (resource != null) {
            file = new File(resource.getFile());
        }
        return file;
    }
}
