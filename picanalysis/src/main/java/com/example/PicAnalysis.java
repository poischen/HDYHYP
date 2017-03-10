package com.example;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Int;

/*
@class helper programm to cluster the collected pictures during the "HDYHYP" study
@param command face or join for writing faceValues or join tables
@param path
@param database
@param db size
 */
public class PicAnalysis {

    public static void main(String args[]) throws ClassNotFoundException, SQLException {

        //String faceValue = args[0];
        String faceValue;
        String command = args[0];
        String path = args[1];
        String db = args[2];
        String dbsize = args[3];
        int dbsizeint = new Integer(dbsize);
        String driverName = "org.sqlite.JDBC";

        switch (command){
            case ("face"):
                System.out.println("Write face values");
                String writecolumn = "faceValue";
                String readcolumn = "photoName";
                ArrayList<String> faceValues = new ArrayList<>();
                faceValues.add("none");
                faceValues.add("partially_0e_1m");
                faceValues.add("partially_1e_0m");
                faceValues.add("partially_1e_1m");
                faceValues.add("partially_2e_0m");
                faceValues.add("partially_2e_1m");
                faceValues.add("whole");
                faceValues.add("whole_partiallylandmarks");
                faceValues.add("partially_none");

                for(int i=0; i<faceValues.size(); i++) {
                    faceValue = faceValues.get(i);
                    System.out.println(faceValue);

                    //geting all pictures
                    File folder = new File(path + File.separator + faceValue + File.separator);
                    File[] listOfFiles = folder.listFiles();
                    for (File file : listOfFiles) {
                        if (file.isFile() && !(file.getAbsolutePath().contains("DataBase"))) {
                            //System.out.println(file.getName());
                        }
                    }
                    System.out.println("getting files done");


                    //get datbase and write faceValue
                    Class.forName(driverName);
                    String dbUrl = "jdbc:sqlite:" + path + File.separator + db;
                    int iTimeout = 5;

                    Connection connection = DriverManager.getConnection(dbUrl);
                    try {
                        Statement statement = connection.createStatement();
                        statement.setQueryTimeout(iTimeout);
                        try {
                            for (int j=0; j<listOfFiles.length; j++){
                                String updateDataString = "UPDATE HDYHYPDataCollection SET " + writecolumn +"='" + faceValue + "' WHERE " + readcolumn + "='" + listOfFiles[i].getName() +"'";
                                String updateSurveyString = "UPDATE HDYHYPSurveyData SET " + writecolumn +"='" + faceValue + "' WHERE " + readcolumn + "='" + listOfFiles[i].getName() +"'";
                                try {
                                    statement.executeUpdate(updateDataString);
                                    statement.executeUpdate(updateSurveyString);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    System.out.print("Failed writing into db" + e);
                                }
                            }

                        } finally {
                            try { statement.close(); } catch (Exception ignore) {}
                        }
                    } finally {
                        try { connection.close();
                            System.out.println("done, faceValue: " + faceValue);} catch (Exception ignore) {}
                    }

                }

                break;
            case ("join"):
                System.out.println("Join tables");






                String devicePosition = "devicePosition";
                String holdingHand = "holdingHand";
                String userPosture = "userPosture";
                String userPosition = "userPosition";
                String userPositionOtherAnswer = "userPositionOtherAnswer";
                String userDoingSomething = "userDoingSomething";
                String userDoingSomethingOtherAnswer = "userDoingSomethingOtherAnswer";

                String joincolumn = "photoName";


                    //get database and join
                    Class.forName(driverName);
                    String dbUrl = "jdbc:sqlite:" + path + File.separator + db;
                    int iTimeout = 5;



                    Connection connection = DriverManager.getConnection(dbUrl);
                    try {
                        Statement statement = connection.createStatement();
                        statement.setQueryTimeout(iTimeout);
                        try {
                            for(int i=0; i<=dbsizeint; i++) {
                                System.out.println("Dataset " + i);

                                String updateDevicePositionString = "UPDATE HDYHYPDataCollection SET devicePosition = (SELECT HDYHYPSurveyData.devicePosition FROM HDYHYPSurveyData WHERE HDYHYPDataCollection.photoName = HDYHYPSurveyData.photoName)";
                                String updateholdingHandString = "UPDATE HDYHYPDataCollection SET holdingHand = (SELECT HDYHYPSurveyData.holdingHand FROM HDYHYPSurveyData WHERE HDYHYPDataCollection.photoName = HDYHYPSurveyData.photoName)";
                                String updateuserPostureString = "UPDATE HDYHYPDataCollection SET userPosture = (SELECT HDYHYPSurveyData.userPosture FROM HDYHYPSurveyData WHERE HDYHYPDataCollection.photoName = HDYHYPSurveyData.photoName)";
                                String updateuserPositionString = "UPDATE HDYHYPDataCollection SET userPosition = (SELECT HDYHYPSurveyData.userPosition FROM HDYHYPSurveyData WHERE HDYHYPDataCollection.photoName = HDYHYPSurveyData.photoName)";
                                String updateuserPositionOtherAnswerString = "UPDATE HDYHYPDataCollection SET userPositionOtherAnswer = (SELECT HDYHYPSurveyData.userPositionOtherAnswer FROM HDYHYPSurveyData WHERE HDYHYPDataCollection.photoName = HDYHYPSurveyData.photoName)";
                                String updateuserDoingSomethingString = "UPDATE HDYHYPDataCollection SET userDoingSomething = (SELECT HDYHYPSurveyData.userDoingSomething FROM HDYHYPSurveyData WHERE HDYHYPDataCollection.photoName = HDYHYPSurveyData.photoName)";
                                String updateuserDoingSomethingOtherAnswerString = "UPDATE HDYHYPDataCollection SET userDoingSomethingOtherAnswer = (SELECT HDYHYPSurveyData.userDoingSomethingOtherAnswer FROM HDYHYPSurveyData WHERE HDYHYPDataCollection.photoName = HDYHYPSurveyData.photoName)";

                                try {
                                    statement.executeUpdate(updateDevicePositionString);
                                    statement.executeUpdate(updateholdingHandString);
                                    statement.executeUpdate(updateuserPostureString);
                                    statement.executeUpdate(updateuserPositionString);
                                    statement.executeUpdate(updateuserPositionOtherAnswerString);
                                    statement.executeUpdate(updateuserDoingSomethingString);
                                    statement.executeUpdate(updateuserDoingSomethingOtherAnswerString);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    System.out.print("Failed writing into db" + e);
                                }
                            }

                        } finally {
                            try { statement.close(); } catch (Exception ignore) {}
                        }
                    } finally {
                        try { connection.close();
                            System.out.println("done, faceValue: " + faceValue);} catch (Exception ignore) {}
                    }













                break;

        }

    }

}