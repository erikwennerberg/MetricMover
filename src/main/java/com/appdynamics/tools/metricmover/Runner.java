package com.appdynamics.tools.metricmover;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.InputStream;
import java.util.Properties;

public class Runner {
	
	private static Mover elasticsearchMover = new ElasticsearchMover();
	private static Mover influxDBMover = new InfluxDBMover();
	
	public static void main(String[] args) throws Exception {
		//InputStream stream = Runner.class.getClassLoader().getResourceAsStream("app.properties");
		//Properties props = new Properties();
		//props.load(stream);


		ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));
		
		String db = props.getProperty("destination_db");
		if ("elasticsearch".equalsIgnoreCase(db)) {
			elasticsearchMover.move(props);
		} else if ("influxDB".equalsIgnoreCase(db)) {
			influxDBMover.move(props);			
		} else {
			//show usage
		}
	}
	
}
