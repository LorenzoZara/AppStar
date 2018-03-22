package persistence;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static persistence.Connessione.CONN;

public class FileDao {

    public static void importaFile(File file, String relazione) throws SQLException {

        Connessione.connettiti();
        String importazione = "COPY " + relazione +" FROM \'" + file.getPath() + "\' DELIMITER \',\' csv HEADER";
        //Si e notato che il path del file da importare non deve contenere \Desktop\.
        //Ho ulteriormente notato che PostgreSQL mi legge i file in formato csv solamente se li prendo
        //disco locale C.

        try {
            PreparedStatement ps1 = CONN.prepareStatement("DELETE FROM " + relazione);
            ps1.executeUpdate();

            //CONN.setReadOnly(true);
            PreparedStatement ps2 = CONN.prepareStatement(importazione);
            ps2.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            CONN.close();
        }
    }

    //TODO: Cosa succede dopo gli import da parte dell'utente amministratore.
    public void distribuisciDati(){
        //Distrubuisce i dati contenuti negli imp nel DB di AppStar.

        Connessione.connettiti();

        riempiFilamenti();
        riempiSatelliti();
        riempiSegmenti();

    }

    public void riempiFilamenti(){
        //Riempe la tabella "filamenti".

        String fillQuery = "INSERT INTO filamenti VALUES(SELECT \'IDFIL\', \'NAME\' " +
                                                        "FROM filamenti_imp" +
                                                        "WHERE (\'IDFIL\',\'NAME\') NOT IN (    SELECT \'IDFIL\', \'NAME\'" +
                                                                                                "FROM filamenti))";

        String updateQuery1 = "UPDATE filamenti f SET \"NUMSEG\" = (SELECT COUNT(*) " +
                                                                    "FROM scheletri_imp si " +
                                                                    "WHERE (f.\'IDFIL\' = si.\'IDFIL\' AND" +
                                                                    " \'N\' = 1))";
        try {
            PreparedStatement ps1 = CONN.prepareStatement(fillQuery);
            ps1.executeUpdate();
            PreparedStatement ps2 = CONN.prepareStatement(updateQuery1);
            ps2.executeUpdate();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void riempiSegmenti(){
        //Riempe la tabella "segmenti".

        String fillQuery = "INSERT INTO segmenti (  SELECT \'IDBRANCH\', \'TYPE\', \'IDFIL\'" +
                                                    "FROM scheletri_imp" +
                                                    "WHERE \'N\' = 1)";

        String updateQuery1 =   "UPDATE segmenti" +
                                "SET \'NAME_FIL\' = (   SELECT \'NAME\'" +
                                                        "FROM filamenti_imp f" +
                                                        "WHERE f.\'IDFIL\' = segmenti.\'IDFIL\')";

        try {
            PreparedStatement ps1 = CONN.prepareStatement(fillQuery);
            ps1.executeUpdate();
            PreparedStatement ps2 = CONN.prepareStatement(updateQuery1);
            ps2.executeUpdate();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void riempiSatelliti(){
        //Riempe la tabella "satelliti".

        String fillQuery =  "SELECT DISTINCT \'NAME_SAT\'" +
                            "FROM filamenti_imp;";

        try{
            PreparedStatement ps1 = CONN.prepareStatement(fillQuery);
            ps1.executeUpdate();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void riempiAgenzie(){
        //Riempe la tabella "agenzie".

        String fillQuery; //TODO: non ho la minima idea di come si possa riempire.

        /*try{
            PreparedStatement ps1 = CONN.prepareStatement(fillQuery);
            ps1.executeUpdate();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }*/
    }
}