package model;

public class Sensor {

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

	@Override
	public String toString() {
		return "Sensor [id=" + id + ", x=" + x + ", y=" + y + ", intensity=" + intensity + ", handled=" + handled + "]";
	}
	
}
