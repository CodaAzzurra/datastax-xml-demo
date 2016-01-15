package com.datastax.demo.xml.dao;

import com.datastax.demo.xml.AsyncWriterWrapper;
import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class MovieDao
{
	private static Logger logger = LoggerFactory.getLogger(MovieDao.class);

	private static final String KEYSPACE = "datastax-xml-demo";
	private static final String MOVIES_TABLE = KEYSPACE + ".movies";

	private static final String INSERT_INTO_VEHICLE = String.format(
			"INSERT INTO %s (vehicle, day, date, lat_long, tile2) values (?,?,?,?,?);", MOVIES_TABLE);

	private static final String QUERY_BY_VEHICLE = "select * from " + vehicleTable + " where vehicle = ? and day = ?";

	private Session session;

	private PreparedStatement insertVehicle;
	private PreparedStatement insertCurrentLocation;
	private PreparedStatement queryVehicle;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");

	public MovieDao(String[] contactPoints)
	{

		Cluster cluster = Cluster.builder()
								 .withLoadBalancingPolicy(new TokenAwarePolicy(new DCAwareRoundRobinPolicy()))
								 .addContactPoints(contactPoints).build();

		this.session = cluster.connect();

		this.insertVehicle = session.prepare(INSERT_INTO_VEHICLE);
		this.insertCurrentLocation = session.prepare(INSERT_INTO_CURRENTLOCATION);

		this.queryVehicle = session.prepare(QUERY_BY_VEHICLE);
	}

	public void insertVehicleLocation(Map<String, LatLong> newLocations)
	{
		AsyncWriterWrapper wrapper = new AsyncWriterWrapper();

		Set<Entry<String, LatLong>> entrySet = newLocations.entrySet();

		Date date = new Date();

		for (Entry<String, LatLong> entry : entrySet)
		{

			String tile1 = GeoHash.encodeHash(entry.getValue(), 4);
			String tile2 = GeoHash.encodeHash(entry.getValue(), 7);


			wrapper.addStatement(insertVehicle.bind(entry.getKey(), dateFormatter.format(date), date,
					entry.getValue().getLat() + "," + entry.getValue().getLon(), tile2));

			wrapper.addStatement(insertCurrentLocation.bind(entry.getKey(), tile1, tile2,
					entry.getValue().getLat() + "," + entry.getValue().getLon(), new Date()));
		}
		wrapper.executeAsync(this.session);
	}

	public List<Vehicle> getVehicleMovements(String vehicleId, String dateString)
	{
		ResultSet resultSet = session.execute(this.queryVehicle.bind(vehicleId, dateString));

		List<Vehicle> vehicleMovements = new ArrayList<Vehicle>();
		List<Row> all = resultSet.all();

		for (Row row : all)
		{
			Date date = row.getTimestamp("date");
			String lat_long = row.getString("lat_long");
			String tile = row.getString("tile2");
			Double lat = Double.parseDouble(lat_long.substring(0, lat_long.lastIndexOf(",")));
			Double lng = Double.parseDouble(lat_long.substring(lat_long.lastIndexOf(",") + 1));

			Vehicle vehicle = new Vehicle(vehicleId, date, new LatLong(lat, lng), tile, "");
			vehicleMovements.add(vehicle);
		}

		return vehicleMovements;
	}

	public List<Vehicle> searchVehiclesByLonLatAndDistance(int distance, LatLong latLong)
	{

		String cql = "select * from " + currentLocationTable
				+ " where solr_query = '{\"q\": \"*:*\", \"fq\": \"{!geofilt sfield=lat_long pt="
				+ latLong.getLat() + "," + latLong.getLon() + " d=" + distance + "}\"}'  limit 1000";
		ResultSet resultSet = session.execute(cql);

		List<Vehicle> vehicleMovements = new ArrayList<Vehicle>();
		List<Row> all = resultSet.all();

		for (Row row : all)
		{
			Date date = row.getTimestamp("date");
			String vehicleId = row.getString("vehicle");
			String lat_long = row.getString("lat_long");
			String tile = row.getString("tile2");
			Double lat = Double.parseDouble(lat_long.substring(0, lat_long.lastIndexOf(",")));
			Double lng = Double.parseDouble(lat_long.substring(lat_long.lastIndexOf(",") + 1));

			Vehicle vehicle = new Vehicle(vehicleId, date, new LatLong(lat, lng), tile, "");
			vehicleMovements.add(vehicle);
		}

		return vehicleMovements;
	}

	public List<Vehicle> getVehiclesByTile(String tile)
	{
		String cql = "select * from " + currentLocationTable + " where solr_query = '{\"q\": \"tile1: " + tile + "\"}' limit 1000";
		ResultSet resultSet = session.execute(cql);

		List<Vehicle> vehicleMovements = new ArrayList<Vehicle>();
		List<Row> all = resultSet.all();

		for (Row row : all)
		{
			Date date = row.getTimestamp("date");
			String vehicleId = row.getString("vehicle");
			String lat_long = row.getString("lat_long");
			String tile1 = row.getString("tile1");
			String tile2 = row.getString("tile2");
			Double lat = Double.parseDouble(lat_long.substring(0, lat_long.lastIndexOf(",")));
			Double lng = Double.parseDouble(lat_long.substring(lat_long.lastIndexOf(",") + 1));

			Vehicle vehicle = new Vehicle(vehicleId, date, new LatLong(lat, lng), tile1, tile2);
			vehicleMovements.add(vehicle);
		}

		return vehicleMovements;
	}


}
