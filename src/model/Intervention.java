package model;

import java.util.ArrayList;
import java.util.List;

public class Intervention {

	private int id;
	private Sensor sensor;
	private String start_ts;
	private List<FireEngine> fireEngines;
	
	public Intervention(int id, Sensor sensor, String start_ts) {
		super();
		this.id = id;
		this.sensor = sensor;
		this.start_ts = start_ts;
		this.fireEngines = new ArrayList<>();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Sensor getSensor() {
		return sensor;
	}
	
	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}
	
	public String getStart_ts() {
		return start_ts;
	}

	public void setStart_ts(String start_ts) {
		this.start_ts = start_ts;
	}

	public List<FireEngine> getFireEngines() {
		return fireEngines;
	}
	
	public void addFireEngine(FireEngine fireEngine) {
		this.fireEngines.add(fireEngine);
	}

	@Override
	public String toString() {
		return "Intervention [id=" + id + ", sensor=" + sensor + ", start_ts=" + start_ts + ", fireEngines="
				+ fireEngines + "]";
	}

}
