package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.PostgreSQLJDBC;
import model.FireEngine;
import model.Intervention;
import model.Sensor;
import model.Station;

public class EmergencyManager {
	public EmergencyManager() {
		
		System.out.println("EmergencyManager initialized successfully");
		
		Connection EmergencyManagerConnection = 
				new PostgreSQLJDBC("jdbc:postgresql://manny.db.elephantsql.com:5432/", 
						"ngcbqvhq", 
						"Ppjleq3n6HQF5qPheDze2QFzG4LHxTAf").getConnection();
		
		String getSensorsQuery = "select f.id, f.intensity, f.handled, p.real_x, p.real_y from fire f join real_pos p on f.id_real_pos = p.id where intensity > 0 or p.id in (select id_real_pos from station)";
		String getStations = "select s.id, s.id_real_pos, s.name from station s";
		String getFireEnginesQuery = "select fe.id, fe.x_pos, fe.y_pos, fe.rank, fe.id_station from fire_engine fe";
		String getOngoingInterventionsQuery = "select i.id, i.id_fire_engine, i.id_fire, i.start_ts, f.id_real_pos from intervention i join fire f on i.id_fire = f.id";

		try {
			
			// Get Sensors
			PreparedStatement pstSensors = EmergencyManagerConnection.prepareStatement(getSensorsQuery);
			ResultSet resultSetSensors = pstSensors.executeQuery();	
			
			// Get Fire Stations
			PreparedStatement pstStations = EmergencyManagerConnection.prepareStatement(getStations);
			ResultSet resultSetStations = pstStations.executeQuery();	
			
			// Get Fire Engines
			PreparedStatement pstFireEngines = EmergencyManagerConnection.prepareStatement(getFireEnginesQuery);
			ResultSet resultSetFireEngines = pstFireEngines.executeQuery();	
			
			// Get interventions
			PreparedStatement pstInterventions = EmergencyManagerConnection.prepareStatement(getOngoingInterventionsQuery);
			ResultSet resultSetInterventions = pstInterventions.executeQuery();	
			
			EmergencyManagerConnection.close();
			
			List<Sensor> sensors = new ArrayList<>();
			List<Station> stations = new ArrayList<>();
			
			while (resultSetSensors.next()) {
				
				int id = Integer.parseInt(resultSetSensors.getString("id"));
				double x = Double.parseDouble(resultSetSensors.getString("real_x"));
				double y = Double.parseDouble(resultSetSensors.getString("real_y"));
				int intensity = Integer.parseInt(resultSetSensors.getString("intensity"));
				int handled = Integer.parseInt(resultSetSensors.getString("handled"));
			    
			    sensors.add(new Sensor(id,x,y,intensity,handled));
			}
				
			while (resultSetStations.next()) {
				
				int id = Integer.parseInt(resultSetStations.getString("id"));
				int id_real_pos = Integer.parseInt(resultSetStations.getString("id_real_pos"));
				String name = resultSetStations.getString("name");
				
				Sensor sensorStation = null;
				for(Sensor sensor : sensors){
					if(sensor.getId() == id_real_pos) {
						sensorStation = sensor;
						break;
					}
				}
				
				stations.add(new Station(id,name,sensorStation));
			}
			
			while (resultSetFireEngines.next()) {
				
				int id = Integer.parseInt(resultSetFireEngines.getString("id"));
				double x = Double.parseDouble(resultSetFireEngines.getString("x_pos"));
				double y = Double.parseDouble(resultSetFireEngines.getString("y_pos"));
				int rank = Integer.parseInt(resultSetFireEngines.getString("rank"));
				int id_station = Integer.parseInt(resultSetFireEngines.getString("id_station"));
				
				FireEngine fireEngine = new FireEngine(id,x,y,rank,false);
				
				for(Station station : stations){
					if(station.getId() == id_station) {
						station.addFireEngine(fireEngine);
						break;
					}
				}
			}
			
			while (resultSetInterventions.next()) {
				
				int id = Integer.parseInt(resultSetInterventions.getString("id"));
				int id_real_pos = Integer.parseInt(resultSetInterventions.getString("id_real_pos"));
				String start_ts = resultSetInterventions.getString("start_ts");
				
				Sensor sensorFire = null;
				for(Sensor sensor : sensors){
					if(sensor.getId() == id_real_pos) {
						sensorFire = sensor;
						break;
					}
				}
				
				Intervention intervention = new Intervention(id,sensorFire,start_ts);
			}
			
			System.out.println("got all sensors : ");
			for(Sensor sensor : sensors){
				System.out.println(sensor);
			}
			
			System.out.println("got all stations : ");
			for(Station station : stations){
				System.out.println(station);
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		// insert into intervention values(id, id_fire_engine, id_fire)
	}
}
