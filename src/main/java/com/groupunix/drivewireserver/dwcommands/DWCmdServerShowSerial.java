package com.groupunix.drivewireserver.dwcommands;

import com.fazecast.jSerialComm.SerialPort;
import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdServerShowSerial extends DWCommand {

	DWCmdServerShowSerial(DWProtocol dwProto, DWCommand parent) {
		setParentCmd(parent);
	}

	public String getCommand() {
		return "serial";
	}

	public String getShortHelp() {
		return "Show serial device information";
	}

	public String getUsage() {
		return "dw server show serial";
	}

	public DWCommandResponse parse(String cmdline) {
		StringBuilder text = new StringBuilder();

		text.append("Server serial devices:\r\n\r\n");

		for (SerialPort serialPort : SerialPort.getCommPorts()) {
			try {
				text.append(serialPort.getSystemPortName()).append("  ");

				boolean wasOpen = serialPort.isOpen();
				boolean opened = false;
				if (!wasOpen) {
					opened = serialPort.openPort();
				}

				if (wasOpen || opened) {
					text.append(serialPort.getBaudRate()).append(" bps  ");
					text.append(serialPort.getNumDataBits());

					switch (serialPort.getParity()) {
						case SerialPort.NO_PARITY:
							text.append("N");
							break;

						case SerialPort.EVEN_PARITY:
							text.append("E");
							break;

						case SerialPort.MARK_PARITY:
							text.append("M");
							break;

						case SerialPort.ODD_PARITY:
							text.append("O");
							break;

						case SerialPort.SPACE_PARITY:
							text.append("S");
							break;
					}

					text.append(serialPort.getNumStopBits());

					int flow = serialPort.getFlowControlSettings(); // Corrected method name
					if (flow == SerialPort.FLOW_CONTROL_DISABLED)
						text.append("  No flow control  ");
					else {
						text.append("  ");

						if ((flow & SerialPort.FLOW_CONTROL_RTS_ENABLED) != 0)
							text.append("RTS ");
						if ((flow & SerialPort.FLOW_CONTROL_CTS_ENABLED) != 0)
							text.append("CTS ");
						if ((flow & SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED) != 0)
							text.append("XOn ");
						if ((flow & SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED) != 0)
							text.append("XOff ");
					}

					text.append(" CD:").append(yn(serialPort.getDCD()));
					text.append(" CTS:").append(yn(serialPort.getCTS()));
					text.append(" DSR:").append(yn(serialPort.getDSR()));
					text.append(" DTR:").append(yn(serialPort.getDTR()));
					text.append(" RTS:").append(yn(serialPort.getRTS()));

					text.append("\r\n");

					if (opened)
						serialPort.closePort();
				} else {
					text.append("Not available or in use\r\n");
				}
			} catch (Exception e) {
				return (new DWCommandResponse(false, DWDefs.RC_SERVER_IO_EXCEPTION,
						"While gathering serial port info: " + e.getMessage()));
			}
		}
		return (new DWCommandResponse(text.toString()));
	}

	private String yn(boolean cd) {
		if (cd)
			return "Y";

		return "n";
	}

	public boolean validate(String cmdline) {
		return (true);
	}
}
