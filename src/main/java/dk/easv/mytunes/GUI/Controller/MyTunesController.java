package dk.easv.mytunes.GUI.Controller;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.GUI.Model.MyTunesModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class MyTunesController implements Initializable {


    @FXML
    private TableView<MyTunes> tblSongs;

    @FXML
    private TableColumn<MyTunes, String> colTitle;

    @FXML
    private TableColumn<MyTunes, String> colArtist;

    @FXML
    private TableColumn<MyTunes, String> colCategory;

    @FXML
    private TableColumn<MyTunes, String> colTime;


    private MyTunesModel myTunesModel;

    public MyTunesController() {

        try {
            myTunesModel = new MyTunesModel();
        } catch (Exception e) {
            displayError(e);
            e.printStackTrace();
        }
    }

    private void displayError(Throwable t) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(t.getMessage());
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // setup columns in tableview
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));

        // connect tableview + listview to the ObservableList
        tblSongs.setItems(myTunesModel.getObservableSongs());
    }
}