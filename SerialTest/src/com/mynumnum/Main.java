/**
 * 
 */
package com.mynumnum;

import com.fazecast.jSerialComm.SerialPort;

/**
 * @author jimhathaway
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (SerialPort s : SerialPort.getCommPorts()) {
			System.out.println("Availible port: " + s.getDescriptivePortName());
		}

	}

}
