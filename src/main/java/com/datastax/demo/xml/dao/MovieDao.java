package com.datastax.demo.xml.dao;

import com.datastax.demo.utils.PropertyHelper;
import com.datastax.demo.xml.model.Actor;
import com.datastax.demo.xml.model.Movie;
import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MovieDao
{
	private static final Logger logger = LoggerFactory.getLogger(MovieDao.class);

	private static final String CONTACT_POINTS = "contactPoints";
	private static final String LOCALHOST = "localhost";
	private static final String DELIMITER = ",";

	private static final String KEYSPACE = "datastax-xml-demo";
	private static final String MOVIES_TABLE = KEYSPACE + ".movies";

	private static final String TITLE = "title";
	private static final String YEAR = "year";
	private static final String DIRECTED_BY = "directed_by";
	private static final String GENRES = "genres";

	private static final String CAST = "cast";
	private static final String ACTOR_FIRST_NAME = "first_name";
	private static final String ACTOR_LAST_NAME = "last_name";

	private static final String SOURCE_XML = "source_xml";

	private static final String INSERT_MOVIE = String.format(
			"INSERT INTO %s (title, year, directed_by, genres, cast, source_xml) VALUES (?,?,?,?,?,?);", MOVIES_TABLE);

	private static final String SELECT_MOVIE = String.format(
			"SELECT title, year, directed_by, genres, cast FROM %s WHERE title = ? and year = ?", MOVIES_TABLE);

	private Session session;

	private PreparedStatement insertMoviePrep;
	private PreparedStatement selectMoviePrep;

	public MovieDao(String[] contactPoints)
	{
		super();
		prepareDao(contactPoints);
	}

	public ResultSetFuture insertMovieAsync(Movie movie)
	{
		BoundStatement bound = insertMoviePrep.bind();
		bound.setString(TITLE, movie.getTitle());
		bound.setInt(YEAR, movie.getYear());
		bound.setList(DIRECTED_BY, movie.getDirectedBy());
		bound.setList(GENRES, movie.getGenres());
		bound.setList(CAST, movie.getCast());
		return session.executeAsync(bound);
	}

	public Movie selectMovie(String title, int year)
	{
		BoundStatement bound = selectMoviePrep.bind();
		bound.setString(0, title);
		bound.setInt(1, year);

		ResultSet resultSet = session.execute(bound);
		Row row = resultSet.one();
		Movie movie = null;

		if (row != null)
		{
			String rTitle = row.getString(TITLE);
			int rYear = row.getInt(YEAR);
			List<String> directedBy = row.getList(DIRECTED_BY, String.class);
			List<String> genres = row.getList(GENRES, String.class);

			List<UDTValue> cast = row.getList(CAST, UDTValue.class);
			List<Actor> actors = new ArrayList<>();

			for (UDTValue actorUDT : cast)
			{
				String actorFirstName = actorUDT.getString(ACTOR_FIRST_NAME);
				String actorLastName = actorUDT.getString(ACTOR_LAST_NAME);
				Actor actor = new Actor(actorFirstName, actorLastName);
				actors.add(actor);
			}

			String sourceXml = row.getString(SOURCE_XML);

			movie = new Movie(rTitle, rYear, directedBy, genres, actors, sourceXml);
		}

		return movie;
	}

	public List<Movie> searchByGenre(String genre)
	{
		return null;
	}

//
//	public List<Vehicle> searchVehiclesByLonLatAndDistance(int distance, LatLong latLong)
//	{
//
//		String cql = "select * from " + currentLocationTable
//				+ " where solr_query = '{\"q\": \"*:*\", \"fq\": \"{!geofilt sfield=lat_long pt="
//				+ latLong.getLat() + "," + latLong.getLon() + " d=" + distance + "}\"}'  limit 1000";
//		ResultSet resultSet = session.execute(cql);
//
//		List<Vehicle> vehicleMovements = new ArrayList<Vehicle>();
//		List<Row> all = resultSet.all();
//
//		for (Row row : all)
//		{
//			Date date = row.getTimestamp("date");
//			String vehicleId = row.getString("vehicle");
//			String lat_long = row.getString("lat_long");
//			String tile = row.getString("tile2");
//			Double lat = Double.parseDouble(lat_long.substring(0, lat_long.lastIndexOf(",")));
//			Double lng = Double.parseDouble(lat_long.substring(lat_long.lastIndexOf(",") + 1));
//
//			Vehicle vehicle = new Vehicle(vehicleId, date, new LatLong(lat, lng), tile, "");
//			vehicleMovements.add(vehicle);
//		}
//
//		return vehicleMovements;
//	}
//
//	public List<Vehicle> getVehiclesByTile(String tile)
//	{
//		String cql = "select * from " + currentLocationTable + " where solr_query = '{\"q\": \"tile1: " + tile + "\"}' limit 1000";
//		ResultSet resultSet = session.execute(cql);
//
//		List<Vehicle> vehicleMovements = new ArrayList<Vehicle>();
//		List<Row> all = resultSet.all();
//
//		for (Row row : all)
//		{
//			Date date = row.getTimestamp("date");
//			String vehicleId = row.getString("vehicle");
//			String lat_long = row.getString("lat_long");
//			String tile1 = row.getString("tile1");
//			String tile2 = row.getString("tile2");
//			Double lat = Double.parseDouble(lat_long.substring(0, lat_long.lastIndexOf(",")));
//			Double lng = Double.parseDouble(lat_long.substring(lat_long.lastIndexOf(",") + 1));
//
//			Vehicle vehicle = new Vehicle(vehicleId, date, new LatLong(lat, lng), tile1, tile2);
//			vehicleMovements.add(vehicle);
//		}
//
//		return vehicleMovements;
//	}

	private void prepareDao(String[] contactPoints)
	{
		logger.debug(String.format("Preparing MovieDao with contact points: %s.", Arrays.toString(contactPoints)));
		Cluster cluster = Cluster.builder()
								 .withLoadBalancingPolicy(new TokenAwarePolicy(new DCAwareRoundRobinPolicy()))
								 .addContactPoints(contactPoints).build();
		session = cluster.connect();
		insertMoviePrep = session.prepare(INSERT_MOVIE);
		selectMoviePrep = session.prepare(SELECT_MOVIE);
	}

	public static MovieDao build()
	{
		String contactPointsStr = PropertyHelper.getProperty(CONTACT_POINTS, LOCALHOST);
		String[] contactPoints = contactPointsStr.split(DELIMITER);
		return new MovieDao(contactPoints);
	}
}
