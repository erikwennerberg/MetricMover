package com.appdynamics.tools.metricmover;


import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class Runner {

	private static Mover influxDBMover = new InfluxDBMover();
	private Map<String, ?> config;


	public static void main(String[] args) throws Exception {
		InputStream stream = Runner.class.getClassLoader().getResourceAsStream("app.properties");
		Properties props = new Properties();
		props.load(stream);

		if(args.length>0) {
			props.setProperty("timeInMinutes",args[0]);
		}

		influxDBMover.move(props);

	}
	
}
