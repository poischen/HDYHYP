package com.example;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/*
@class helper programm to cluster the collected pictures during the "HDYHYP" study
 */
public class PicAnalysis {

    public static void main(String args[]) throws ClassNotFoundException, SQLException {

        String faceValue = args[0];
        String path = args[1];
        String db = args[2];
        String writecolumn = "faceValue";
        String readcolumn = "photoName";
        String driverName = "org.sqlite.JDBC";

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
                for (int i=0; i<listOfFiles.length; i++){
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
            try { connection.close(); } catch (Exception ignore) {}
        }


    }

}