package persistence;

import entity.Filamento;
import entity.Stella;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static persistence.Connessione.CONN;

public class FileDao {

    //REQ 5 - Calcola il centroide di un filamento
    public static ArrayList calcolaCentroide(String nomeFil, int idFil, String satellite) throws SQLException{
        //Calcola la posizione del centroide del filamento specificato.

        ArrayList<String> centroide = new ArrayList<>(2);

        Connessione.connettiti();
        String query1 = "SELECT AVG(p.\"GLON_CONT\") AS lon_centroide, AVG(p.\"GLAT_CONT\") AS lat_centroide\n" +
                "FROM punti_contorni p JOIN contorni c ON p.\"N\" = c.\"NPCONT\" AND p.\"SATELLITE\" = c.\"SATELLITE\"\n" +
                "WHERE c.\"NAME_FIL\" = ?";

        String query2 = "SELECT AVG(p.\"GLON_CONT\") AS lon_centroide, AVG(p.\"GLAT_CONT\") AS lat_centroide\n" +
                "FROM punti_contorni p JOIN contorni c ON p.\"N\" = c.\"NPCONT\" " +
                "JOIN filamenti f ON f.\"NAME\" = c.\"NAME_FIL\" AND f.\"SATELLITE\" = p.\"SATELLITE\"" +
                "WHERE f.\"IDFIL\" = ? AND p.\"SATELLITE\" = ?";

        try{
            PreparedStatement ps1;
            if (!nomeFil.equals("")){
                ps1 = CONN.prepareStatement(query1);
                ps1.setString(1, nomeFil);

            }else{
                ps1 = CONN.prepareStatement(query2);
                ps1.setInt(1, idFil);
                ps1.setString(2, satellite);

            }
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            BigDecimal lon_centroide = rs1.getBigDecimal(1);
            BigDecimal lat_centroide = rs1.getBigDecimal(2);
            centroide.add(0, lon_centroide.toString());
            centroide.add(1, lat_centroide.toString());
            System.out.println("Centroide calcolato correttamente");
            return centroide;
        }catch (SQLException | NullPointerException | java.lang.IndexOutOfBoundsException e){
            ArrayList<String> errore = new ArrayList<>(2);
            errore.add(0, "NON TROVATO");
            errore.add(1, "NON TROVATO");
            System.out.println("Centroide non trovato");
            return errore;
        } finally{
            CONN.close();
        }
    }
    // REQ 5 - calcola l'estensione del contorno di un filamento
    public static ArrayList calcolaEstensione(String nomeFil, int idFil, String satellite) throws SQLException{
        //Calcola l'estensione del filamento specificato.
        ArrayList<String> estensione = new ArrayList<>(2);

        Connessione.connettiti();
        String query1 = "SELECT max(\"GLON_CONT\")-min(\"GLON_CONT\") AS est_lon, max(\"GLAT_CONT\")-min(\"GLAT_CONT\") AS est_lat\n" +
                "FROM filamenti f JOIN contorni c ON f.\"NAME\" = c.\"NAME_FIL\" JOIN punti_contorni p ON c.\"NPCONT\" = p.\"N\" \n" +
                "WHERE \"NAME\" = ? OR (f.\"IDFIL\" = ? AND f.\"SATELLITE\" = ?)";

        try{
            PreparedStatement ps1;
            ps1 = CONN.prepareStatement(query1);
            ps1.setString(1, nomeFil);
            if (idFil>=0) ps1.setInt(2, idFil); else ps1.setInt(2, 0);
            ps1.setString(3, satellite);

            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            BigDecimal lon_centroide = rs1.getBigDecimal(1);
            BigDecimal lat_centroide = rs1.getBigDecimal(2);
            estensione.add(0, lon_centroide.toString());
            estensione.add(1, lat_centroide.toString());
            System.out.println("Estensione calcolata correttamente");
            return estensione;
        }catch (SQLException | NullPointerException | java.lang.IndexOutOfBoundsException e){
            ArrayList<String> errore = new ArrayList<>(2);
            errore.add(0, "NON TROVATO");
            errore.add(1, "NON TROVATO");
            System.out.println("Estensione non trovata");
            return errore;
        } finally{
            CONN.close();
        }
    }

