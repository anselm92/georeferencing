/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.anselm92.georeferencing;

import com.gmail.anselm92.georeferencing.data.GeoReferencingMetaData;
import java.io.File;
import java.io.IOException;

/**
 * Testfile for reading geodata from a part of a picture
 *
 * @author Anselm
 */
public class Test {

    public static void main(String[] args) throws IOException {
        File image = new File("D:\\Seafile\\3 - Uni\\6. Semester\\Seminar Bildverarbeitung und Mustererkennung\\Georeferenzierung\\Ausarbeitung Testdaten\\Bild 2.jpg");
        GeoReferencing geo = new GeoReferencing(image, new GeoReferencingMetaData(image));

        float[][][] result = geo.getGeodata(0, 0, 1, 1, 2092, 971);
        for (int y = 0; y < result.length; y++) {
            for (int x = 0; x < result[0].length; x++) {
                System.out.print(result[y][x][0]);
                System.out.println(" , " + result[y][x][1]);
            }
        }
        float[][][] result2 = geo.getGeodata(2091, 970, 2092, 971, 2092, 971);
        for (int y = 0; y < result2.length; y++) {
            for (int x = 0; x < result2[0].length; x++) {
                System.out.print(result2[y][x][0]);
                System.out.println(" , " + result2[y][x][1]);
            }
        }
    }
}
