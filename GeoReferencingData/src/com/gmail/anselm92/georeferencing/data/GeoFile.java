/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.anselm92.georeferencing.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a geofile. It loads either an existing geofile or it
 * creates a new geofile if the given filepath does not exists
 *
 * @author Anselm
 */
public class GeoFile {

    /**
     * ByteBuffer holding the loaded geofile.
     */
    ByteBuffer buffer;
    /**
     * The Descriptor for this file, holding the image specific informations,
     * like width and height.
     */
    GeoFileDescriptor descriptor;
    /**
     * The file loaded from the given path.
     */
    File geoFile;

    /**
     * True if the geo file already exists.
     */
    boolean exists = false;
    RandomAccessFile raf;

    /**
     * Creates a new GeoFile.
     *
     * @param descriptor the descriptor that contains informations about the
     * image width, height and path.
     */
    public GeoFile(GeoFileDescriptor descriptor) {

        geoFile = new File(descriptor.getFileName());
        this.descriptor = descriptor;

    }

    /**
     * Writes a floating point into the bytebuffer.
     *
     * @param dataToWrite the float number to be written.
     * @throws Exception is thrown if the file was not opened before writing
     * data.
     */
    public void writeGeoData(float dataToWrite) throws Exception {
        try {
            buffer.putFloat(dataToWrite);
        } catch (NullPointerException ni) {
            throw new Exception("File not opened for reading, call openFile()", ni);
        }
    }

    /**
     * Reads a float number from the given position and returns it. 8 + ((xPos)
     * + descriptor.getImageWidth() * yPos)*8 + offset*4 For example: image
     * (1200x1000) to read the geodata from the last pixel of the image a call
     * would look like this: readGeoData(1199,999,0)
     *
     * @param xPos the x position of the image
     * @param yPos the y position of the image
     * @param offset should be zero or one. if zero the latitude is returned, if
     * 1 the longitude is returned
     * @returns longitude or latitude of a given pixel
     * @throws Exception if the file was not opened for reading before.
     */
    public float readGeoData(int xPos, int yPos, int offset) throws Exception {
        try {
            System.out.println((8 + ((xPos) + descriptor.getImageWidth() * yPos) * 8 + offset * 4) + "");
            return buffer.getFloat(8 + ((xPos) + descriptor.getImageWidth() * yPos) * 8 + offset * 4);
        } catch (NullPointerException ni) {
            throw new Exception("File not opened for reading, call openFile()", ni);
        }
    }

    /**
     * Closes the bytebuffer and writes the data to disk. Call this method to
     * save up memory.
     */
    public void closeFile() {
        if (!exists) {
            try (FileOutputStream out = new FileOutputStream(geoFile)) {

                FileChannel fc = out.getChannel();
                buffer.flip();
                
                while(fc.write(buffer)!=-1);
                fc.close();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(GeoFile.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GeoFile.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        try {
            raf.close();
        } catch (IOException ex) {
            Logger.getLogger(GeoFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        buffer = null;
    }

    /**
     * True if the file already exists otherwise a new file will be created,
     * when calling openfile().
     *
     * @return if the file already exists.
     */
    public boolean exists() {
        return exists;
    }

    /**
     * Prepares this geofile for reading and writing.
     */
    public void openFile() throws IOException {
        //buffer = ByteBuffer.allocate(descriptor.getImageHeight() * descriptor.getImageWidth() * 8 + 8);
        buffer = ByteBuffer.allocate(descriptor.getImageHeight() * descriptor.getImageWidth() * 8 + 8);
        if (geoFile.exists()) {
            exists = true;

           // try (FileInputStream out = new FileInputStream(geoFile)) {
            try {
                raf = new RandomAccessFile(geoFile, "rw");
                FileChannel channel = raf.getChannel();
                System.out.println("size "+channel.size());
                while(channel.read(buffer)!=-1);
                // ByteBuffer buf = ByteBuffer.allocate((int)file.length());
//                FileInputStream in = new FileInputStream(geoFile);
//                byte bytes[] = new byte[1024];
//                int read;
//
//                while ((read = in.read(bytes)) != -1) {
//                    buffer.put(bytes, 0, read);
//                }
//                System.out.println("done ");
                //raf = new RandomAccessFile(geoFile, "rw");
                //FileChannel channel = raf.getChannel();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GeoFile.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GeoFile.class.getName()).log(Level.SEVERE, null, ex);
            }

                // extract a file channel
                // you can memory-map a byte-buffer, but it keeps the file locked
            buffer.flip();
//            } catch (FileNotFoundException ex) {
//                Logger.getLogger(GeoFile.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (IOException ex) {
//                Logger.getLogger(GeoFile.class.getName()).log(Level.SEVERE, null, ex);
//            }
        } else {
            try {
                raf = new RandomAccessFile(geoFile, "rw");
                FileChannel channel = raf.getChannel();
                // ByteBuffer buf = ByteBuffer.allocate((int)file.length());
                int read = channel.read(buffer);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GeoFile.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GeoFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
