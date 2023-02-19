package com.example.a9_11;

import android.os.StrictMode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class DbSetup {


    public Connection ConnectDB() {
        String connectionUrl = "jdbc:jtds:sqlserver://192.168.1.245:1433;databaseName=kianMorphoTab;user=Sa;password=Moh@123";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            Connection connection = DriverManager.getConnection(connectionUrl);
            return connection;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean ExecuteInsertintoDb(String Command) {
        String connectionUrl = "jdbc:jtds:sqlserver://192.168.1.245:1433;databaseName=kianMorphoTab;user=Sa;password=Moh@123";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ResultSet resultSet = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            Connection connection = DriverManager.getConnection(connectionUrl);
            PreparedStatement prepsInsertProduct = connection.prepareStatement(Command, Statement.RETURN_GENERATED_KEYS);
            prepsInsertProduct.execute();
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean savePhotoToDatabase(String Image,String NatId, boolean isLast) {
        try {
            if (ConnectDB() != null) {

                String insertSql = "INSERT INTO FaceTranining (Img,IsLast,NationalId) VALUES " + "('" + Image + "','" + isLast +"','" + NatId + "');";

                PreparedStatement prepsInsertProduct = ConnectDB().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                prepsInsertProduct.execute();
                return true;

            } else
                return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean InsertImgValidation(String imgStr,int code) {
        try {
            if (ConnectDB() != null) {

                String insertSql = "INSERT INTO FingerSearch (Code,Img) VALUES " + "('" + code + "','" + imgStr + "');";

                PreparedStatement prepsInsertProduct = ConnectDB().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                prepsInsertProduct.execute();
                return true;

            } else
                return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getFinger(int Code) {
        if (ConnectDB() != null) {
            String selectSql = "SELECT NationalId from FingerRecognation where Code ='" + Code + "'";
            try {
                Visitor V = null;
                ResultSet resultSet = null;
                Statement statement = ConnectDB().createStatement();
                resultSet = statement.executeQuery(selectSql);
                while (resultSet.next()) {
                    String NatId=resultSet.getString(1);
                    return NatId;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public Visitor GetCardHolderByNatID(String natId) {
        Connection DbConnect = ConnectDB();
        if (DbConnect != null) {
            try {
                Visitor V = null;
                ResultSet resultSet = null;
                Statement statement = DbConnect.createStatement();
                {
                    // Create and execute a SELECT SQL statement.
                    String selectSql = "SELECT Name,NationalId,Rank,Weapon,Address,Religion,MaritalStatus,Image" +
                            ",Unit,Description,MilitaryId from CardHolderSetup where NationalId ='" + natId + "'";
                    resultSet = statement.executeQuery(selectSql);
                    // Print results from select statement
                    while (resultSet.next()) {
                        V = new Visitor(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
                                resultSet.getString(4), resultSet.getString(5), resultSet.getString(6), resultSet.getString(7),
                                resultSet.getString(8), resultSet.getString(9), resultSet.getString(10),
                                resultSet.getString(11));
                        break;

                    }
                }
                return V;
            } catch (Exception ex) {
                ex.printStackTrace();

                return null;
            }
        }
        return null;
    }

    public String checkFaceRecognation(int code) {
        if (ConnectDB() != null) {
            String selectSql = "SELECT NationalId from FaceRecognation where Code ='" + code + "'";
            try {
                Visitor V = null;
                ResultSet resultSet = null;
                Statement statement = ConnectDB().createStatement();
                resultSet = statement.executeQuery(selectSql);
                while (resultSet.next()) {
                    String NatId=resultSet.getString(1);
                    return NatId;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public boolean InsertToFaceSearch(String imgStr,int code) {
        try {
            if (ConnectDB() != null) {

                String insertSql = "INSERT INTO FaceSearch (Code,Img) VALUES " + "('" + code + "','" + imgStr + "');";

                PreparedStatement prepsInsertProduct = ConnectDB().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                prepsInsertProduct.execute();
                return true;

            } else
                return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /*public Visitor GetCardHolderByCardNum(String NatId) {
        Connection DbConnect = ConnectDB();
        if (DbConnect != null) {
            try {
                Visitor V = null;
                ResultSet resultSet = null;
                Statement statement = DbConnect.createStatement();
                // Create and execute a SELECT SQL statement.
                String selectSql = "SELECT Name,NationalId,Rank,Weapon,Address,Religion,MaritalStatus,Image" +
                        ",Unit,Description,MilitaryId from CardHolderSetup where CardNumber ='" + NatId + "'";
                resultSet = statement.executeQuery(selectSql);
                // Print results from select statement
                while (resultSet.next()) {
                    V = new Visitor(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
                            resultSet.getString(4), resultSet.getString(5), resultSet.getString(6), resultSet.getString(7),
                            resultSet.getString(8), resultSet.getString(9), resultSet.getString(10),
                            resultSet.getString(11));
                    break;
                }
                return V;
            } catch (Exception ex) {
                ex.printStackTrace();

                return null;
            }
        }
        return null;*/
    }


