# DriveWire Java Server
## Required For build
 1. Java JDK V1.8 or later. 
 2. Apache Maven

##  Building DriveWire Java Server
Run Maven from the root of **dws** folder

    mvn clean package

After the packaging is complete, you should see the message 'BUILD SUCCESS' from Maven.
```
[INFO] --- maven-assembly-plugin:2.2-beta-5:single (zip-assembly) @ DriveWire ---
[INFO] Reading assembly descriptor: src/main/assembly/zip.xml
[INFO] Building zip: /Users/jimmiehathaway/dws/target/DriveWire-4.3.6.zip
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  4.183 s
[INFO] Finished at: 2023-12-31T10:24:40-06:00
[INFO] ------------------------------------------------------------------------
```
**Note:** config.xml must be modified to support your configuration before you run DriveWire Java Server.  See the help.xml file for more information on how to configure the server.xml.
## Running DriveWire Java Server
The compiled and zipped DriveWire server will be located under dws/target after completing the compile.
```
dws/
├── src/
│   └── ... (source files and resources)
├── target/
│   └── DriveWire-4.3.6.zip
└── pom.xml
```
Copy the DriveWire-x.x.x.zip file to a location outside of the dws folder, like your home directory.
Unzip the DriveWire-x.x.x.zip file and run the correct file to launch DriveWire
Windows:

    dws.bat

Linux/OSX: 

    chmod +x dws.sh
    ./dws.sh

## Typical DriveWire server output
If all goes well, you should see a detailed log of DriveWire activities like this.
```
dietpi@DietPi:~/dws/target/DriveWire-4.3.6 $ ./dws.sh 
This script must be run as root. Requesting sudo access...
31 Dec 2023 10:20:08 INFO  [dwserver-1    ] DriveWire Server com.groupunix.drivewireserver.Version@6e06451e starting
31 Dec 2023 10:20:08 INFO  [dwserver-1    ] UI listening on port 6800
10:20:08: [dwserver-1    ] Auto save of configuration is enabled
10:20:08: [dwserver-1    ] Starting handler #0: DWProtocolHandler
10:20:08: [dwproto-0-12  ] init /dev/ttyS0 for handler #0 (logging bytes: false  xorinput: false)
10:20:08: [dwproto-0-12  ] attempting to open device '/dev/ttyS0'
RXTX Warning:  Removing stale lock file. /var/lock/LCK..ttyS0
10:20:09: [dwproto-0-12  ] setting port params to 115200 8:0:1
10:20:09: [dwproto-0-12  ] opened serial device /dev/ttyS0
10:20:09: [dwproto-0-12  ] disk drives init for handler #0
10:20:09: [dwproto-0-12  ] Resetting all virtual serial ports - part 1, close all sockets
10:20:09: [dwproto-0-12  ] Resetting all virtual serial ports - part 2, init all ports
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 1 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 2 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 3 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 4 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 5 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 6 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 7 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 8 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 9 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 10 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 11 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 12 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 13 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 14 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 15 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 16 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 17 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 18 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 19 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 20 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 21 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 22 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 23 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 24 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 25 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 26 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 27 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 28 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 29 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 30 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 31 in handler #0
10:20:09: [dwproto-0-12  ] New DWVSerialPort for port 0 in handler #0
10:20:09: [dwproto-0-12  ] dwprinter init for handler #0
10:20:09: [dwproto-0-12  ] init for handler #0
10:20:09: [dwproto-0-12  ] handler #0 is ready
10:20:09: [dwserver-1    ] ready...
10:20:09: [dwserver-1    ] Serial ports:
10:20:09: [dskwriter-14  ] started, write interval is 15000
10:20:09: [dwserver-1    ] Searching for serial ports...


10:20:09: [dwserver-1    ] Adding serial port /dev/ttyAMA0 to list of available ports
10:20:09: [dwserver-1    ] /dev/ttyAMA0
```
## See DriveWire Version 4 website for more details
https://www.hat3.net/home/java-drivewire-server
