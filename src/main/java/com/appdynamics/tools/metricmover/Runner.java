package com.appdynamics.tools.metricmover;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class Runner {

	private static Mover influxDBMover = new InfluxDBMover();
	private static final Logger logger = LoggerFactory.getLogger(Runner.class);

	public static void main(String[] args) throws Exception{
		InputStream stream = Runner.class.getClassLoader().getResourceAsStream("app.properties");
		Properties props = new Properties();
		props.load(stream);

		if(args.length>0) {
			props.setProperty("timeInMinutes",args[0]);
		}

		try {
			influxDBMover.move(props);
		} catch (Exception e) {
			logger.error("Exception happened: "+e.getMessage());
			e.printStackTrace();
		}

	}
	
}
