package model;

import java.util.ArrayList;
import java.util.List;

public class Sensor implements Comparable<Sensor> {

	private int id;
	private double x;
	private double y;
	private int intensity;
	private int handled;

	public Sensor(int id, double x, double y, int intensity, int handled) {
		super();
		this.id = id;
		this.x = x;
		this.y = y;
		this.intensity = intensity;
		this.handled = handled;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public int getIntensity() {
		return intensity;
	}

	public void setIntensity(int intensity) {
		this.intensity = intensity;
	}

	public int getHandled() {
		return handled;
	}

	public void setHandled(int handled) {
		this.handled = handled;
	}
	
	public void increaseHandled(int handledIncrease) {
		this.handled += handledIncrease;
	}

	public List<Station> orderStation(List<Station> stations) {

		List<Station> orderedStations = new ArrayList<>();
		List<Station> unorderedStations = new ArrayList<>();
		unorderedStations.addAll(stations);

		double minDist;
		Station s = null;

		while (unorderedStations.size() > 0) {
			minDist = Float.MAX_VALUE;
			for (Station station : unorderedStations) {
				if (Math.hypot(this.x - station.getSensor().getX(), this.y - station.getSensor().getY()) < minDist) {
					minDist = Math.hypot(this.x - station.getSensor().getX(), this.y - station.getSensor().getY());
					s = station;
				}
			}
			unorderedStations.remove(s);
			orderedStations.add(s);
		}

		return orderedStations;
	}

	@Override
	public String toString() {
		return "Sensor [id=" + id + ", x=" + x + ", y=" + y + ", intensity=" + intensity + ", handled=" + handled + "]";
	}

	@Override
	public int compareTo(Sensor arg0) {
		return arg0.getIntensity() - arg0.getHandled() - (this.intensity - this.handled);
	}

}
