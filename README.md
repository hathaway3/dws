# DriveWire Java Server
## Required For build
 1. Java JDK V1.8 or later. 
 2. Apache Maven

##  Building DriveWire Java Server
Run Maven from root of **dws** folder

    mvn clean package

**Note:** config.xml must be modified to support your configuration before you run DriveWire Java Server.  See help.xml file for more information on how to configure the server.xml.
## Running DriveWire Java Server
The compiled and ziped DriveWire server will be located under dws/target after the compile is complete.
```
dws/
├── src/
│   └── ... (source files and resources)
├── target/
│   └── DriveWire-4.3.6.zip
└── pom.xml
```
Copy the DriveWire<version>.zip file to a location outside of the dws folder, like your home directory.
Unzip the DriveWire<version>.zip file and run the correct file to launch DriveWire
Windows:

    dws.bat

Linux/OSX: 

    chmod +x dws.sh
    ./dws.sh

## See DriveWire Version 4 website for more details
https://www.hat3.net/home/java-drivewire-server
