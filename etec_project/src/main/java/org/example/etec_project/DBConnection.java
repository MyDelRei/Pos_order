package org.example.etec_project;

import java.sql.*;

public class DBConnection {
    private Connection connection ;
    private final String HostName= "localhost";
    private final String Username = "root";
    private final String password = "";
    private final String DB = "pos_system";
    private final int port = 3306;
    static DBConnection instance = new DBConnection();

    private DBConnection() {

        try{        //jbc:mysql://localhost:3306/testing __original syntax__
            String url = "jdbc:mariadb://"+HostName+":"+port+"/"+DB;
            connection = DriverManager.getConnection(url,Username,password);
            System.out.println("success");
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }


}


