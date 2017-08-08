# Tomar 

A [Spring Boot](https://projects.spring.io/spring-boot/) ESME to ESME Simulator using [apache camel smpp component](http://camel.apache.org/smpp.html). It uses [SMPPSim](http://www.seleniumsoftware.com/user-guide.htm) as an SMSC to route messages to and from the running ESME instances. This application is designed to use one number as source and another as a destination, although it can be extended to allow dynamic sources and destinations, it's just enough for now to showcase camel smpp implementation using spring-boot.

## Engine Architecture
The image below shows the SMS engine architeture.

<img src="http://drive.google.com/uc?export=view&id=0B78cWOygph6QT01mUWgxZWZ6UjA" alt="SMS engine architecture" width="400" height="500">

### Bring it up
Run the following command inside messenger module to bring up two instances with numbers 8081 & 8082. The instances will also run on the same ports.

```yaml 
docker-compose up 
```
go to localhost:8081 and localhost:8082 to see both instances and test sending messages.

### Bring them up separately
#### SMPPSim
In this project, SMPPSim will be running on docker container, if you want to run it without docker, please refer to [this](http://www.seleniumsoftware.com/user-guide.htm) documentation.

```yaml 
docker run -d  run -d -p 8088:88 -p 2775:2775 -p 2776:2776 antenehrepos/docker-smppsim
```

#### EMSE instances
Instance 1 (8081) on port 8081
```bash 
java -jar /app/messenger-1.0.0.jar --spring.profiles.active=docker --smpp.address-range='^(\d{3})(8081)' --smpp.origin=8081 --smpp.destination=8082 --smpp.username=smppclient1 --smpp.password=password
```
Instance 2 (8082) on port 8082
```bash
java -jar /app/messenger-1.0.0.jar --spring.profiles.active=docker --smpp.address-range='^(\d{3})(8082)' --smpp.origin=8082 --smpp.destination=8081 --smpp.username=smppclient2 --smpp.password=password
```

### Copyright and license
    A spring-boot ESME to ESME simulator using apache camel smpp component.

    Copyright (C) 2016, Anteneh Aklilu

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses/.