    // REQ 5 - Calcola il numero di segmenti di un filamento
    public static int calcolaNumSeg(String nomeFil, int idFil, String satellite) throws SQLException{
        //Calcola l'estensione del filamento specificato.
        int num_seg;

        Connessione.connettiti();
        String query1 = "SELECT \"NUM_SEG\"\n" +
                "FROM filamenti\n" +
                "WHERE (\"IDFIL\" = ? AND \"SATELLITE\" = ?) OR \"NAME\" = ?";

        try{
            PreparedStatement ps1;
            ps1 = CONN.prepareStatement(query1);
            if (idFil>=0) ps1.setInt(1, idFil); else ps1.setInt(1, 0);
            ps1.setString(2, satellite);
            ps1.setString(3, nomeFil);

            ResultSet rs1 = ps1.executeQuery();
            rs1.next();

            num_seg = rs1.getInt(1);

            System.out.println("Numero di segmenti calcolato correttamente");
            return num_seg;
        }catch (SQLException | NullPointerException | java.lang.IndexOutOfBoundsException  e){
            System.out.println(e.getMessage());
            System.out.println("Numero di segmenti non trovato");
            return -1;
        } finally{
            CONN.close();
        }
    }

    // REQ 6 - Ricerca i filamenti in base al contrasto E alla ellitticita'
    public static ArrayList<Integer> cercaFilamenti(ObservableList<Filamento> filamento, TableView tableView, TableColumn id, TableColumn nome,
                                      TableColumn numSeg, TableColumn satellite, TableColumn con, TableColumn ell,
                                      float lum, float ellipt1, float ellipt2, int pagina)
            throws SQLException, NumberFormatException{

        Connessione.connettiti();

        int numRic;
        int tot;
        ArrayList<Integer> result = new ArrayList<>(2);

        double contrasto = 1.0 + (lum/100.0);

        int offset = (pagina - 1) * 20;

        String query =  "SELECT f.\"IDFIL\", f.\"NAME\", f.\"NUM_SEG\", f.\"SATELLITE\", m.\"CONTRAST\", m.\"ELLIPTICITY\" " +
                        "FROM filamenti f JOIN misurazione m ON f.\"NAME\" = m.\"FILAMENTO\" " +
                        "WHERE m.\"CONTRAST\" > '" + contrasto + "' AND m.\"ELLIPTICITY\" > '" + ellipt1 +
                                        "' AND m.\"ELLIPTICITY\" < '" + ellipt2 + "' " +
                        "ORDER BY \"SATELLITE\" ASC, \"IDFIL\" ASC LIMIT 20 OFFSET ?";

        String queryCount =     "SELECT COUNT(*) AS \"CONTEGGIO\" " +
                                "FROM filamenti f JOIN misurazione m ON f.\"NAME\" = m.\"FILAMENTO\" "+
                                "WHERE m.\"CONTRAST\" > '" + contrasto + "' AND m.\"ELLIPTICITY\" > '" + ellipt1 +
                                "' AND m.\"ELLIPTICITY\" < '" + ellipt2 + "' ";

        String countAll =   "SELECT COUNT(*) FROM filamenti";

        try{
            PreparedStatement ps1 = CONN.prepareStatement(query);
            ps1.setInt(1, offset);
            filamento = FXCollections.observableArrayList();
            ResultSet rs = ps1.executeQuery();
            while (rs.next()){
                Filamento filamenti = new Filamento(rs.getString("IDFIL"), rs.getString("NAME"),
                        rs.getInt("NUM_SEG"), rs.getString("SATELLITE"),
                        (rs.getBigDecimal("CONTRAST")), (rs.getBigDecimal("ELLIPTICITY")));
                filamento.add(filamenti);
            }

            Statement st2 = CONN.createStatement();
            ResultSet rs2 = st2.executeQuery(queryCount);
            rs2.next();
            numRic = rs2.getInt(1);
            System.out.println("NUMERO RICORRENZE TROVATE: " + numRic);
            Statement st3 = CONN.createStatement();
            ResultSet rs3 = st3.executeQuery(countAll);
            rs3.next();
            tot = rs3.getInt(1);
            result.add(0, numRic); result.add(1, tot);
        }catch (SQLException e){
            System.out.println(e.getMessage());
            result.add(0, 0); result.add(1, 0);
        } finally{
            CONN.close();
        }
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        nome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        numSeg.setCellValueFactory(new PropertyValueFactory<>("numSeg"));
        satellite.setCellValueFactory(new PropertyValueFactory<>("satellite"));
        con.setCellValueFactory(new PropertyValueFactory<>("con"));
        ell.setCellValueFactory(new PropertyValueFactory<>("ell"));
        tableView.setItems(null);
        tableView.setItems(filamento);

        return result;
    }

