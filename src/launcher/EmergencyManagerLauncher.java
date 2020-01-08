package launcher;

import java.util.concurrent.TimeUnit;

import controller.EmergencyManager;

public class EmergencyManagerLauncher {
	public static void main(String[] args) {
		
		EmergencyManager emergencyManager = new EmergencyManager();
		int runs = 0;
		
		while(true) {
			System.out.println("\nRunning routine (" + runs + ")");
			emergencyManager.run();
			try {
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			runs++;
		}
	}
}
