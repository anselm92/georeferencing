/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.anselm92.georeferencing;

import com.gmail.anselm92.georeferencing.data.GeoReferencingMetaData;
import com.gmail.anselm92.georeferencing.ImageCore.WorkerThread;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 * @author Anselm
 */
public class ProcessedFile {

    File file;
    GeoReferencing geodata;
    GeoReferencingMetaData metaData;
    SimpleIntegerProperty state = new SimpleIntegerProperty();

    public ProcessedFile(File file) {
        state.set(ProcessingState.WAITING);
        this.file = file;
         GeoReferencing geo = null;
        try {
            metaData = new GeoReferencingMetaData(file);
            geo = new GeoReferencing(file,metaData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.geodata = geo;
    }
    
    public void setState(int state){
        this.state.set(state);
    }
    
    public ReadOnlyIntegerProperty getStateProperty(){
        return this.state;
    }
    
    public GeoReferencing getGeoData(){
        return this.geodata;
    }
    
    public GeoReferencingMetaData getMetaData(){
        return this.metaData;
    }
    
}
