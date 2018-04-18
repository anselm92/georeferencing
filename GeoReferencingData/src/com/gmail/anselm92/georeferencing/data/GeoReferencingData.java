/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.anselm92.georeferencing.data;

/**
 * Example file that shows how to open and read data from a .geo file.
 * @author Anselm
 */
public class GeoReferencingData {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        String filename = "C:\\Users\\Anselm\\Desktop\\munich.geo";
        GeoFile geoFile = new GeoFile(new GeoFileDescriptor(2092, 971, filename));
        geoFile.openFile();
        
        float latitude1 = geoFile.readGeoData(0, 0, 0);
        float longitude1 = geoFile.readGeoData(0, 0, 1);
        System.out.println(latitude1 + " " + longitude1);
        
        float latitude2 = geoFile.readGeoData(500, 600, 0);
        float longitude2 = geoFile.readGeoData(500, 600, 1);
        System.out.println(latitude2+ " " + longitude2);
    }

}
