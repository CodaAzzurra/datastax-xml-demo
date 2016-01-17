package com.datastax.demo.xml.dao;

import com.datastax.demo.utils.PropertyHelper;
import com.datastax.demo.xml.model.Actor;
import com.datastax.demo.xml.model.Movie;
import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MovieDao
{
	private static final Logger logger = LoggerFactory.getLogger(MovieDao.class);

	private static final String CONTACT_POINTS = "contactPoints";
	private static final String LOCALHOST = "localhost";
	private static final String DELIMITER = ",";

	private static final String KEYSPACE = "datastax_xml_demo";
	private static final String MOVIES_TABLE = KEYSPACE + ".movies";
	private static final String ACTOR_UDT = "actor";

	private static final String TITLE = "title";
	private static final String YEAR = "year";
	private static final String DIRECTED_BY = "directed_by";
	private static final String GENRES = "genres";

	private static final String CAST = "cast";
	private static final String ACTOR_FIRST_NAME = "first_name";
	private static final String ACTOR_LAST_NAME = "last_name";

	private static final String SOURCE_BYTES = "source_bytes";

	private static final String INSERT_MOVIE = String.format(
			"INSERT INTO %s (title, year, directed_by, genres, cast, source_bytes) VALUES (?,?,?,?,?,?);", MOVIES_TABLE);

	private static final String SELECT_MOVIE = String.format(
			"SELECT title, year, directed_by, genres, cast, source_bytes FROM %s WHERE title = ? and year = ?", MOVIES_TABLE);

	private static final String SEARCH_MOVIE = String.format(
			"SELECT title, year, directed_by, genres, cast, source_bytes FROM %s WHERE solr_query = ?", MOVIES_TABLE);

	private Session session;

	private PreparedStatement insertMoviePrep;
	private PreparedStatement selectMoviePrep;
	private PreparedStatement searchMoviePrep;

	private UserType actorUDT;

	public MovieDao(String[] contactPoints)
	{
		super();
		prepareDao(contactPoints);
	}

	public ResultSetFuture insertMovieAsync(Movie movie)
	{
		List<UDTValue> actorUdtValues = null;
		List<Actor> actors = movie.getCast();

		if (actors != null)
		{
			actorUdtValues = new ArrayList<>();

			for (Actor actor : movie.getCast())
			{
				UDTValue actorUdtValue = actorUDT.newValue()
						.setString(ACTOR_FIRST_NAME, actor.getFirstName())
						.setString(ACTOR_LAST_NAME, actor.getLastName());
				actorUdtValues.add(actorUdtValue);
			}
		}

		Integer year = movie.getYear();
		int yearUnboxed = (year == null) ? Integer.MIN_VALUE : year;

		BoundStatement bound = insertMoviePrep.bind()
				.setString(TITLE, movie.getTitle())
				.setInt(YEAR, yearUnboxed)
				.setList(DIRECTED_BY, movie.getDirectedBy())
				.setList(GENRES, movie.getGenres())
				.setList(CAST, actorUdtValues)
				.setBytes(SOURCE_BYTES, ByteBuffer.wrap(movie.getSourceBytes()));

		return session.executeAsync(bound);
	}

	public Movie selectMovie(String title, int year)
	{
		BoundStatement bound = selectMoviePrep.bind()
				.setString(0, title)
				.setInt(1, year);
		ResultSet resultSet = session.execute(bound);
		Row row = resultSet.one();
		return (row == null) ? null : rowToMovie(row);
	}

	public List<Movie> searchByTitle(String title)
	{
		String solrQuery = String.format("title:%s", title);

		BoundStatement bound = searchMoviePrep.bind()
				.setString(0, solrQuery);
		ResultSet resultSet = session.execute(bound);

		List<Movie> movies = new ArrayList<>();
		List<Row> rows = resultSet.all();

		for (Row row : rows)
		{
			Movie movie = rowToMovie(row);
			movies.add(movie);
		}

		return movies;
	}

	public List<Movie> searchByYear(int year)
	{
		String solrQuery = String.format("year:%d", year);

		BoundStatement bound = searchMoviePrep.bind()
				.setString(0, solrQuery);
		ResultSet resultSet = session.execute(bound);

		List<Movie> movies = new ArrayList<>();
		List<Row> rows = resultSet.all();

		for (Row row : rows)
		{
			Movie movie = rowToMovie(row);
			movies.add(movie);
		}

		return movies;
	}

	public List<Movie> searchByGenre(String genre)
	{
		String solrQuery = String.format("genres:%s", genre);

		BoundStatement bound = searchMoviePrep.bind()
				.setString(0, solrQuery);
		ResultSet resultSet = session.execute(bound);

		List<Movie> movies = new ArrayList<>();
		List<Row> rows = resultSet.all();

		for (Row row : rows)
		{
			Movie movie = rowToMovie(row);
			movies.add(movie);
		}

		return movies;
	}

	public List<Movie> searchByDirector(String director)
	{
		String solrQuery = String.format("directed_by:%s", director);

		BoundStatement bound = searchMoviePrep.bind()
				.setString(0, solrQuery);
		ResultSet resultSet = session.execute(bound);

		List<Movie> movies = new ArrayList<>();
		List<Row> rows = resultSet.all();

		for (Row row : rows)
		{
			Movie movie = rowToMovie(row);
			movies.add(movie);
		}

		return movies;
	}

	private Movie rowToMovie(Row row)
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

		ByteBuffer sourceByteBuffer = row.getBytes(SOURCE_BYTES);
		return new Movie(rTitle, rYear, directedBy, genres, actors, sourceByteBuffer.array());
	}

	private void prepareDao(String[] contactPoints)
	{
		logger.debug(String.format("Preparing MovieDao with contact points: %s.", Arrays.toString(contactPoints)));
		Cluster cluster = Cluster.builder()
				.withLoadBalancingPolicy(new TokenAwarePolicy(new DCAwareRoundRobinPolicy()))
				.addContactPoints(contactPoints).build();
		session = cluster.connect();

		insertMoviePrep = session.prepare(INSERT_MOVIE);
		selectMoviePrep = session.prepare(SELECT_MOVIE);
		searchMoviePrep = session.prepare(SEARCH_MOVIE);

		actorUDT = session.getCluster().getMetadata().getKeyspace(KEYSPACE).getUserType(ACTOR_UDT);
	}

	public static MovieDao build()
	{
		String contactPointsStr = PropertyHelper.getProperty(CONTACT_POINTS, LOCALHOST);
		String[] contactPoints = contactPointsStr.split(DELIMITER);
		return new MovieDao(contactPoints);
	}
}
