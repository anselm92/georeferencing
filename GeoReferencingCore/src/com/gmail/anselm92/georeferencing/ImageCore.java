/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.anselm92.georeferencing;

import com.gmail.anselm92.georeferencing.data.GeoReferencingMetaData;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;

/**
 *
 * @author Anselm
 */
public class ImageCore extends Thread {

    private final Map<String, ProcessedFile> toProcessFiles = new ConcurrentHashMap<>();
    private final ObservableList<WorkerThread> currentWorkers = FXCollections.observableArrayList();

    private final Semaphore maxFiles;
    private final Semaphore currentWorkersSema;
    private boolean running = true;

    public ImageCore(int maxFiles) {
        this.maxFiles = new Semaphore(maxFiles);
        this.currentWorkersSema = new Semaphore(1);
    }

    public ObservableList<WorkerThread> getCurrentWorkers() {
        return currentWorkers;
    }

    @Override
    public void run() {
        while (running) {
            List<Entry<String, ProcessedFile>> entriesToBeRemoved = new ArrayList<>();

            for (Entry<String, ProcessedFile> entry : toProcessFiles.entrySet()) {
                try {
                    System.out.println("new file found");
                    GeoReferencingMetaData geoMeta = entry.getValue().getMetaData();
                    if (geoMeta.fileFound()) {
                        System.out.println("starting");
                        entry.getValue().setState(ProcessingState.PROCESSING);
                        this.maxFiles.acquire();
                        final WorkerThread worker = new WorkerThread(entry);
                        //currentWorkersSema.acquire();
                        currentWorkers.add(worker);
                        //currentWorkersSema.release();
                        new Thread(worker).start();
                        entriesToBeRemoved.add(entry);
                        System.out.println("done");
                    } else {
                        entry.getValue().setState(ProcessingState.META_DATA_MISSING);
                        //entriesToBeRemoved.add(entry);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(ImageCore.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ImageCore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //synchronized(toProcessFiles){
            for (Entry<String, ProcessedFile> entry : entriesToBeRemoved) {
                toProcessFiles.remove(entry.getKey());
            }
            //}
            entriesToBeRemoved.clear();
            try {
                Thread.sleep(600);
            } catch (InterruptedException ex) {
                Logger.getLogger(ImageCore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void addFileToProcessor(String fileName, ProcessedFile pFile) {
        toProcessFiles.put(fileName, pFile);
    }

    public void stopImageCore() {
        this.running = false;
    }

    public class WorkerThread implements Runnable{

        Entry<String, ProcessedFile> task;

        public WorkerThread(Entry<String, ProcessedFile> task) {
            this.task = task;
        }

        private DoubleProperty progress = new SimpleDoubleProperty();

        public ReadOnlyDoubleProperty getWorkerProcess() {
            return this.progress;
        }

        public ProcessedFile getTask() {
            return task.getValue();
        }

        String getFileName() {
            return task.getKey();
        }

        public void update(long workDone, double max) {
            this.progress.set(workDone / max);
            if(this.progress.doubleValue()==1){
                maxFiles.release();
            }
        }
        
        public void updateState(int state ){
            task.getValue().setState(state);
        }

        @Override
        public void run() {
            System.out.println("started processing");
            task.getValue().getGeoData().setWorker(this);
            try {
                task.getValue().getGeoData().startProcessing();
            } catch (Exception ex) {
                Logger.getLogger(ImageCore.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("processing done");
            currentWorkers.remove(this);
            //currentWorkersSema.release();
            
            
        }

    }
}
