package model;

public class Intervention {

	private int id;
	private Sensor sensor;
	private String start_ts;
	private FireEngine fireEngine;
	private boolean returning;
	
	public Intervention(Sensor sensor, FireEngine fireEngine, String start_ts, boolean returning) {
		super();
		id = -1;
		this.sensor = sensor;
		this.start_ts = start_ts;
		this.fireEngine = fireEngine;
		this.returning = returning;
	}
	
	public Intervention(int id, Sensor sensor, FireEngine fireEngine, String start_ts, boolean returning) {
		super();
		this.id = id;
		this.sensor = sensor;
		this.start_ts = start_ts;
		this.fireEngine = fireEngine;
		this.returning = returning;
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

	public boolean isReturning() {
		return returning;
	}

	public void setReturning(boolean returning) {
		this.returning = returning;
	}

	@Override
	public String toString() {
		return "Intervention [id=" + id + " ,sensor=" + sensor + ", start_ts=" + start_ts + ", fireEngine=" + fireEngine.getId() + "]";
	}

}
