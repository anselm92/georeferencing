/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.anselm92.georeferencing;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 *
 * @author Anselm
 */
public class GeoReferencingApp extends Application {
    FXMLDocumentController mainController;
    
    @Override
    public void start(Stage stage) throws Exception {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/FXMLDocument.fxml"));
	Scene scene = new Scene((Parent)loader.load(), 1200, 700);
        mainController = loader.getController();
        stage.getIcons().add(
        new Image(GeoReferencingApp.class.getResourceAsStream( "view/initial_pic.png" )));
        
        stage.setTitle("Georeferencing - SAM");
        stage.setScene(scene);
        stage.show();
        FXMLDocumentController.setStage(stage);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        mainController.stop(); //To change body of generated methods, choose Tools | Templates.
        System.exit(0);
    }
    
    
}
