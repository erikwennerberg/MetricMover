package com.appdynamics.tools.metricmover;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.appdynamics.appdrestapi.RESTAccess;
import org.appdynamics.appdrestapi.data.BusinessTransaction;
import org.appdynamics.appdrestapi.data.BusinessTransactions;
import org.appdynamics.appdrestapi.data.MetricData;
import org.appdynamics.appdrestapi.data.MetricDatas;
import org.appdynamics.appdrestapi.data.MetricValue;
import org.appdynamics.appdrestapi.data.MetricValues;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfluxDBMover implements Mover {

	private static final Logger logger = LoggerFactory.getLogger(InfluxDBMover.class);
	
	public void move(Properties props) throws Exception {
		String controller = props.getProperty("controller_url");
		String port = props.getProperty("controller_port");
		boolean useSSL = Boolean.parseBoolean(props.getProperty("controller_useSSL"));
		String user = props.getProperty("controller_user");
		String passwd = props.getProperty("controller_passwd");
		String account = props.getProperty("controller_account");
		List<String> apps = Arrays.asList(props.getProperty("controller_apps").split(","));
		List<String> metrics = Arrays.asList(props.getProperty("controller_metrics").split(","));

		logger.debug("apps found:" + apps);
		logger.debug("metrics found:" + metrics);

		String dbUrl = props.getProperty("influxDB_url");
		String dbPort = props.getProperty("influxDB_port");
		String dbuser = props.getProperty("influxDB_user");
		String dbpasswd = props.getProperty("influxDB_passwd");
		String dbName = props.getProperty("influxDB_db_name");
		long start = Long.parseLong(props.getProperty("controller_start_time"));
		long end = Long.parseLong(props.getProperty("controller_end_time"));
		String timeInMinutesString = props.getProperty("timeInMinutes");


		// if time in minutes was specified we override the start and end properties
		logger.debug("time paramater was passed:" + timeInMinutesString);

		if (timeInMinutesString != null){
			long timeInMinutes = Long.parseLong(timeInMinutesString );
			long now = System.currentTimeMillis() ;
			start = (now - (timeInMinutes*60000));
			end = now;

		}


		InfluxDB influxDB = createDatabase(dbUrl, dbPort, dbuser, dbpasswd, dbName);

		RESTAccess access = new RESTAccess(controller, port, useSSL, user, passwd, account);
		//wrapping in debug if loop to avoid date object creation in other cases
		if(logger.isDebugEnabled()) {
			logger.debug("Unix time interval from" + start + " to " + end);
			logger.debug("Date time interval is: " + new java.util.Date(start) + " to " + new java.util.Date(end));
			access.setDebugLevel(3);
		}


		//BusinessTransactions bts = access.getBTSForApplication(app);
		BatchPoints batchPoints = BatchPoints.database(dbName).retentionPolicy("autogen").consistency(ConsistencyLevel.ALL).build();
		for (String app : apps ) {
			logger.debug("processing app " + app );
			for (String metricPath: metrics) {
				logger.debug("processing metric path " + metricPath );
				//MetricDatas mDatas = access.getRESTBTMetricQuery(i, app, bt.getTierName(), bt.getName(), start, end);



				MetricDatas mDatas = access.getRESTGenericMetricQuery(app,metricPath,start,end,false);
				if (mDatas != null && mDatas.getMetric_data().size() > 0) {
					ArrayList<MetricData> mDataList = mDatas.getMetric_data();
					if (mDataList != null) {
						for (MetricData mData : mDataList) {
							//String freq = mData.getFrequency();
							//int mId = mData.getMetricId();
							String mPath = mData.getMetricPath();
							String metricName = getMeasurement(mPath);					
							//measurement: metricName (from mPath); time: time 
							//Tag: bt_name, tier, app
							//Field: count, current, val, max, min
							ArrayList<MetricValues> mValss = mData.getMetricValues();
							if (mValss != null) {
								for (MetricValues mVals : mValss) {
									ArrayList<MetricValue> mVal = mVals.getMetricValue();
									if (mVal != null) {
										for (MetricValue mv : mVal) {
											long count = mv.getCount();
											long current = mv.getCurrent();
											long val = mv.getValue();
											long max = mv.getMax();
											long min = mv.getMin();
											long sum = mv.getSum();
											double std = mv.getStdDev();
											long occurrences = mv.getOccurrences();
											
											long time = mv.getStartTimeInMillis();

											logger.debug("About to insert metric value into Influx DB: " + mv );
											Point point = Point.measurement(metricName).time(time, TimeUnit.MILLISECONDS)
											.addField("count", count).addField("current", current).addField("val", val).addField("max", max).addField("min", min)
											.addField("sum", sum).addField("std", std).addField("occurrences", occurrences)
											.tag("metric_path", metricPath).tag("app", app)
											.build();
											batchPoints.point(point);
										}										
									}
								}								
							}
						}						
					}
				} else {
					logger.info("No data for " + metricPath + " " + app + " for that time interval");

				}
			}
			influxDB.write(batchPoints);
			logger.info("Done for app "  + app);
		}

	}

	private static InfluxDB createDatabase(String dbUrl, String dbPort, String user, String passwd, String dbName) {
		InfluxDB influxDB = InfluxDBFactory.connect("http://" + dbUrl + ":" + dbPort, user, passwd);
		List<String> dbs = influxDB.describeDatabases();
		if (!dbs.contains(dbName)) {
			influxDB.createDatabase(dbName);
		}
		return influxDB;
	}
	
	private static String getMeasurement(String mPath) {
		//Business Transaction Performance|Business Transactions|Rocklin|search|Average Block Time (ms)
		if (mPath.contains("|")) {
			return mPath.substring(mPath.lastIndexOf("|") + 1);			
		} else {
			return mPath;
		}
	}
	
//	public static void main5(String[] args) {
//		InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
//		String dbName = "aTimeSeries";
//		influxDB.deleteDatabase(dbName);		
//	}
	
}