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
		
		String getActiveSensorsQuery = "select f.id, f.intensity, f.handled, p.real_x, p.real_y from fire f join real_pos p on f.id_real_pos = p.id where intensity > 0";
		String getStationSensorsQuery = "select f.id, f.intensity, f.handled, p.real_x, p.real_y from fire f join real_pos p on f.id_real_pos = p.id where p.id in (select id_real_pos from station)";

		try {
			
			// Get Active Sensors
			PreparedStatement pstActiveSensors = EmergencyManagerConnection.prepareStatement(getActiveSensorsQuery);
			ResultSet resultSetActiveSensors = pstActiveSensors.executeQuery();
			
			// Get Station Sensors
			PreparedStatement pstStationSensors = EmergencyManagerConnection.prepareStatement(getStationSensorsQuery);
			ResultSet resultSetStationSensors = pstStationSensors.executeQuery();
			
			EmergencyManagerConnection.close();
			
			List<Sensor> activeSensors = new ArrayList<>();
			List<Sensor> stationSensors = new ArrayList<>();
			
			while (resultSetActiveSensors.next()) {
				
				int id = Integer.parseInt(resultSetActiveSensors.getString("id"));
				double x = Double.parseDouble(resultSetActiveSensors.getString("real_x"));
				double y = Double.parseDouble(resultSetActiveSensors.getString("real_y"));
				int intensity = Integer.parseInt(resultSetActiveSensors.getString("intensity"));
				int handled = Integer.parseInt(resultSetActiveSensors.getString("handled"));
				
				activeSensors.add(new Sensor(id,x,y,intensity,handled));
			}
			
			while (resultSetStationSensors.next()) {
				
				int id = Integer.parseInt(resultSetStationSensors.getString("id"));
				double x = Double.parseDouble(resultSetStationSensors.getString("real_x"));
				double y = Double.parseDouble(resultSetStationSensors.getString("real_y"));
				int intensity = Integer.parseInt(resultSetStationSensors.getString("intensity"));
				int handled = Integer.parseInt(resultSetStationSensors.getString("handled"));
				
				stationSensors.add(new Sensor(id,x,y,intensity,handled));
			}
			
			System.out.println("got all active sensors : ");
			for(Sensor sensor : activeSensors){
				System.out.println(sensor);
			}
			
			System.out.println("got all station sensors : ");
			for(Sensor sensor : stationSensors){
				System.out.println(sensor);
			}
			
			boolean hasFire = false;
			
			System.out.println("check handled > intensity for each sensor : ");
			for(Sensor sensor : activeSensors){
				if(sensor.getHandled() > sensor.getIntensity()) {
					hasFire = true;
					break;
				}
			}
				
			if(hasFire) {
				List<Station> stations = new ArrayList<>();
				
				// Get Fire Stations
				String getStations = "select s.id, s.id_real_pos, s.name from station s";
	            PreparedStatement pstStations = EmergencyManagerConnection.prepareStatement(getStations);
	            ResultSet resultSetStations = pstStations.executeQuery();   
	            while (resultSetStations.next()) {
	                
	                int id = Integer.parseInt(resultSetStations.getString("id"));
	                int id_real_pos = Integer.parseInt(resultSetStations.getString("id_real_pos"));
	                String name = resultSetStations.getString("name");
	                
	                Sensor sensorStation = null;
	                for(Sensor sSensor : stationSensors){
	                    if(sSensor.getId() == id_real_pos) {
	                        sensorStation = sSensor;
	                        break;
	                    }
	                }
	                
	                stations.add(new Station(id,name,sensorStation));
	            }
	            
	            for(Sensor sensor : activeSensors) {
	            	double minDist = Float.MAX_VALUE;
	            	Station closestStation = null;
	            	// TODO : sort station by dist
	            	int handlingCapacity = sensor.getHandled();
	            	while(handlingCapacity < sensor.getIntensity()) {
	            		int indexStat = 0;
	            		for(Station stat : stations) {
		            		if(Math.hypot(sensor.getX()-stat.getSensor().getX(), sensor.getY()-stat.getSensor().getY()) < minDist) {
		            			minDist = Math.hypot(sensor.getX()-stat.getSensor().getX(), sensor.getY()-stat.getSensor().getY());
		            			closestStation = stat;
		            		}
		            		indexStat ++;
		            	}
	            		//remove station being treated from the list, so it isn't taken into account again on a hypothetical second loop
	            		stations.remove(indexStat);
	            		
	            		// handle closestStation fire_engine dispatch
	            		List<FireEngine> fireEngines = closestStation.getFireEngines();
        				for(FireEngine fireEngine : fireEngines) {
            				if(fireEngine.getRank() > sensor.getIntensity() - handlingCapacity) {
            					// TODO : Create intervention
            					String makeIntervention = String.format("insert into intervention(id_fire_engine, id_fire) values(%d, %d)", fireEngine.getId(), sensor.getId());
            					PreparedStatement pstMakeIntervention = EmergencyManagerConnection.prepareStatement(makeIntervention);
            		            pstMakeIntervention.executeUpdate();
            					handlingCapacity = fireEngine.getRank();
            					// TODO : update handled to handlingCapaticity
            					String updateHandled = String.format("update fire set handled = %d where id = %d", handlingCapacity, sensor.getId());
        						PreparedStatement pstupdateHandled = EmergencyManagerConnection.prepareStatement(updateHandled);
        						pstupdateHandled.executeUpdate();
            					break;
            				}
            			}
        				// if no fire_engine was sufficient on it's own, send every available one until the fire is solved
        				if(handlingCapacity < sensor.getIntensity()) {
        					for(FireEngine fireEngine : fireEngines) {
        						// Create intervention
        						String makeIntervention = String.format("insert into intervention(id_fire_engine, id_fire) values(%d, %d)", fireEngine.getId(), sensor.getId());
        						PreparedStatement pstMakeIntervention = EmergencyManagerConnection.prepareStatement(makeIntervention);
            		            pstMakeIntervention.executeUpdate();
        						handlingCapacity += fireEngine.getRank();
            					// Update handled to handlingCapaticity
        						String updateHandled = String.format("update fire set handled = %d where id = %d", handlingCapacity, sensor.getId());
        						PreparedStatement pstupdateHandled = EmergencyManagerConnection.prepareStatement(updateHandled);
        						pstupdateHandled.executeUpdate();
        						if(handlingCapacity > sensor.getIntensity()) {
        							break;
        						}
        					}
        				}
	            	}
		        }
	        }	
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// TODO : handle intervention suppression, and truck movment in simulator
	}
}
