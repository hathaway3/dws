package com.groupunix.drivewireserver.dwprotocolhandler;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;

public class DWSerialReader implements SerialPortDataListener {
	private ArrayBlockingQueue<Byte> queue;
	private InputStream in;
	private boolean wanttodie = false;

	public DWSerialReader(InputStream in, ArrayBlockingQueue<Byte> q) {
		this.queue = q;
		this.in = in;
	}

	@Override
	public int getListeningEvents() {
		return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
			return;
		}

		int data;
		try {
			while (!wanttodie && in.available() > 0 && (data = in.read()) > -1) {
				queue.add((byte) data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		this.wanttodie = true;

	}

}
