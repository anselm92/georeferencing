package com.gmail.anselm92.georeferencing;

import com.gmail.anselm92.georeferencing.ImageCore.WorkerThread;
import com.gmail.anselm92.georeferencing.data.GeoFile;
import com.gmail.anselm92.georeferencing.data.GeoFileDescriptor;
import com.gmail.anselm92.georeferencing.data.GeoReferencingMetaData;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class GeoReferencing {

    private double latperMeter = 0;
    private double lonperMeter = 0;
    private File file;
    private Image img;
    private double radians;
    private double cosinus;
    private double sinus;
    private int pixelNumber = 1200;
    private int processedPixels = 0;
    private WorkerThread worker;
    GeoFile geoFile;
    private final GeoReferencingMetaData meta;

    /**
     * Ctor for GeoReferencing
     * @param file the image file.
     * @param meta metainformations about the image (using the same named .txt file)
     * @throws IOException if the image file does not exists
     */
    public GeoReferencing(File file, GeoReferencingMetaData meta) throws IOException {
        this.file = file;
        this.meta = meta;
        img = new Image(new FileInputStream(file));
    }

    public void setWorker(WorkerThread worker) {
        this.worker = worker;
    }

    /**
     * Creates a geofile for a picture with geoinformations for every pixel.
     * If you don't want a file and only want to have informations for a subset of pixels use
     * the getGeodata method
     * @throws Exception 
     */
    public void startProcessing() throws Exception {

        double width = 0;
        double height = 0;

        try (ImageInputStream in = ImageIO.createImageInputStream(file)) {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    width = reader.getWidth(0);
                    height = reader.getHeight(0);
                } finally {
                    reader.dispose();
                }
            }
        }

        String outputPath = file.getAbsolutePath();
        outputPath = outputPath.substring(0, outputPath.lastIndexOf("."));
        outputPath += ".geo";
        float initLong;
        float initLat;
        float initAlt;
        float initCameraAngle;
        float initAngle;

        try {
            initLong = Float.parseFloat(meta.getProperty("longitude"));
            initLat = Float.parseFloat(meta.getProperty("latitude"));
            initAlt = Float.parseFloat(meta.getProperty("relativeAltitude"));
            initCameraAngle = Float.parseFloat(meta.getProperty("camera"));
            initAngle = 360 - Float.parseFloat(meta.getProperty("heading"));
        } catch (Exception e) {
            worker.updateState(ProcessingState.META_DATA_WRONG);
            worker.update(pixelNumber, pixelNumber);
            return;
        }
        this.geoFile = new GeoFile(new GeoFileDescriptor((int) width, (int) height, outputPath));
        geoFile.openFile();

        pixelNumber = (int) (height * width);
        if (geoFile.exists()) {
            System.out.println("file exixsts ");
            geoFile.closeFile();
            worker.update(pixelNumber, pixelNumber);
            worker.updateState(ProcessingState.PROCESSED);
            return;
        }
        double start = System.currentTimeMillis();
        initialize(initAngle);

        float scale = (float) calculateMeterPixelRatio(initCameraAngle, (int) height, (int) width, (int) initAlt);
        float[] middle = new float[]{initLat, initLong};
        float lat = middle[0];
        float lon = middle[1];
        float latitude = (float) (Math.PI * lat / 180);
        this.latperMeter = 111132.92 - 559.82 * Math.cos(2 * latitude) + 1.175 * Math.cos(4 * latitude) - 0.0023 * Math.cos(6*latitude);
        latperMeter = 1 / latperMeter;
        this.lonperMeter = 111412.84 * Math.cos(latitude) - 93.5 * Math.cos(3 * latitude) - 0.118 * Math.cos(5*latitude);
        lonperMeter = 1 / lonperMeter;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                processedPixels += 1;

                double[] latLonNew;
                double[] latLonTemp = RotateVector2d((x - width / 2), -(y - height / 2), initAngle);
                latLonNew = referencePicture(middle[0], middle[1], latLonTemp[0] * scale, latLonTemp[1] * scale);

                geoFile.writeGeoData((float) latLonNew[0]);
                geoFile.writeGeoData((float) latLonNew[1]);
            }
            worker.update(processedPixels, pixelNumber);
        }
        worker.updateState(ProcessingState.PROCESSED);
        System.out.println("time " + (System.currentTimeMillis() - start));

        geoFile.closeFile();
        System.out.println("file closed");
    }

    /**
     * Calculates the geodata for the specified range of the specified image.
     * @param startX the startindex for the range (width ort the start column)
     * @param startY the startindex for the y position (height or the start row)
     * @param endX the last index for the x position
     * @param endY the last index for the y position
     * @param width width of the image which geodata is calculated
     * @param height height of the image which geodata is calculated
     * @return a matrix which first index is the y position, the second the x position and the last index is longitude (1) or latitude (0) of the pixel.
     */
    public float[][][] getGeodata(int startX, int startY, int endX, int endY,  int width, int height ) {
        float initLong;
        float initLat;
        float initAlt;
        float initCameraAngle;
        float initAngle;

        try {
            initLong = Float.parseFloat(meta.getProperty("longitude"));
            initLat = Float.parseFloat(meta.getProperty("latitude"));
            initAlt = Float.parseFloat(meta.getProperty("relativeAltitude"));
            initCameraAngle = Float.parseFloat(meta.getProperty("camera"));
            initAngle = 360 - Float.parseFloat(meta.getProperty("heading"));
        } catch (Exception e) {
            worker.updateState(ProcessingState.META_DATA_WRONG);
            worker.update(pixelNumber, pixelNumber);
            return null;
        }
        
                initialize(initAngle);

        float scale = (float) calculateMeterPixelRatio(initCameraAngle, (int) height, (int) width, (int) initAlt);
        float[] middle = new float[]{initLat, initLong};
        float lat = middle[0];
        float lon = middle[1];
        float latitude = (float) (Math.PI * lat / 180);
        this.latperMeter = 111132.92 - 559.82 * Math.cos(2 * latitude) + 1.175 * Math.cos(4 * latitude);
        latperMeter = 1 / latperMeter;
        this.lonperMeter = 111412.84 * Math.cos(latitude) - 93.5 * Math.cos(3 * latitude);
        lonperMeter = 1 / lonperMeter;

        float[][][] result = new float [endY-startY][endX-startX][2];
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                processedPixels += 1;

                double[] latLonNew;
                double[] latLonTemp = RotateVector2d((x - width / 2), -(y - height / 2), initAngle);
                latLonNew = referencePicture(middle[0], middle[1], latLonTemp[0] * scale, latLonTemp[1] * scale);
                result[y-startY][x-startX][0] = (float) latLonNew[0];
                result[y-startY][x-startX][1] = (float) latLonNew[1];
            }
           
        }
        return result;
    }

    private void initialize(float degrees) {
        radians = Math.toRadians(degrees);
        cosinus = Math.cos(radians);
        sinus = Math.sin(radians);
    }

    private double[] RotateVector2d(double x, double y, double degrees) {
        double[] result = new double[2];
        result[0] = (float) (cosinus * (x) - sinus * (y));
        result[1] = (float) (sinus * (x) + cosinus * (y));
        return result;
    }

    private double[] referencePicture(double lat, double lon, double x, double y) {
        double[] newVector = new double[2];
        newVector[1] = (double) (lon + x * lonperMeter);
        newVector[0] = (double) (lat + y * latperMeter);
        return newVector;
    }

    private static double calculateMeterPixelRatio(float cameraAngle,
            float xlength, int ylength, int zlength) {
        double distanceXMiddle = Math.tan(Math.toRadians(cameraAngle / 2))
                * zlength;
        return distanceXMiddle / (ylength / 2);
    }

    public Image getImage() {
        return this.img;
    }
}
