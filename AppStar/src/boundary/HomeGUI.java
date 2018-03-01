package boundary;

import control.HomeController;
import control.LoginController;
import entity.UtenteRegistrato;
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
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeGUI implements Initializable {

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private RadioButton importaFileSatellite;
    @FXML
    private RadioButton registraUtente;
    @FXML
    private RadioButton nuoviDatiSatellite;
    @FXML
    private RadioButton nuoviDatiStrumenti;
    @FXML
    private RadioButton infoFilamento;
    @FXML
    private RadioButton ricercaFilamento;
    @FXML
    private RadioButton ricercaStelle;
    @FXML
    private RadioButton calcolaDistanze;
    @FXML
    private Button logoutButton;
    @FXML
    private Button avantiButton;

    public void istanziaHomeGUIFXML(Event e){
        //Lancia l'interfaccia grafica HomeGUI.fxml.

        try{
            Parent root = FXMLLoader.load(getClass().getResource("/boundary/HomeGUI.fxml"));
            ((Node) (e.getSource())).getScene().setRoot(root);
            //Imposta il root relativo alla schermata di Home.
        }catch (Exception er){
            System.err.println(er.getMessage());
        }
    }

    public void initialize(URL location, ResourceBundle resources){

        infoFilamento.setSelected(true);
        //RadioButton selezionato di default.
        HomeController homeController = new HomeController();

        boolean isAmministratore = homeController.verificaAmministratore();
        //Controlla se l'attuale utente connesso (istanza del Singleton) sia un amministratore o meno...
        if (!isAmministratore){
            //...se non lo e' disabilita i RadioButton relativi ai comandi dell'amministratore.
            importaFileSatellite.setDisable(true);
            registraUtente.setDisable(true);
            nuoviDatiSatellite.setDisable(true);
            nuoviDatiStrumenti.setDisable(true);
        }

        logoutButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                homeController.disconnettiUtente(); //Vedi control.HomeController.disconnettiUtente().
                LoginController loginController = new LoginController();
                loginController.istanziaLoginGUI(event);
            }
        });
    }
}

