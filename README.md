<h1>Metric Mover</h1>

A utility that moves metric data from AppDynamics Controller for a time period to InfluxDB.

<h2>Prerequisites</h2>

- Java 1.7 JDK / Java 1.8 JDK
- Maven 
- InfluxDB/Grafana

<h2>Steps to use</h2>

0. Download the zip file from
https://github.com/erikwennerberg/MetricMover

1. Unzip the file on your local machine

2. Edit app.properties and add metric paths, controller info etc...

3. cd into the unzipped folder MetricMover

4. Build the project | Install maven before<br />
  a. mvn clean<br />
  b. mvn install<br />

5. Use a terminal to<br /> 
  a. cd bin<br />
  b. chmod +x *.sh<br />
  c. ./MetricMover.sh $t where $t is an optional parameter of the timeframe of the metrics needed, in minutes<br />

<h2>Current Support</h2>
Tested with 4.5.16 Controller and InfluxDB 1.8.0

For support please email: erik.wennerberg@appdynamics.com
