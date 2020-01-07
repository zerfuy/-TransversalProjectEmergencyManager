package launcher;

import controller.EmergencyManager;

public class EmergencyManagerLauncher {
	public static void main(String[] args) {
		EmergencyManager emergencyManager = new EmergencyManager();
		emergencyManager.start();
	}
}
