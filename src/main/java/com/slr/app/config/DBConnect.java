package com.slr.app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {

	private final String url = "jdbc:postgresql://localhost:5432/dbslr";
	private final String user = "postgres";
	private final String password = "postgres";
	

    private static DBConnect instance;
    private Connection connection;
	
    
    private DBConnect() throws SQLException {
    	try {
    		Class.forName("org.postgresql.Driver");
    		this.connection = DriverManager.getConnection(url, user, password);
    	 } catch (ClassNotFoundException ex) {
             System.out.println("Database Connection Creation Failed : " + ex.getMessage());
         }
    }
	
    public Connection getConnection() {
        return connection;
    }
    
    public static DBConnect getInstance() throws SQLException {
        if (instance == null) {
            instance = new DBConnect();
        } else if (instance.getConnection().isClosed()) {
            instance = new DBConnect();
        }

        return instance;
    }
	
    public void closeConnection() throws SQLException
    {
    	instance.closeConnection();
    }
}
