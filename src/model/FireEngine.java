package model;

public class FireEngine implements Comparable<FireEngine> {

	private int id;
	private double x;
	private double y;
	private int rank;
	private boolean busy;

	public FireEngine(int id, double x, double y, int rank, boolean busy) {
		super();
		this.id = id;
		this.x = x;
		this.y = y;
		this.rank = rank;
		this.busy = busy;
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

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public boolean getBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}

	@Override
	public String toString() {
		return "FireEngine [id=" + id + ", x=" + x + ", y=" + y + ", rank=" + rank + ", busy=" + busy + "]";
	}

	@Override
	public int compareTo(FireEngine o) {
		return this.rank - o.getRank();
	}

}
