/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.anselm92.georeferencing.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * This class loads a textfile that should have the same name as the image. It
 * packs the file into a property object, which can be accessed using
 * @getProperty()
 *
 * @author Anselm
 */
public class GeoReferencingMetaData {

    private File metaFile;
    private Properties metaFilePrefs;
    private boolean fileFound = false;

    public GeoReferencingMetaData(File file) {
        String path = file.getAbsolutePath();
        path = path.substring(0, path.lastIndexOf("."));
        path += ".txt";
        System.out.println(path);
        metaFile = new File(path);
        if (metaFile.exists()) {
            fileFound = true;
            metaFilePrefs = new Properties();
            try {
                metaFilePrefs.load(new FileInputStream(new File(path)));
            } catch (IOException ex) {
                Logger.getLogger(GeoReferencingMetaData.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public boolean fileFound() {
        return this.fileFound;
    }

    /**
     * Returns the value for a given key. Throws an exception if the key was not
     * found
     *
     * @param key string with the key like "longitude"
     * @return the value for this property entry
     */
    public String getProperty(String key) {
        return this.metaFilePrefs.getProperty(key);
    }

}
