package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Random;
import java.util.Timer;

import model.PostgreSQLJDBC;

public class EmergencyManager {
	public EmergencyManager() {
		
		System.out.println("EmergencyManager initialized successfully");
		
		Connection EmergencyManagerConnection = 
				new PostgreSQLJDBC("jdbc:postgresql://manny.db.elephantsql.com:5432/", 
						"ngcbqvhq", 
						"Ppjleq3n6HQF5qPheDze2QFzG4LHxTAf").getConnection();
		
		String getOngoingInterventionsQuery = "select id, id_fire_engine, id_fire from intervention where "
				+ "id_fire_engine in (select id_fire_engine from fire where intensity > 0)";

		try {
			
			PreparedStatement pst = EmergencyManagerConnection.prepareStatement(getOngoingInterventionsQuery);
			ResultSet rsActiveFires = pst.executeQuery();
			EmergencyManagerConnection.close();
			System.out.println("got all active fires : ");
			ResultSetMetaData rsmd = rsActiveFires.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			while (rsActiveFires.next()) {
			    for (int i = 1; i <= columnsNumber; i++) {
			        if (i > 1) System.out.print(",  ");
			        String columnValue = rsActiveFires.getString(i);
			        System.out.print(columnValue + " " + rsmd.getColumnName(i));
			    }
			    System.out.println("");
			}
	        }
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		// insert into intervention values(id, id_fire_engine, id_fire)
	}
}
