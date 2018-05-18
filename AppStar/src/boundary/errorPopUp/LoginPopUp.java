package boundary.errorPopUp;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginPopUp implements Initializable {
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private Button okButton;

    public void istanziaLoginPopUpFXML(){
        //Lancia l'interfaccia grafica LoginPopUp.fxml.

        try{
            Parent root = FXMLLoader.load(getClass().getResource("/boundary/errorPopUp/LoginPopUp.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Error");
            stage.setScene(new Scene(root, 450, 150));
            stage.show();
        }catch (Exception er){
            System.err.println(er.getMessage());
        }
    }

    public void initialize(URL location, ResourceBundle resources){

        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ((Node)(event.getSource())).getScene().getWindow().hide();
            }
        });

    }
}