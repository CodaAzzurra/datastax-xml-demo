package com.datastax.demo.xml.sampledata;

import com.datastax.demo.utils.FileUtils;
import com.datastax.demo.utils.XmlUtils;
import com.datastax.demo.xml.dao.MovieDao;
import com.datastax.demo.xml.model.Actor;
import com.datastax.demo.xml.model.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SampleDataLoader
{
	private static final Logger logger = LoggerFactory.getLogger(SampleDataLoader.class);

	private static final String XML_MOVIES_DIR_PATH = "sample_data/xml-movies";

	private static final String W4F_DOC = "W4F_DOC";
	private static final String MOVIE = "Movie";
	private static final String TITLE = "Title";
	private static final String YEAR = "Year";
	private static final String DIRECTED_BY = "Directed_By";
	private static final String DIRECTOR = "Director";
	private static final String GENRES = "Genres";
	private static final String GENRE = "Genre";
	private static final String CAST = "Cast";
	private static final String ACTOR = "Actor";
	private static final String FIRST_NAME = "FirstName";
	private static final String LAST_NAME = "LastName";

	public SampleDataLoader()
	{
		super();
	}

	private void execute() throws ParserConfigurationException, SAXException, IOException
	{
		MovieDao dao = MovieDao.build();

		File moviesDir = new File(XML_MOVIES_DIR_PATH);
		File[] movieFiles = moviesDir.listFiles();

		if (movieFiles == null)
		{
			logger.warn(String.format("Unable to read XML Movies directory: %s", XML_MOVIES_DIR_PATH));
		}
		else
		{
			for (File movieFile : movieFiles)
			{
				Movie movie = parseMovieFile(movieFile);

				if (movie != null)
				{
					dao.insertMovieAsync(movie);
				}
			}
		}
	}

	private Movie parseMovieFile(File movieFile) throws ParserConfigurationException, IOException, SAXException
	{
		logger.debug(String.format("Parsing movie file: %s", movieFile));
		String sourceXml = FileUtils.readFileIntoString(movieFile);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new ByteArrayInputStream(sourceXml.getBytes()));

		Node wf4Node = XmlUtils.getNode(W4F_DOC, document.getChildNodes());
		assert wf4Node != null;

		Node movieNode = XmlUtils.getNode(MOVIE, wf4Node.getChildNodes());
		assert movieNode != null;
		logger.debug("Found Movie node in document. Parsing.");

		return parseMovieNode(movieNode, sourceXml);
	}

	private Movie parseMovieNode(Node movieNode, String sourceXml)
	{
		String title = null;
		Integer year = null;
		List<String> directedBy = null;
		List<String> genres = null;
		List<Actor> cast = null;

		NodeList children = movieNode.getChildNodes();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);

			switch (child.getLocalName())
			{
				case TITLE:
					title = child.getTextContent();
					break;

				case YEAR:
					year = Integer.parseInt(child.getTextContent());
					break;

				case DIRECTED_BY:
					directedBy = parseDirectedByNode(child);
					break;

				case GENRES:
					genres = parseGenresNode(child);
					break;

				case CAST:
					cast = parseCastNode(child);
					break;

				default:
					logger.warn(String.format("Encountered unexpected tag: %s.", child.getLocalName()));
					break;
			}
		}

		return new Movie(title, year, directedBy, genres, cast, sourceXml);
	}

	private List<String> parseDirectedByNode(Node directedByNode)
	{
		ArrayList<String> directors = new ArrayList<>();
		NodeList children = directedByNode.getChildNodes();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);

			if (DIRECTOR.equals(child.getLocalName()))
			{
				directors.add(child.getTextContent());
			}
		}

		return directors;
	}

	private List<String> parseGenresNode(Node genresNode)
	{
		ArrayList<String> genres = new ArrayList<>();
		NodeList children = genresNode.getChildNodes();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);

			if (GENRE.equals(child.getLocalName()))
			{
				genres.add(child.getTextContent());
			}
		}

		return genres;
	}

	private List<Actor> parseCastNode(Node castNode)
	{
		ArrayList<Actor> actors = new ArrayList<>();
		NodeList castChildren = castNode.getChildNodes();

		for (int i = 0; i < castChildren.getLength(); i++)
		{
			Node castChild = castChildren.item(i);

			if (ACTOR.equals(castChild.getLocalName()))
			{
				String firstName = null;
				String lastName = null;
				NodeList actorChildren = castChild.getChildNodes();

				for (int j = 0; j < actorChildren.getLength(); j++)
				{

					Node actorChild = actorChildren.item(i);

					if (FIRST_NAME.equals(actorChild.getLocalName()))
					{
						firstName = actorChild.getTextContent();
					}
					else if (LAST_NAME.equals(actorChild.getLocalName()))
					{
						lastName = actorChild.getTextContent();
					}
				}

				Actor actor = new Actor(firstName, lastName);
				actors.add(actor);
			}
		}

		return actors;
	}

	/**
	 * @param args None.
	 */
	public static void main(String[] args)
	{
		SampleDataLoader loader = new SampleDataLoader();

		try
		{
			loader.execute();
		}
		catch (Exception e)
		{
			logger.error("Fatal error loading sample data.", e);
		}
	}
}