    // REQ 7 - Ricerca i filamenti con un numero di segmenti compreso in un range
    public static int cercaFilamentiSeg(ObservableList<Filamento> filamento, TableView tableView, TableColumn idColumn,
                                        TableColumn nomeColumn, TableColumn satColumn, TableColumn numSegColumn,
                                        int seg1, int seg2, int pagina) throws SQLException{

        Connessione.connettiti();

        int numRic;
        int offset = (pagina-1) * 20;

        String query = "SELECT * " +
                        "FROM filamenti " +
                        "WHERE \"NUM_SEG\" >= ? AND \"NUM_SEG\" <= ? " +
                        "LIMIT 20 OFFSET ?";

        String queryCount = "SELECT COUNT(*) " +
                            "FROM filamenti " +
                            "WHERE \"NUM_SEG\" >= ? AND \"NUM_SEG\" <= ?";

        try{
            PreparedStatement ps1 = CONN.prepareStatement(query);
            ps1.setInt(1, seg1);
            ps1.setInt(2, seg2);
            ps1.setInt(3, offset);
            filamento = FXCollections.observableArrayList();
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()){
                Filamento filamenti = new Filamento(rs1.getString("IDFIL"), rs1.getString("NAME"),
                        rs1.getInt("NUM_SEG"), rs1.getString("SATELLITE"), null, null);
                filamento.add(filamenti);
            }

            PreparedStatement ps2 = CONN.prepareStatement(queryCount);
            ps2.setInt(1, seg1);
            ps2.setInt(2, seg2);
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();
            numRic = rs2.getInt(1);
            System.out.println("NUMERO RICORRENZE TROVATE: " + numRic);
        }catch (SQLException e){
            System.out.println(e.getMessage());
            numRic = 0;
        } finally{
            CONN.close();
        }
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        satColumn.setCellValueFactory(new PropertyValueFactory<>("satellite"));
        numSegColumn.setCellValueFactory(new PropertyValueFactory<>("numSeg"));
        tableView.setItems(null);
        tableView.setItems(filamento);

