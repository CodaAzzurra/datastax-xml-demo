package com.datastax.demo.xml.sampledata;

import com.datastax.demo.xml.VehicleDao;
import com.github.davidmoten.geo.LatLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SampleDataLoader
{
	private static final Logger logger = LoggerFactory.getLogger(SampleDataLoader.class);

	private static final String XML_MOVIES_DIR_PATH = "sample_data/xml-movies";

	private static int TOTAL_VEHICLES = 10000;
	private static int BATCH = 1000;
	private static Map<String, LatLong> vehicleLocations = new HashMap<String, LatLong>();

	private VehicleDao dao;

	public SampleDataLoader()
	{
	}

	private void populate()
	{

	}

//	public SampleDataLoader()
//	{
//		String contactPointsStr = PropertyHelper.getProperty("contactPoints", "localhost");
//		this.dao = new VehicleDao(contactPointsStr.split(","));
//
//		Timer timer = new Timer();
//		timer.start();
//
//		logger.info("Creating Locations");
//		createStartLocations();
//
//		while (true)
//		{
//			logger.info("Updating Locations");
//			updateLocations();
//			sleep(1);
//		}
//	}
//
//	private void updateLocations()
//	{
//		Map<String, LatLong> newLocations = new HashMap<String, LatLong>();
//
//		for (int i = 0; i < BATCH; i++)
//		{
//			String random = new Double(Math.random() * TOTAL_VEHICLES).intValue() + 1 + "";
//
//			LatLong latLong = vehicleLocations.get(random);
//			LatLong update = update(latLong);
//			vehicleLocations.put(random, update);
//			newLocations.put(random, update);
//		}
//
//		dao.insertVehicleLocation(newLocations);
//	}
//
//	private LatLong update(LatLong latLong)
//	{
//		double lon = latLong.getLon();
//		double lat = latLong.getLat();
//
//		if (Math.random() < .1)
//			return latLong;
//
//		if (Math.random() < .5)
//			lon += .0001d;
//		else
//			lon -= .0001d;
//
//		if (Math.random() < .5)
//			lat += .0001d;
//		else
//			lat -= .0001d;
//
//		return new LatLong(lat, lon);
//	}
//
//	private void createStartLocations()
//	{
//
//		for (int i = 0; i < TOTAL_VEHICLES; i++)
//		{
//			double lat = getRandomLat();
//			double lon = getRandomLng();
//
//			this.vehicleLocations.put("" + (i + 1), new LatLong(lat, lon));
//		}
//	}
//
//	/**
//	 * Between 1 and -1
//	 *
//	 * @return
//	 */
//	private double getRandomLng()
//	{
//		return (Math.random() < .5) ? Math.random() : -1 * Math.random();
//	}
//
//	/**
//	 * Between 50 and 55
//	 */
//	private double getRandomLat()
//	{
//
//		return Math.random() * 5 + 50;
//	}
//
//	private void sleep(int seconds)
//	{
//		try
//		{
//			Thread.sleep(seconds * 1000);
//		}
//		catch (InterruptedException e)
//		{
//			e.printStackTrace();
//		}
//	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		(new SampleDataLoader()).populate();
	}
}
