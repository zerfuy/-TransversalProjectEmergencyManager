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
	private List<Intervention> interventions;
	private List<Intervention> newInterventions;
	private int fireHandled;

	public EmergencyManager() {

		debug = 1;
		System.out.println("EmergencyManager initialized successfully\n");
	}

	public int run() {

		// Opening database connection
		EmergencyManagerConnection = new PostgreSQLJDBC("jdbc:postgresql://manny.db.elephantsql.com:5432/", "ngcbqvhq",
				"Ppjleq3n6HQF5qPheDze2QFzG4LHxTAf").getConnection();

		// Getting Sensors from db
		this.getSensors();
		
		// Getting Intervention from db
		this.updateInterventions();

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
			
			// Updating fires
			this.addInterventions();
			
			System.out.println("Fire handled " + fireHandled + "/" + unhandledActiveFires.size());

		} else {
			
			System.out.println("no unhandled fire detected");
			
		}
		
		System.out.printf("\nRun done... \n");

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
	
	private void getSensors() {
		
		activeSensors = new ArrayList<>();
		stationSensors = new ArrayList<>();

		try {
			// Get Active Sensors
			String getActiveSensorsQuery = "select f.id, f.intensity, f.handled, p.real_x, p.real_y from fire f join real_pos p on f.id_real_pos = p.id where intensity > 0 or f.id in (select id_fire from intervention)";
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
					System.out.println("\t" + sensor);
				}
			}
			System.out.println();

			System.out.println("Got all station sensors (" + stationSensors.size() + ")");
			if (debug > 0) {
				for (Sensor sensor : stationSensors) {
					System.out.println("\t" + sensor);
				}
			}
			System.out.println();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void updateInterventions() {
		
		interventions = new ArrayList<>();
		List<FireEngine> intervention_fire_engine = new ArrayList<>();
		
		try {
			
			// Get Intervention fire engine
			String getFireEnginesQuery = "select fe.id, fe.x_pos, fe.y_pos, fe.rank, fe.busy from fire_engine fe";
			PreparedStatement pstFireEngines = EmergencyManagerConnection.prepareStatement(getFireEnginesQuery);
			ResultSet resultSetFireEngines = pstFireEngines.executeQuery();
			
			// Get Intervention
			String getInterventionsQuery = "select i.id, i.id_fire, i.id_fire_engine, i.start_ts from intervention i where i.id_fire in (select id from fire)";
			PreparedStatement pstInterventions = EmergencyManagerConnection.prepareStatement(getInterventionsQuery);
			ResultSet resultSetInterventions = pstInterventions.executeQuery();
			
			while (resultSetFireEngines.next()) {
				
				int id = Integer.parseInt(resultSetFireEngines.getString("id"));
				double x = Double.parseDouble(resultSetFireEngines.getString("x_pos"));
				double y = Double.parseDouble(resultSetFireEngines.getString("y_pos"));
				int rank = Integer.parseInt(resultSetFireEngines.getString("rank"));
				boolean busy = Boolean.parseBoolean(resultSetFireEngines.getString("busy"));
	
				intervention_fire_engine.add(new FireEngine(id, x, y, rank, busy));
			}
	
			while (resultSetInterventions.next()) {
	
				int id = Integer.parseInt(resultSetInterventions.getString("id"));
				int id_sensor = Integer.parseInt(resultSetInterventions.getString("id_fire"));
				int id_fire_engine = Integer.parseInt(resultSetInterventions.getString("id_fire_engine"));
				String start_ts = resultSetInterventions.getString("start_ts");
				
				Sensor sensor = null;
				for(Sensor s : activeSensors) {
					if(s.getId() == id_sensor) {
						sensor = s;
						break;
					}
				}
				
				FireEngine fireEngine = null;
				for(FireEngine fe : intervention_fire_engine) {
					if(fe.getId() == id_fire_engine) {
						fireEngine = fe;
						break;
					}
				}
				
				interventions.add(new Intervention(id, sensor, fireEngine, start_ts));
			}
	
			
	
			System.out.println("Got all interventions (" + interventions.size() + ")");
			if (debug > 0) {
				for (Intervention intervention : interventions) {
					System.out.println("\t" + intervention);
				}
			}
			System.out.println();
			
			// Deleting handled interventions
			System.out.println("Checking for handled interventions...");
			for(Intervention intervention : interventions) {
				if(intervention.getSensor().getIntensity() <= 0) {
					
					try {
						
						// Deleting intervention
						System.out.println("\tDeleteting " + intervention);
						String deletingInterventionQuery = "delete from intervention where id = " + intervention.getId();
						PreparedStatement pstIntervention = EmergencyManagerConnection.prepareStatement(deletingInterventionQuery);
						pstIntervention.executeUpdate();
						
						// Freeing trucks
						System.out.println("\tFreeing " + intervention.getFireEngine());
						String freeingFireEngineQuery = "update fire_engine set busy = false where id = " + intervention.getFireEngine().getId();
						PreparedStatement pstFireEngine = EmergencyManagerConnection.prepareStatement(freeingFireEngineQuery);
						pstFireEngine.executeUpdate();
						
						// Updating handling value
						System.out.println("\tUpdating handling value " + intervention.getSensor());
						String updatingFireQuery = "update fire set handled = 0 where id = " + intervention.getSensor().getId();
						PreparedStatement pstFire = EmergencyManagerConnection.prepareStatement(updatingFireQuery);
						pstFire.executeUpdate();
						
						
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
	
	}
	
	private void getFireStations() {

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
			System.out.println();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void handleFires() {
		
		System.out.println("at least one unhandled fire detected");
		newInterventions = new ArrayList<>();

		// Ordering fires to manage the most important ones first
		Collections.sort(unhandledActiveFires);

		// Getting stations with trucks from db
		stations = new ArrayList<>();
		this.getFireStations();

		
		System.out.printf("\n/////////////////////////////\n");
		System.out.println("Assigning trucks to fires");
		System.out.printf("/////////////////////////////\n\n");
		
		fireHandled = 0;
		
		// Assigning trucks to a fire
		for (Sensor sensor : unhandledActiveFires) {
			
			// Ordering stations from closest to farthest
			List<Station> orderedStations = sensor.orderStation(stations);

			System.out.printf("Sensor: " + sensor.getId() + " - fire handle: " + sensor.getHandled() + "/"
					+ sensor.getIntensity() + " (" + (sensor.getIntensity() - sensor.getHandled()) + ")"
					+ " - stations ordered: ");
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
					
					if(fe.getRank() >= sensor.getIntensity() - sensor.getHandled()) {
						done = true;
						fireEngine = fe;
						break;
					}

					maxStation += fe.getRank();
					
				}
				
				if (done) {
					
					// Assigning
					System.out.println("\tAssigning truck with level " + fireEngine.getRank() + " from station " + closestStation.getId() + " to fire with unhandled intensity of " + (sensor.getIntensity()-sensor.getHandled()));
					newInterventions.add(new Intervention(sensor, fireEngine, "Test"));
					sensor.increaseHandled(fireEngine.getRank());	
					
					closestStation.removeTruck(fireEngine);
					if(closestStation.getFireEngines().size()>0) {
						stations.get(stations.indexOf(closestStation)).removeTruck(fireEngine);
					} else {
						stations.remove(closestStation);
					}
					
				} else {	
					
					if(maxStation < (sensor.getIntensity() - sensor.getHandled())) {
						System.out.println("\tAssigning all trucks from the station " + closestStation.getId() + " as they'll anyway be not enough");
						
						for(FireEngine fe : closestStation.getFireEngines()) {
							// Assigning
							newInterventions.add(new Intervention(sensor, fe, "Test"));
							sensor.increaseHandled(fe.getRank());
						}
						
						orderedStations.remove(closestStation);
						stations.remove(closestStation);
						
					} else {
						
						fireEngine = closestStation.getFireEngines().get(closestStation.getFireEngines().size()-1);
						
						// Assigning
						System.out.println("\tAssigning bigger truck (" + fireEngine.getRank() + ") from the station " + closestStation.getId());
						newInterventions.add(new Intervention(sensor, fireEngine, "Test"));
						sensor.increaseHandled(fireEngine.getRank());
						
						closestStation.removeTruck(fireEngine);
						if(closestStation.getFireEngines().size()>0) {
							stations.get(stations.indexOf(closestStation)).removeTruck(fireEngine);
						} else {
							stations.remove(closestStation);
						}
						
					}
					
				}
				
			}
			
			System.out.println("\tSensor " + sensor.getId() + " - fire handle: " + sensor.getHandled() + "/"
					+ sensor.getIntensity() + " (" + (sensor.getIntensity() - sensor.getHandled()) + ")");
			
			if(sensor.getIntensity() - sensor.getHandled() <= 0) {
				fireHandled ++;
				System.out.printf("\tSensor " + sensor.getId() + " handled\n\n");
			} else {
				System.out.printf("Sensor " + sensor.getId() + " not handled \nOut of fire engines \n\n");
				break;
			}
			
		}
	}
	
	private void addInterventions() {
		
		for(Intervention intervention : newInterventions) {
			
			try {
				
				// Adding new intervention
				String addInterventionQuery = "insert into intervention (id_fire_engine, id_fire, start_ts) values ("+ intervention.getFireEngine().getId() +", "+ intervention.getSensor().getId() +", '"+ intervention.getStart_ts() +"')";
				PreparedStatement pstInterventions = EmergencyManagerConnection.prepareStatement(addInterventionQuery);
				pstInterventions.executeUpdate();
				System.out.println("Added : " + intervention);
				
				// Updating fire engine status
				String updateFireEngineQuery = "update fire_engine set busy = true where id = " + intervention.getFireEngine().getId();
				PreparedStatement pstFireEngines = EmergencyManagerConnection.prepareStatement(updateFireEngineQuery);
				pstFireEngines.executeUpdate();
				System.out.println("Fire engine status updated");
				
				// Updating fire handle
				String updateFireQuery = "update fire set handled = " + intervention.getSensor().getHandled() + " where id = " + intervention.getSensor().getId();
				PreparedStatement pstFires = EmergencyManagerConnection.prepareStatement(updateFireQuery);
				pstFires.executeUpdate();
				System.out.println("Fire handled value updated");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println();

		}
		
	}
}
