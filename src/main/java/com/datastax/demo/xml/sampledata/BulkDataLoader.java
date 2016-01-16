package com.datastax.demo.xml.sampledata;

import com.datastax.demo.utils.XmlUtils;
import com.datastax.demo.xml.dao.MovieDao;
import com.datastax.demo.xml.model.Actor;
import com.datastax.demo.xml.model.Movie;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
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

public class BulkDataLoader
{
	private static final Logger logger = LoggerFactory.getLogger(BulkDataLoader.class);

	private static final String XML_MOVIES_DIR_PATH = "src/main/resources/sample_data/xml-movies";
	private static final String XML = "xml";

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

	public BulkDataLoader()
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
				BooleanResult br = shouldParseXmlMovieFile(movieFile);

				if (br.result)
				{
					Movie movie = parseMovieFile(movieFile);

					if (movie != null)
					{
						dao.insertMovieAsync(movie);
					}
				}
				else
				{
					logger.warn(br.message);
				}
			}
		}
	}

	private Movie parseMovieFile(File movieFile) throws ParserConfigurationException, IOException, SAXException
	{
		logger.debug(String.format("Reading movie file: %s", movieFile));
		String sourceXml = FileUtils.readFileToString(movieFile);
		Movie movie = null;

		if (StringUtils.isBlank(sourceXml))
		{
			logger.warn(String.format("Ignoring blank movie file: %s", movieFile));
		}
		else
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new ByteArrayInputStream(sourceXml.getBytes()));

			Node wf4Node = XmlUtils.getNode(W4F_DOC, document.getChildNodes());
			assert wf4Node != null;

			Node movieNode = XmlUtils.getNode(MOVIE, wf4Node.getChildNodes());
			assert movieNode != null;
			logger.debug("Found Movie node in document. Parsing.");

			movie = parseMovieNode(movieNode, sourceXml);
		}

		return movie;
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
			else
			{
				logger.warn(String.format("Encountered unexpected tag: %s.", child.getLocalName()));
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
			else
			{
				logger.warn(String.format("Encountered unexpected tag: %s.", child.getLocalName()));
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
					else
					{
						logger.warn(String.format("Encountered unexpected tag: %s.", actorChild.getLocalName()));
					}
				}

				Actor actor = new Actor(firstName, lastName);
				actors.add(actor);
			}
			else
			{
				logger.warn(String.format("Encountered unexpected tag: %s.", castChild.getLocalName()));
			}
		}

		return actors;
	}

	private static BooleanResult shouldParseXmlMovieFile(File movieFile)
	{
		BooleanResult br = new BooleanResult();

		if (movieFile.isFile())
		{
			if (movieFile.isHidden())
			{
				br.result = false;
				br.message = String.format("Ignoring hidden file: %s", movieFile);
			}
			else
			{
				String ext = FilenameUtils.getExtension(movieFile.getName());

				if (XML.equalsIgnoreCase(ext))
				{
					br.result = true;
					br.message = null;
				}
				else
				{
					br.result = false;
					br.message = String.format("Ignoring file without XML extension: %s", movieFile);
				}
			}
		}
		else
		{
			br.result = false;
			br.message = String.format("Ignoring directory: %s", movieFile);
		}

		return br;
	}

	private static class BooleanResult
	{
		public boolean result;
		public String message;
	}

	/**
	 * @param args None.
	 */
	public static void main(String[] args)
	{
		BulkDataLoader loader = new BulkDataLoader();

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
