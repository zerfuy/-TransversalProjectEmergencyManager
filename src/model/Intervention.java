package model;

public class Intervention {

	private int id;
	private Sensor sensor;
	private String start_ts;
	private FireEngine fireEngine;
	
	public Intervention(Sensor sensor, FireEngine fireEngine, String start_ts) {
		super();
		id = -1;
		this.sensor = sensor;
		this.start_ts = start_ts;
		this.fireEngine = fireEngine;
	}
	
	public Intervention(int id, Sensor sensor, FireEngine fireEngine, String start_ts) {
		super();
		this.id = id;
		this.sensor = sensor;
		this.start_ts = start_ts;
		this.fireEngine = fireEngine;
	}
	
	public int getId() {
		return this.id;
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

	public FireEngine getFireEngine() {
		return fireEngine;
	}

	public void setFireEngine(FireEngine fireEngine) {
		this.fireEngine = fireEngine;
	}

	@Override
	public String toString() {
		return "Intervention [id=" + id + " ,sensor=" + sensor + ", start_ts=" + start_ts + ", fireEngine=" + fireEngine.getId() + "]";
	}

}
