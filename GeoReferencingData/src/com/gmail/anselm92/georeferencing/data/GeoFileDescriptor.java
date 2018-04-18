/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.anselm92.georeferencing.data;

/**
 * Descriptor for a GeoFile.
 * Holds every informations that are needed to open the geofile.
 * @author Anselm
 */
public class GeoFileDescriptor {
    
    private int imageWidth;
    private int imageHeight;
    private String fileName;

    public int getImageHeight() {
        return imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Creates a new GeoFileDescriptor.
     * @param imageWidth the imageWidth
     * @param imageHeight the imageHeight
     * @param fileName the abosult path for the geofile
     */
    public GeoFileDescriptor(int imageWidth, int imageHeight,String fileName) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.fileName = fileName;
    }
    
    
    
}
