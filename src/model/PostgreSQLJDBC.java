package model;

import java.sql.Connection;
import java.sql.DriverManager;

public class PostgreSQLJDBC {

	private Connection connection;
	private String user;

	public PostgreSQLJDBC(String url, String user, String pwd) {

		this.user = user;
		
		System.out.printf("Opening database... ");
		try {
			Class.forName("org.postgresql.Driver");
			this.setConnection(DriverManager.getConnection(url, user, pwd));
			System.out.printf("Opened database %s successfully with %s \n\n", url, user);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.err.printf("Database %s with %s could not be opened \n\n", url, user);
			this.connection = null;
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	public String getUser() {
		return user;
	}
	
	public boolean hasConnection() {
		return this.connection != null;
	}
	
}
