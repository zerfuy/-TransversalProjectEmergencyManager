package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.PostgreSQLJDBC;
import model.FireEngine;
import model.Intervention;
import model.Sensor;
import model.Station;

public class EmergencyManager {

	private Connection EmergencyManagerConnection;
	private int debug;
	private List<Sensor> activeSensors;
	private List<Sensor> stationSensors;
	private List<Sensor> unhandledActiveFires;
	private List<Station> stations;

	public EmergencyManager() {

		debug = 0;
		System.out.println("EmergencyManager initialized successfully");
	}

	public int start() {

		// Opening database connection
		EmergencyManagerConnection = new PostgreSQLJDBC("jdbc:postgresql://manny.db.elephantsql.com:5432/", "ngcbqvhq",
				"Ppjleq3n6HQF5qPheDze2QFzG4LHxTAf").getConnection();

		// Getting Sensors from db
		activeSensors = new ArrayList<>();
		stationSensors = new ArrayList<>();
		this.getSensorsFromDB();

		// Checking for unhandled fires
		System.out.printf("Checking for unhandled fires... ");
		unhandledActiveFires = new ArrayList<>();
		for (Sensor sensor : activeSensors) {
			if (sensor.getHandled() < sensor.getIntensity()) {
				unhandledActiveFires.add(sensor);
			}
		}

		if (unhandledActiveFires.size() > 0) {

			// Handling fires
			this.handleFires();

		} else {
			
			System.out.println("no unhandled fire detected");
			
		}

		try {
			System.out.printf("Closing conection with db... ");
			EmergencyManagerConnection.close();
			System.out.println("Closed");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 1;
	}

	private void getSensorsFromDB() {

		try {
			// Get Active Sensors
			String getActiveSensorsQuery = "select f.id, f.intensity, f.handled, p.real_x, p.real_y from fire f join real_pos p on f.id_real_pos = p.id where intensity > 0";
			PreparedStatement pstActiveSensors = EmergencyManagerConnection.prepareStatement(getActiveSensorsQuery);
			ResultSet resultSetActiveSensors = pstActiveSensors.executeQuery();

			// Get Station Sensors
			String getStationSensorsQuery = "select f.id, f.intensity, f.handled, p.real_x, p.real_y from fire f join real_pos p on f.id_real_pos = p.id where p.id in (select id_real_pos from station)";
			PreparedStatement pstStationSensors = EmergencyManagerConnection.prepareStatement(getStationSensorsQuery);
			ResultSet resultSetStationSensors = pstStationSensors.executeQuery();

			while (resultSetActiveSensors.next()) {

				int id = Integer.parseInt(resultSetActiveSensors.getString("id"));
				double x = Double.parseDouble(resultSetActiveSensors.getString("real_x"));
				double y = Double.parseDouble(resultSetActiveSensors.getString("real_y"));
				int intensity = Integer.parseInt(resultSetActiveSensors.getString("intensity"));
				int handled = Integer.parseInt(resultSetActiveSensors.getString("handled"));

				activeSensors.add(new Sensor(id, x, y, intensity, handled));
			}

			while (resultSetStationSensors.next()) {

				int id = Integer.parseInt(resultSetStationSensors.getString("id"));
				double x = Double.parseDouble(resultSetStationSensors.getString("real_x"));
				double y = Double.parseDouble(resultSetStationSensors.getString("real_y"));
				int intensity = Integer.parseInt(resultSetStationSensors.getString("intensity"));
				int handled = Integer.parseInt(resultSetStationSensors.getString("handled"));

				stationSensors.add(new Sensor(id, x, y, intensity, handled));
			}

			System.out.println("Got all active sensors (" + activeSensors.size() + ")");
			if (debug > 0) {
				for (Sensor sensor : activeSensors) {
					System.out.println(sensor);
				}
			}

			System.out.println("Got all station sensors (" + stationSensors.size() + ")");
			if (debug > 0) {
				for (Sensor sensor : stationSensors) {
					System.out.println(sensor);
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void getFireStationsFromDB() {

		try {
			// Get Fire Stations
			String getStations = "select s.id, s.id_real_pos, s.name from station s where s.id in (select fe.id_station from fire_engine fe where fe.busy = false)";
			PreparedStatement pstStations = EmergencyManagerConnection.prepareStatement(getStations);
			ResultSet resultSetStations = pstStations.executeQuery();

			while (resultSetStations.next()) {

				int id = Integer.parseInt(resultSetStations.getString("id"));
				int id_real_pos = Integer.parseInt(resultSetStations.getString("id_real_pos"));
				String name = resultSetStations.getString("name");

				Sensor sensorStation = null;
				for (Sensor sSensor : stationSensors) {
					if (sSensor.getId() == id_real_pos) {
						sensorStation = sSensor;
						break;
					}
				}

				Station station = new Station(id, name, sensorStation);
				// Get Fire Engines
				String getFireEngines = "select fe.id, fe.x_pos, fe.y_pos, fe.rank, fe.busy from fire_engine fe where fe.busy = false and fe.id_station = "
						+ id;
				PreparedStatement pstFireEngines = EmergencyManagerConnection.prepareStatement(getFireEngines);
				ResultSet resultSetFireEngines = pstFireEngines.executeQuery();

				while (resultSetFireEngines.next()) {

					int idt = Integer.parseInt(resultSetFireEngines.getString("id"));
					double x = Double.parseDouble(resultSetFireEngines.getString("x_pos"));
					double y = Double.parseDouble(resultSetFireEngines.getString("y_pos"));
					int rank = Integer.parseInt(resultSetFireEngines.getString("rank"));
					boolean busy = Boolean.parseBoolean(resultSetFireEngines.getString("busy"));

					station.addFireEngine(new FireEngine(idt, x, y, rank, busy));
				}
				
				station.sortFireEngine();

				stations.add(station);

			}

			System.out.println("Got all stations (" + stations.size() + ")");
			if (debug > 0) {
				for (Station station : stations) {
					System.out.println(station);
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void handleFires() {
		
		System.out.println("at least one unhandled fire detected");

		// Ordering fires to manage the most important ones first
		Collections.sort(unhandledActiveFires);

		// Getting stations with trucks from db
		stations = new ArrayList<>();
		this.getFireStationsFromDB();

		// Assigning trucks to a fire
		for (Sensor sensor : unhandledActiveFires) {
			
			// Ordering stations from closest to farthest
			List<Station> orderedStations = sensor.orderStation(stations);

			System.out.printf("Sensor: " + sensor.getId() + " - fire handle: " + sensor.getHandled() + "/"
					+ sensor.getIntensity() + " (" + (sensor.getIntensity() - sensor.getHandled()) + ")"
					+ "\t- stations ordered: ");
			for (Station s : orderedStations) {
				System.out.printf(s.getId() + " ( ");
				for (FireEngine fe : s.getFireEngines()) {
					System.out.printf(fe.getRank() + " ");
				}
				System.out.printf(") - ");
			}
			System.out.println();
			
			while (((sensor.getIntensity() - sensor.getHandled()) > 0) && orderedStations.size()>0) {
				
				Station closestStation = orderedStations.get(0);
				boolean done = false;
				FireEngine fireEngine = null;
				int maxStation = 0;
				
				for(FireEngine fe : closestStation.getFireEngines()) {
					
					if(fe.getRank() == sensor.getIntensity() - sensor.getHandled()) {
						System.out.println("Assigning truck with level " + fe.getRank() + " from station " + closestStation.getId() + " to fire with unhandled intensity of " + (sensor.getIntensity()-sensor.getHandled()));
						sensor.increaseHandled(fe.getRank());
						done = true;
						fireEngine = fe;
						break;
					}
					
					if(fe.getRank() == sensor.getIntensity() - sensor.getHandled() + 1) {
						fireEngine = fe;
					}
					
					if(fe.getRank() == sensor.getIntensity() - sensor.getHandled() + 2 && fireEngine == null) {
						fireEngine = fe;
					}

					maxStation += fe.getRank();
					
				}
				
				if (done) {
					
					closestStation.removeTruck(fireEngine);
					Station globalStation = stations.get(stations.indexOf(closestStation));
					globalStation.removeTruck(fireEngine);
					if (globalStation.getFireEngines().size() <= 0) stations.remove(globalStation);
					
				} else if(fireEngine != null) {
					
					System.out.println("Assigning truck with level " + fireEngine.getRank() + " (only one or two above so it's close enough) from station " + closestStation.getId() + " to fire with unhandled intensity of " + (sensor.getIntensity()-sensor.getHandled()));
					sensor.increaseHandled(fireEngine.getRank());
					
					closestStation.removeTruck(fireEngine);
					Station globalStation = stations.get(stations.indexOf(closestStation));
					globalStation.removeTruck(fireEngine);
					if (globalStation.getFireEngines().size() <= 0) stations.remove(globalStation);
					
				} else {	
					
					if(maxStation < (sensor.getIntensity() - sensor.getHandled())) {
						System.out.println("Assigning all trucks from the station " + closestStation.getId() + " as they'll anyway be not enough");
						
						for(FireEngine fe : closestStation.getFireEngines()) {
							sensor.increaseHandled(fe.getRank());
						}
						
						orderedStations.remove(closestStation);
						stations.remove(closestStation);
						
					} else {
						fireEngine = closestStation.getFireEngines().get(0);
						System.out.println("Assigning smallest trucks (" + fireEngine.getRank() + ") from the station " + closestStation.getId());
						sensor.increaseHandled(fireEngine.getRank());
						closestStation.removeTruck(fireEngine);
						stations.get(stations.indexOf(closestStation)).removeTruck(fireEngine);
					}
					
				}
				
			}
			
			System.out.println("Sensor: " + sensor.getId() + " - fire handle: " + sensor.getHandled() + "/"
					+ sensor.getIntensity() + " (" + (sensor.getIntensity() - sensor.getHandled()) + ")");
		}
	}
}
