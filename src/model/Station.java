package model;

import java.util.ArrayList;
import java.util.List;

public class Station {

	private int id;
	private String name;
	private Sensor sensor;
	private List<FireEngine> fireEngines;
	
	public Station(int id, String name, Sensor sensor) {
		super();
		this.id = id;
		this.name = name;
		this.sensor = sensor;
		this.fireEngines = new ArrayList<>();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Sensor getSensor() {
		return sensor;
	}
	
	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}
	
	public List<FireEngine> getFireEngines() {
		return fireEngines;
	}
	
	public void addFireEngine(FireEngine fireEngine) {
		this.fireEngines.add(fireEngine);
	}

	@Override
	public String toString() {
		return "Station [id=" + id + ", name=" + name + ", sensor=" + sensor + ", fireEngines=" + fireEngines + "]";
	}
	
}
