package org.provapoo3.utils;

import java.nio.file.Paths;

public class PathFXML {
    public static String pathBase(){
        String path = "src/main/java/org/provapoo3/view";
        return Paths.get(path).toAbsolutePath().toString();
    }
}
