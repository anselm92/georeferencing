/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.anselm92.georeferencing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 *
 * @author Anselm
 */
public class CoreServer {
    static int pid;
    public static void main(String[] args) throws IOException {
        pid = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        System.out.println(pid);
        final ServerSocket socket = new ServerSocket(9999, 10);
        while (true) {
            new ClientThread(socket.accept()).start();
        }
    }

    private static class ClientThread extends Thread {

        private Socket client;
        private BufferedReader reader;
        private PrintWriter writer;
        private ImageCore core = new ImageCore(2);

        public ClientThread(Socket client) {
            
            
            System.out.println("client connected: " + client.getInetAddress());
            try {
                 reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));
            } catch (IOException ex) {
                Logger.getLogger(CoreServer.class.getName()).log(Level.SEVERE, null, ex);
            }

            ObservableList<ImageCore.WorkerThread> workers = core.getCurrentWorkers();
            workers.addListener(new ListChangeListener() {
                @Override
                public void onChanged(ListChangeListener.Change change) {
                    while (change.next()) {
                        if (change.wasAdded()) {
                            List<ImageCore.WorkerThread> workersAdded = change.getAddedSubList();
                            workersAdded.stream().forEach((worker) -> {
                                worker.getWorkerProcess().addListener(new ChangeListener<Number>() {
                                    @Override
                                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
         
                                            writer.println("PROGRESS\t" + worker.getFileName() + "\t" + oldValue + "\t" + newValue);
                                            writer.flush();

                                    }
                                });
                            });
                        }
                    }
                }
            });

        }

        @Override
        public void run() {
            boolean running = true;
            core.start();
            writer.println(pid);
            writer.flush();
            while (running) {
                try {
                    String fileName = reader.readLine();
                    if(fileName==null){
                        core.stopImageCore();
                        break;
                    }
                    System.out.println("new task "+fileName);
                    ProcessedFile processedFile = new ProcessedFile(new File(fileName));
                    core.addFileToProcessor(fileName, processedFile);
                    processedFile.getStateProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                            writer.println("STATE\t" + fileName + "\t"+ newValue);
                            writer.flush();
                    });
                } catch (IOException ex) {
                    running = false;
                    core.stopImageCore();
                    core = null;
                    System.gc();
                }
            }

        }

    }
}
