package boundary;

import control.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
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
    private RadioButton ricercaFilamentoLum;
    @FXML
    private RadioButton ricercaFilamentoNumSeg;
    @FXML
    private RadioButton ricercaStelle;
    @FXML
    private RadioButton calcolaDistanze;
    @FXML
    private Button logoutButton;
    @FXML
    private Button avantiButton;
    @FXML
    private Label nomeCognomeLabel;
    @FXML
    private Label userIDLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label adminLabel;

    public void istanziaHomeGUIFXML(Event e){
        //Lancia l'interfaccia grafica HomeGUI.fxml.

        try{
            Parent root = FXMLLoader.load(getClass().getResource("/boundary/HomeGUI.fxml"));
            root.prefHeight(450);
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

        nomeCognomeLabel.setText(homeController.getUtente().getNome() + " " + homeController.getUtente().getCognome());
        userIDLabel.setText(homeController.getUtente().getUserID());
        emailLabel.setText(homeController.getUtente().getEmail());

        boolean isAmministratore = homeController.verificaAmministratore();
        //Controlla se l'attuale utente connesso (istanza del Singleton) sia un amministratore o meno...
        if (!isAmministratore){
            //...se non lo e' disabilita i RadioButton relativi ai comandi dell'amministratore.
            importaFileSatellite.setDisable(true);
            registraUtente.setDisable(true);
            nuoviDatiSatellite.setDisable(true);
            nuoviDatiStrumenti.setDisable(true);
            adminLabel.setText("U");
        }

        avantiButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (importaFileSatellite.isSelected()){
                    homeController.troncaDati();
                    ImportaFileSatelliteController importaFileSatelliteController = new ImportaFileSatelliteController();
                    importaFileSatelliteController.istanziaImportaFileSatelliteGUI(event);
                }else if(registraUtente.isSelected()){
                    RegistraUtenteController registraUtenteController = new RegistraUtenteController();
                    registraUtenteController.istanziaRegistraUtenteGUI(event);
                }else if(nuoviDatiSatellite.isSelected()){
                    InserisciFileDatiSatelliteController inserisciFileDatiSatelliteController =
                            new InserisciFileDatiSatelliteController();
                    inserisciFileDatiSatelliteController.istanziaInserisciFileDatiSatelliteGUI(event);
                }else if(nuoviDatiStrumenti.isSelected()){
                    InserisciDatiStrumentiController inserisciDatiStrumentiController =
                            new InserisciDatiStrumentiController();
                    inserisciDatiStrumentiController.istanziaInserisciFileDatiSatelliteGUI(event);
                }else if(infoFilamento.isSelected()){
                    InfoFilamentiController infoFilamentiController =
                            new InfoFilamentiController();
                    infoFilamentiController.istanziaInfoFilamentiGUI(event);
                }else if(ricercaFilamentoLum.isSelected()){
                    RicercaFilamentoLumController ricercaFilamentoLumController =
                            new RicercaFilamentoLumController();
                    ((Node) (event.getSource())).getScene().getRoot().prefHeight(500);
                    ricercaFilamentoLumController.istanziaRicercaFilamentoLumGUI(event);
                }else if (ricercaFilamentoNumSeg.isSelected()){

                }else if(ricercaStelle.isSelected()){

                }else if(calcolaDistanze.isSelected()){

                }
            }
        });

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

