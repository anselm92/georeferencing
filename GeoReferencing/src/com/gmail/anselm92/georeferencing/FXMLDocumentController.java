/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.anselm92.georeferencing;

import com.gmail.anselm92.georeferencing.CoreClient.Task;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Anselm
 */
public class FXMLDocumentController implements Initializable {

    private static Stage stage;
    private Image image = new Image(getClass().getResourceAsStream("view/initial_pic.png"));
    private final Image waiting = new Image(getClass().getResourceAsStream("view/loading.png"));
    private final Image processing = new Image(getClass().getResourceAsStream("view/processing.gif"));
    private final Image metadataMissing = new Image(getClass().getResourceAsStream("view/metadataMissing.png"));
    private final Image processed = new Image(getClass().getResourceAsStream("view/processed.png"));
    
    private CoreClient imageCore;
    private List<File> openedFilesList = new ArrayList<>();
    private Map<String, Task> processedFiles = new HashMap<>();
    private Task openedFile;

    @FXML private MagnifierPane magnifierPane;
    
    @FXML private ImageView canvasMap;

    @FXML private Button addFile;
    
    @FXML private TreeView openedFiles;

    @FXML private TreeItem rootItem;

    @FXML private ListView errors;

    @FXML private AnchorPane canvasMapParent;

    @FXML private VBox progressbars;

    private Socket server;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ImageView img = new ImageView(new Image(getClass().getResourceAsStream("view/open-file-icon.png")));
        img.setFitHeight(30);
        img.setFitWidth(30);
        addFile.setGraphic(img);
        try {
            imageCore = new CoreClient();
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        magnifierPane.setScopeLinesVisible(true);
        magnifierPane.setScaleFactor(3.0);
        Tooltip tp = new Tooltip("");
        canvasMap.setImage(image);

        canvasMap.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                double x1 = event.getX() * image.getWidth()/canvasMap.getFitWidth();
                double y1 = event.getY() * image.getHeight()/canvasMap.getFitHeight();

            }
        });
        canvasMap.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            double x1 = event.getX() * image.getWidth()/canvasMap.getFitWidth();
            double y1 = event.getY() * image.getHeight()/canvasMap.getFitHeight();
            String longi;
            if (openedFile != null && openedFile.geofile!=null) {
                System.out.println(x1 + " " + y1);

                try {
                    longi = openedFile.geofile.readGeoData((int) x1, (int) y1, 0) + " , " + openedFile.geofile.readGeoData((int) x1, (int) y1, 1);
                    tp.setText(longi);
                    final Clipboard clipboard = Clipboard.getSystemClipboard();
                    final ClipboardContent content = new ClipboardContent();
                    content.putString(longi);
                    
                    clipboard.setContent(content);
                    Node node = (Node) event.getSource();
                    tp.show(node, stage.getX() + event.getSceneX(), stage.getY() + event.getSceneY());
                } catch (Exception ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        openedFiles.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<TreeItem<String>>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends TreeItem<String>> observable,
                            TreeItem<String> old_val, TreeItem<String> new_val) {
                                TreeItem<String> selectedItem = new_val;
                                if (selectedItem != null && selectedItem.getParent() != null) {
                                    try{
                                        openedFile.geofile.closeFile();
                                    } catch(NullPointerException e) {
                                        //e.printStackTrace();
                                    }
                                    Task file = processedFiles.get(selectedItem.getValue());
                                    openedFile = file;

                                    try{
                                    file.geofile.openFile();
                                    } catch(NullPointerException e) {
                                      e.printStackTrace();
                                    } catch (IOException ex) {
                                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    image = file.image;
                                    canvasMap.setImage(file.image);
                                }
                            }
                });
        canvasMap.fitWidthProperty().bind(
                canvasMapParent.widthProperty());
        canvasMap.fitHeightProperty().bind(
                canvasMapParent.heightProperty());

        ObservableMap<String, Task> workers = imageCore.getCurrentTasks();
        workers.addListener(new MapChangeListener() {
            @Override
            public void onChanged(MapChangeListener.Change change) {
                
                    if (change.wasAdded()) {
                        Task addedTask = (Task)change.getValueAdded();
                        

                            ProgressBar progress = new ProgressBar();
                            final Label label = new Label();
                            Platform.runLater(() -> {
                                progress.setMinWidth(200);
                                progress.setTranslateX(5);
                                label.setText("Progress file: " +addedTask.fileName);
                                progressbars.setSpacing(5);
                                progress.setLayoutX(20);
                                progressbars.setAlignment(Pos.TOP_LEFT);
                                progressbars.getChildren().addAll(label, progress);

                            });
                            progress.progressProperty().bind(addedTask.process);
                        
                    }
                

            }
        });
        imageCore.start();

    }

    @FXML
    private void importImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        List<File> list
                = fileChooser.showOpenMultipleDialog(stage);
        if (list != null) {
            list.stream().forEach((file) -> {
                openFile(file);
            });
        }
    }

    private void openFile(File file) {

        openedFilesList.add(file);
        Task processedFile = new Task(file.getAbsolutePath());
        try {
            processedFile.image = new Image(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        processedFiles.put(file.getName(), processedFile);
        imageCore.addTask(file.getName(), processedFile);
        
        ImageView img = new ImageView(waiting);
        img.setFitHeight(15);
        img.setFitWidth(15);
        processedFile.state.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            System.out.println("state changed");
            Platform.runLater(() -> {
                switch ((Integer) newValue) {
                    case ProcessingState.META_DATA_MISSING:
                        errors.getItems().add(file.getName() + "\n : Metainformations are missing. Please make sure that there\n is a same named textfile next to the image");
                        img.setImage(metadataMissing);
                        break;
                    case ProcessingState.META_DATA_WRONG:
                        errors.getItems().add(file.getName() + "\n : Metainformations are missing. Some important tags are\n missing. Processing aborted");
                        img.setImage(metadataMissing);
                        break;
                    case ProcessingState.PROCESSING:
                        img.setImage(processing);
                        break;
                    case ProcessingState.PROCESSED:
                        img.setImage(processed);
                        break;
                }
            });
        });
        TreeItem item = new TreeItem<>(file.getName(), img);
        rootItem.getChildren().add(item);
    }

    public static void setStage(Stage stage) {
        FXMLDocumentController.stage = stage;
    }

    void stop() {
        openedFile.geofile.closeFile();
        imageCore.stopCore();
    }

}
