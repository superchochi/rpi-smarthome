package smarthome.arduino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import smarthome.arduino.utils.Logger;

public class App {

	private static final String TAG = "ArduinoTestApp";

	public static void main(String[] args) {
		System.out.println("Hello World!");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			while (true) {
				String line = br.readLine();
				if (line == null) {
					Logger.debug(TAG, "End of stream!");
					break;
				}
				if (line.equalsIgnoreCase("exit")) {
					Logger.debug(TAG, "Exit!");
					break;
				}
				System.out.println("Line read: " + line);
			}
		} catch (IOException e) {
			Logger.error(TAG, "Error reading line!", e);
		}
	}

}