        return numRic;
    }

    // REQ 8 - Ricerca tutti i filamenti che sono interni a un cerchio o un quadrato
    public static void cercaInRegione(ObservableList<Filamento> filamento, TableView tableView, TableColumn id, TableColumn
                               nome, TableColumn satellite, TableColumn numSeg, float lungh, float centLon, float
                               centLat, boolean geom, int pagina) throws SQLException{

        Connessione.connettiti();

        String query;
        int offset = (pagina-1) * 20;

        float maxLat = centLat + lungh/2;
        float minLat = centLat - lungh/2;
        float maxLon = centLon + lungh/2;
        float minLon = centLon - lungh/2;

        String squareQuery = "SELECT *\n" +
                "FROM filamenti \n" +
                "WHERE \"NAME\" NOT IN ( \n" +
                "  SELECT DISTINCT c.\"NAME_FIL\"\n" +
                "  FROM punti_contorni p JOIN contorni c ON (p.\"N\" = c.\"NPCONT\" AND p.\"SATELLITE\" = c.\"SATELLITE\")\n" +
                "  WHERE p.\"GLON_CONT\" < '" + minLon + "' OR \n" +
                "    p.\"GLON_CONT\" > '" + maxLon + "' OR\n" +
                "    p.\"GLAT_CONT\" < '" + minLat + "' OR\n" +
                "    p.\"GLAT_CONT\" > '" + maxLat + "')\n" +
                "ORDER BY \"IDFIL\"\n" +
                "LIMIT 20 OFFSET '" + offset + "';";

        String circleQuery = "SELECT *\n" +
                "FROM filamenti\n" +
                "WHERE \"NAME\" NOT IN (\n" +
                "SELECT DISTINCT c.\"NAME_FIL\"\n" +
                "FROM punti_contorni p JOIN contorni c ON (p.\"N\" = c.\"NPCONT\" AND p.\"SATELLITE\" = c.\"SATELLITE\")\n" +
                "WHERE sqrt((p.\"GLON_CONT\"- '" + centLon + "')^2 + (p.\"GLAT_CONT\" + '" + centLat + "')^2) > '" + lungh + "') \n" +
                "ORDER BY \"IDFIL\" \n" +
                "LIMIT 20 OFFSET '" + offset + "';";

        if (geom){                  //true = quadrato
            query = squareQuery;
        } else {                    //false = cerchio
            query = circleQuery;
        }
        try{
            PreparedStatement ps1 = CONN.prepareStatement(query);
            filamento = FXCollections.observableArrayList();
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()){
                Filamento filamenti = new Filamento(rs1.getString("IDFIL"), rs1.getString("NAME"),
                        rs1.getInt("NUM_SEG"), rs1.getString("SATELLITE"), null, null);
                filamento.add(filamenti);
            }
        }catch (SQLException e){
        System.out.println(e.getMessage());
        } finally{
            CONN.close();
        }
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        nome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        satellite.setCellValueFactory(new PropertyValueFactory<>("satellite"));
        numSeg.setCellValueFactory(new PropertyValueFactory<>("numSeg"));
        tableView.setItems(null);
        tableView.setItems(filamento);
    }

    // REQ 9 - Cerca le stelle dentro un filamento
    public static ArrayList<Integer> cercaInFilamento(ObservableList<Stella> listaStelle, TableView tableView, TableColumn id,
                                                      TableColumn nameStar, TableColumn glon, TableColumn glat,
                                                      TableColumn flux, TableColumn type, int idFil, String satellite,
                                                    int pagina)
            throws SQLException{

        Connessione.connettiti();

        ArrayList<Integer> array = new ArrayList<>(3);
        int unbound;
        int prestellar;
        int protostellar;
        int offset = (pagina-1) * 20;

        // Serve per la verifica iniziale e trova le stelle
        String query = "SELECT *\n" +
                "FROM stelle\n" +
                "WHERE \"IDSTAR\" IN ( SELECT stella FROM stelle_in_filamenti_tmp WHERE  in_filamento = (" +
                                                                                        "SELECT \"NAME\" \n" +
                                                                                        "FROM filamenti \n" +
                                                                                        "WHERE \"IDFIL\" = ? AND \"SATELLITE\" = ?))" +
                "LIMIT 20 OFFSET ?";
        // trova il numero di stelle per ogni tipo
        String query2 = "SELECT s.\"TYPE\", COUNT(*) AS numero_tipo \n" +
                        "FROM stelle s \n" +
                        "WHERE s.\"IDSTAR\" IN ( SELECT stella \n" +
                                                "FROM stelle_in_filamenti_tmp \n" +
                                                "WHERE  in_filamento = (SELECT \"NAME\" \n" +
                                                                        "FROM filamenti \n" +
                                                                        "WHERE \"IDFIL\" = ? AND \"SATELLITE\" = ?)) \n" +
                        "GROUP BY s.\"TYPE\" \n" +
                        "ORDER BY s.\"TYPE\" ";
        //inserisce le stelle se la prima query di verifica fallisce
        String insert = "INSERT INTO stelle_in_filamenti_tmp  (\n" +
                "SELECT \"IDSTAR\", f.\"NAME\"\n" +
                "FROM punti_contorni p\n" +
                "     JOIN punti_contorni p2 ON (p2.\"N\" = (p.\"N\" + 1) AND p.\"SATELLITE\" = p2.\"SATELLITE\")\n" +
                "     JOIN contorni c ON (p.\"N\" = c.\"NPCONT\" AND p.\"SATELLITE\" = c.\"SATELLITE\")\n" +
                "     JOIN filamenti f ON (c.\"NAME_FIL\" = f.\"NAME\" AND c.\"SATELLITE\" = f.\"SATELLITE\"), stelle\n" +
                "WHERE f.\"SATELLITE\" =  ? AND f.\"IDFIL\"= ?   AND \"IDSTAR\" % 2 != 0 AND   \"IDSTAR\" % 3 != 0  --aggiungiamo queste 2 per velocizzare\n" +
                "GROUP BY \"IDSTAR\", f.\"NAME\"\n" +
                "HAVING abs(sum(atan(((p.\"GLON_CONT\"-\"GLON_ST\" )*(p2.\"GLAT_CONT\"-\"GLAT_ST\")-(p.\"GLAT_CONT\"-\"GLAT_ST\")*(p2.\"GLON_CONT\"-\"GLON_ST\"))/\n" +
                "\t\t    ((p.\"GLON_CONT\"-\"GLON_ST\")* (p2.\"GLON_CONT\"-\"GLON_ST\") + ((p.\"GLAT_CONT\"-\"GLAT_ST\"))*( (p2.\"GLAT_CONT\"-\"GLAT_ST\")))))) >= 0.01)\n" +
                "ON CONFLICT DO NOTHING";

//------------------------------------------------INSERT PER RICERCA GENERICA-------------------------------------------
                // sostituire alla clausola where della query precedente la seguente clausola
                //"WHERE f.\"SATELLITE\" =  ? AND f.\"IDFIL\"= ?   \n"
//------------------------------------------------------------------------------------------------------------------------

        try{
            //Prova a cercare i filamenti nella tabella temporanea.
            PreparedStatement ps1 = CONN.prepareStatement(query);
            ps1.setInt(1, idFil);
            ps1.setString(2, satellite);
            ps1.setInt(3, offset);
            listaStelle = FXCollections.observableArrayList();
            ResultSet rs1 = ps1.executeQuery();

            if (!rs1.next()) { //se rs1 non trova i filamenti nella tabella temporanea...
                //...allora li inserisce...
                System.out.println("Inserisco stelle del filamento  nella tabella");
                PreparedStatement ps2 = CONN.prepareStatement(insert);
                ps2.setString(1, satellite); ps2.setInt(2, idFil);
                ps2.executeUpdate();
                //... e ripete la ricerca.
                rs1.close();
                rs1 = ps1.executeQuery(); // ps1 perche' riesegue la query iniziale
            }

            unbound = 0; prestellar = 0; protostellar = 0;
            System.out.println("mostro le stelle");
            while (rs1.next()){
                // prende i dati dell'entita' "stella"
                Stella stella = new Stella(rs1.getInt("IDSTAR"), rs1.getString("NAME_STAR"),
                        rs1.getFloat("GLON_ST"), rs1.getFloat("GLAT_ST"),
                        rs1.getFloat("FLUX"), rs1.getString("TYPE"));
                // aggiunge la stella nell'array "listaStelle"
                listaStelle.add(stella);
            }
            System.out.println("conto i tipi");
            //In ogni caso vengono contati i tipi di stelle.
            PreparedStatement ps3 = CONN.prepareStatement(query2);
            ps3.setInt(1, idFil);
            ps3.setString(2, satellite);
            ResultSet rs3 = ps3.executeQuery();
            int i = 0;
            while (rs3.next()) {
                switch (i) {
                    case 0:
                        prestellar = rs3.getInt("numero_tipo");
                        break;
                    case 1:
                        protostellar = rs3.getInt("numero_tipo");
                        break;
                    case 2:
                        unbound = rs3.getInt("numero_tipo");
                        break;
                    default:
                        break;
                }
                i++;
            }
            System.out.println("prestar: "+ prestellar  + "\nprotostar: "+ protostellar + "\nunbound: " + unbound);

            //array contiene [numero prestellar, numero protostellar, numero unbound].*/
            array.add(0, prestellar); array.add(1, protostellar); array.add(2, unbound);
            System.out.println("FINE");
        }catch (SQLException e){
            array.add(0, 0); array.add(1, 0); array.add(2, 0);
            System.out.println(e.getMessage());
        } finally {
            CONN.close();
        }
        id.setCellValueFactory(new PropertyValueFactory<>("idStar"));
        nameStar.setCellValueFactory(new PropertyValueFactory<>("nameStar"));
        glon.setCellValueFactory(new PropertyValueFactory<>("glon"));
        glat.setCellValueFactory(new PropertyValueFactory<>("glat"));
        flux.setCellValueFactory(new PropertyValueFactory<>("flux"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        tableView.setItems(null);
        tableView.setItems(listaStelle);

        return array;
    }
    // REQ10 - Calcola i tipi di stelle interne a una regione rettangolare suddividendole in interne e esterne ai filamenti (nella regione)
    public static ArrayList<Integer> cercaInRegione(ObservableList<Stella> stella, TableView tableView, TableColumn id,
                                                  TableColumn nameStar, TableColumn glon, TableColumn glat,
                                                  TableColumn flux, TableColumn type, float h, float b, float lon,
                                                  float lat, int pagina) throws SQLException{
        Connessione.connettiti();

        ArrayList<Integer> array = new ArrayList<>(6);
        int unboundIn; int unboundOut;
        int prestellarIn; int prestellarOut;
        int protostellarIn; int protostellarOut;
        int offset = (pagina-1)*20;

        String query1 = "";

        try{
            PreparedStatement ps1 = CONN.prepareStatement(query1);
            stella = FXCollections.observableArrayList();
            ResultSet rs1 = ps1.executeQuery();
            unboundIn = 0; unboundOut = 0;
            prestellarIn = 0; prestellarOut = 0;
            protostellarIn = 0; protostellarOut = 0;
            while (rs1.next()){
                Stella stelle = new Stella(rs1.getInt("IDSTAR"), rs1.getString("NAME_STAR"),
                        rs1.getFloat("GLON_ST"), rs1.getFloat("GLAT_ST"),
                        rs1.getFloat("FLUX"), rs1.getString("TYPE"));
                stella.add(stelle);
            }
            /*if (condizione){
                switch (rs1.getString("TYPE")) {
                    case "PRESTELLAR":
                        prestellarIn += 1;
                        break;
                    case "PROTOSTELLAR":
                        protostellarIn += 1;
                        break;
                    case "UNBOUND":
                        unboundIn += 1;
                        break;
                }
            }else {
                switch (rs1.getString("TYPE")) {
                    case "PRESTELLAR":
                        prestellarOut += 1;
                        break;
                    case "PROTOSTELLAR":
                        protostellarOut += 1;
                        break;
                    case "UNBOUND":
                        unboundOut += 1;
                        break;
            }*/
            array.add(0, unboundIn); array.add(1, prestellarIn); array.add(2, protostellarIn);
            array.add(3, unboundOut); array.add(4, prestellarOut); array.add(5, protostellarOut);
        }catch (SQLException e){
            array.add(0, 0); array.add(1, 0); array.add(2, 0);
            array.add(3, 0); array.add(4, 0); array.add(5, 0);
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally{
            CONN.close();
        }
        id.setCellValueFactory(new PropertyValueFactory<>("idStar"));
        nameStar.setCellValueFactory(new PropertyValueFactory<>("nameStar"));
        glon.setCellValueFactory(new PropertyValueFactory<>("glon"));
        glat.setCellValueFactory(new PropertyValueFactory<>("glat"));
        flux.setCellValueFactory(new PropertyValueFactory<>("flux"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        tableView.setItems(null);
        tableView.setItems(stella);

        return array;
    }
    //REQ 11 - calcola la distanza minima degli estremi del segmento dal contorno
    public static ArrayList<Float> calcolaDistSegCon(int idSeg, int idFil, String satellite) throws SQLException{

        Connessione.connettiti();

        ArrayList<Float> distanze = new ArrayList<>(2);
        String query = "SELECT round(min(sqrt((u.\"GLON_CONT\"- p.\"GLON_BR\")^2 + (u.\"GLAT_CONT\" - p.\"GLAT_BR\")^2)),5) AS distanza_dal_contorno\n" +
                "FROM punti_segmenti p JOIN contorni c ON p.\"NAME_FIL\" = c.\"NAME_FIL\" JOIN punti_contorni u ON (c.\"NPCONT\" = u.\"N\" and c.\"SATELLITE\" = u.\"SATELLITE\")\n" +
                "WHERE p.\"SEGMENTO\" = ? AND p.\"NAME_FIL\" = (SELECT \"NAME\" FROM filamenti WHERE \"IDFIL\" = ? AND \"SATELLITE\" = ?) AND (p.\"N\" = 1 OR p.\"N\" in (\n" +
                "  SELECT max(q.\"N\")\n" +
                "  FROM punti_segmenti q\n" +
                "  WHERE p.\"SEGMENTO\" = q.\"SEGMENTO\" AND p.\"NAME_FIL\" = q.\"NAME_FIL\"))\n" +
                "GROUP BY p.\"IDSEG\",p.\"SEGMENTO\", p.\"GLON_BR\", p.\"GLAT_BR\", p.\"N\";\n";

        try{
            PreparedStatement ps1 = CONN.prepareStatement(query);
            ps1.setInt(1,idSeg);
            ps1.setInt(2, idFil);
            ps1.setString(3, satellite);
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            distanze.add(0, rs1.getFloat("distanza_dal_contorno"));
            rs1.next();
            distanze.add(1, rs1.getFloat("distanza_dal_contorno"));
            rs1.close();
        }catch (SQLException e){
            e.printStackTrace();
            distanze.add(0, null); distanze.add(1, null);
        } finally{
            CONN.close();
        }
        return distanze;
    }

    // REQ 11 - Cerca i segmenti di un filamento per aggiornare la choice box
    public static ArrayList<Integer> trovaSegmenti(int idFil, String satellite) throws SQLException{
        Connessione.connettiti();
        ArrayList<Integer> listaChoiceBox = new ArrayList<>();

        String query = "SELECT s.\"IDBRANCH\"\n" +
                "FROM segmenti s JOIN filamenti f ON s.\"NAME_FIL\" = f.\"NAME\"\n" +
                "WHERE f.\"IDFIL\" = ? AND f.\"SATELLITE\" = ? \n" +
                "ORDER BY s.\"IDBRANCH\"";
        try {
            PreparedStatement ps1 = CONN.prepareStatement(query);
            ps1.setInt(1, idFil);
            ps1.setString(2, satellite);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()){
                listaChoiceBox.add(rs1.getInt("IDBRANCH"));
            }
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            CONN.close();
        }
        return listaChoiceBox;
    }

    /**/
    //REQ 12 - Trova la distanza delle stelle in un filamento dalla spina dorsale. Permette di ordinare per distanza o flusso
    public static void calcolaDistStellaSpina(ObservableList<Stella> listaStelle, TableView tableView, TableColumn id,
                                              TableColumn nameStar, TableColumn glon, TableColumn glat,
                                              TableColumn flux, TableColumn type, int idFil, String satellite,
                                              int pagina) throws SQLException{
        Connessione.connettiti();

        ArrayList<Integer> array = new ArrayList<>(3);
        int unbound;
        int prestellar;
        int protostellar;
        int offset = (pagina-1) * 20;

        // Serve per la verifica iniziale e trova le stelle
        String queryStelle = "SELECT *\n" +
                "FROM stelle\n" +
                "WHERE \"IDSTAR\" IN ( SELECT stella FROM stelle_in_filamenti_tmp WHERE  in_filamento = (" +
                "SELECT \"NAME\" \n" +
                "FROM filamenti \n" +
                "WHERE \"IDFIL\" = ? AND \"SATELLITE\" = ?))" +
                "LIMIT 1";
        // calcola la distanza minimo di ogni stella nel filamento dalla spina dorsale
        String queryDistanze = "";
        //inserisce le stelle se la prima query di verifica fallisce
        String insert = "INSERT INTO stelle_in_filamenti_tmp  (\n" +
                "SELECT \"IDSTAR\", f.\"NAME\"\n" +
                "FROM punti_contorni p\n" +
                "     JOIN punti_contorni p2 ON (p2.\"N\" = (p.\"N\" + 1) AND p.\"SATELLITE\" = p2.\"SATELLITE\")\n" +
                "     JOIN contorni c ON (p.\"N\" = c.\"NPCONT\" AND p.\"SATELLITE\" = c.\"SATELLITE\")\n" +
                "     JOIN filamenti f ON (c.\"NAME_FIL\" = f.\"NAME\" AND c.\"SATELLITE\" = f.\"SATELLITE\"), stelle\n" +
                "WHERE f.\"SATELLITE\" =  ? AND f.\"IDFIL\"= ?   AND \"IDSTAR\" % 2 != 0 AND   \"IDSTAR\" % 3 != 0  --aggiungi queste 2 per velocizzare\n" +
                "GROUP BY \"IDSTAR\", f.\"NAME\"\n" +
                "HAVING abs(sum(atan(((p.\"GLON_CONT\"-\"GLON_ST\" )*(p2.\"GLAT_CONT\"-\"GLAT_ST\")-(p.\"GLAT_CONT\"-\"GLAT_ST\")*(p2.\"GLON_CONT\"-\"GLON_ST\"))/\n" +
                "\t\t    ((p.\"GLON_CONT\"-\"GLON_ST\")* (p2.\"GLON_CONT\"-\"GLON_ST\") + ((p.\"GLAT_CONT\"-\"GLAT_ST\"))*( (p2.\"GLAT_CONT\"-\"GLAT_ST\")))))) >= 0.01)\n" +
                "ON CONFLICT DO NOTHING";

//------------------------------------------------INSERT PER RICERCA GENERICA-------------------------------------------
        // sostituire alla clausola where della query precedente la seguente clausola
        //"WHERE f.\"SATELLITE\" =  ? AND f.\"IDFIL\"= ?   \n"
//------------------------------------------------------------------------------------------------------------------------
        try{
            //Prova a cercare i filamenti nella tabella temporanea.
            PreparedStatement ps1 = CONN.prepareStatement(queryStelle);
            ps1.setInt(1, idFil);
            ps1.setString(2, satellite);

            listaStelle = FXCollections.observableArrayList();
            ResultSet rs1 = ps1.executeQuery();

            if (!rs1.next()) { //se rs1 non trova i filamenti nella tabella temporanea...
                //...allora li inserisce...
                System.out.println("Inserisco stelle del filamento  nella tabella");
                PreparedStatement ps2 = CONN.prepareStatement(insert);
                ps2.setString(1, satellite); ps2.setInt(2, idFil);
                ps2.executeUpdate();
                //... e ripete la ricerca.
                rs1.close();
                rs1 = ps1.executeQuery(); // ps1 perche' riesegue la query iniziale
            }

            while (rs1.next()){

                Stella stella = new Stella(rs1.getInt("IDSTAR"), rs1.getString("NAME_STAR"),
                        rs1.getFloat("GLON_ST"), rs1.getFloat("GLAT_ST"),
                        rs1.getFloat("FLUX"), rs1.getString("TYPE"));

                listaStelle.add(stella);
            }

        }catch (SQLException e){
            array.add(0, 0); array.add(1, 0); array.add(2, 0);
            System.out.println(e.getMessage());
        } finally {
            CONN.close();
        }
        id.setCellValueFactory(new PropertyValueFactory<>("idStar"));
        nameStar.setCellValueFactory(new PropertyValueFactory<>("nameStar"));
        glon.setCellValueFactory(new PropertyValueFactory<>("glon"));
        glat.setCellValueFactory(new PropertyValueFactory<>("glat"));
        flux.setCellValueFactory(new PropertyValueFactory<>("flux"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        tableView.setItems(null);
        tableView.setItems(listaStelle);

    }
}




