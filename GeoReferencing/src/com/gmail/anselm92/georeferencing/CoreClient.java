/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.anselm92.georeferencing;

import com.gmail.anselm92.georeferencing.data.GeoFile;
import com.gmail.anselm92.georeferencing.data.GeoFileDescriptor;
import com.gmail.anselm92.georeferencing.data.GeoReferencingMetaData;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.image.Image;

/**
 *
 * @author Anselm
 */
public class CoreClient extends Thread{
    private boolean running = true;

   ObservableMap<String,Task> getCurrentTasks() {
        return currentTasks;
    }

    public static class Task {
        String fileName;
        GeoFile geofile;
        Image image;
        SimpleIntegerProperty state = new SimpleIntegerProperty();
        SimpleDoubleProperty process = new SimpleDoubleProperty();
       
        public Task(String fileName) {
            this.fileName = fileName;
        }
    }

    Socket socket;
    BufferedReader readerS;
    PrintWriter writerS;
    private ServerConnector connector;
    private int serverCorePID;
    private ObservableMap<String,Task> currentTasks = FXCollections.observableHashMap();
    private ObservableList<Task> newTasks = FXCollections.observableArrayList();

    /**
     * todo add all the observables and the lists that were hold by the
     * imagecore and change references from the imagecore to this class
     *
     * @throws IOException
     */
    public CoreClient() throws IOException {
        this.socket = new Socket("localhost", 9999);
        try {
             readerS = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             writerS = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
             serverCorePID = Integer.parseInt(readerS.readLine());
             this.connector = new ServerConnector();
             connector.start();
//            writerS.println("C:\\Users\\Anselm\\Downloads\\Bilder_10.05.15\\Flug_2 10.05.15\\2015-05-10_18-44-22-793+0200");
//            writerS.println("C:\\Users\\Anselm\\Downloads\\Bilder_10.05.15\\Flug_2 10.05.15\\2015-05-10_18-44-24-369+0200");
//            writerS.println("C:\\Users\\Anselm\\Downloads\\Bilder_10.05.15\\Flug_2 10.05.15\\2015-05-10_18-44-26-191+0200");
//            writerS.println("C:\\Users\\Anselm\\Downloads\\Bilder_10.05.15\\Flug_2 10.05.15\\2015-05-10_18-44-27-671+0200");
//            writerS.flush();
//            while (true) {
//                String line = readerS.readLine();
//                String[] params = line.split(":");
//                System.out.println(line);
//                if (params[0].equals("STATE")) {
//                    if (params[1].equals("1")) {
//                        socket.close();
//                        break;
//                    }
//                }
//            }

        } catch (IOException io) {

        }

    }

    public void stopCore(){
        this.running = false;
    }
    
    public void addTask(String path,Task t){
        System.out.println("addTask called");
        this.newTasks.add(t);
        this.currentTasks.put(path,t);
    }
    
    
    @Override
    public void run() {
        System.out.println("core started");
        while(running){
            List<Task> entriesToBeRemoved = new ArrayList<>();
            entriesToBeRemoved.addAll(newTasks);
            for (Task task : entriesToBeRemoved) {
                
                System.out.println("new task" +task.fileName);
                writerS.println(task.fileName);
                writerS.flush();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CoreClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            for (Task toRemove : entriesToBeRemoved) {
                newTasks.remove(toRemove);
            }
            entriesToBeRemoved.clear();
            try {
                Thread.sleep(600);
            } catch (InterruptedException ex) {
                Logger.getLogger(CoreClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private class ServerConnector extends Thread{
        
        @Override
        public void run() {
            while(running){
                String lineReceived;
                try {
                    lineReceived = readerS.readLine();
                    //System.out.println(lineReceived);
                    String[] splittedLine = lineReceived.split("\t");
                    String fileName = splittedLine[1];
                    
                    String param1=splittedLine[0];
                    String param2=splittedLine[2];
                    Task currTask = currentTasks.get(fileName.substring(fileName.lastIndexOf("\\")+1,fileName.length() ));
                    //System.out.println(fileName);
                    fileName= fileName.substring(0, fileName.lastIndexOf("."));
                    fileName+=".geo";
                    switch (param1) {
                        case "STATE":
                            int state = Integer.parseInt(param2);
                            currTask.state.set(state);
                            if(state == ProcessingState.PROCESSED){
                                currTask.geofile = new GeoFile(new GeoFileDescriptor(
                                        (int)currTask.image.getWidth() ,(int)currTask.image.getHeight() , fileName
                                ));
                                currTask.geofile.openFile();
                            }
                            break;
                        case "PROGRESS":
                            String param3 = splittedLine[3];
                            currTask.process.set(Double.parseDouble(param3));
                            break;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(CoreClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        }
        
    }
}
