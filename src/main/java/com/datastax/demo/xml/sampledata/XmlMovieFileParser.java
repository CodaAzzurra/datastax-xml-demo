package com.datastax.demo.xml.sampledata;

import com.datastax.demo.utils.XmlUtils;
import com.datastax.demo.xml.model.Actor;
import com.datastax.demo.xml.model.Movie;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XmlMovieFileParser
{
	private static final Logger logger = LoggerFactory.getLogger(XmlMovieFileParser.class);

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

	private String moviesDtdPath;

	public XmlMovieFileParser(String moviesDtdPath)
	{
		super();
		this.moviesDtdPath = moviesDtdPath;
	}

	public Movie parseMovieFile(File movieFile) throws ParserConfigurationException, IOException, SAXException
	{
		logger.debug(String.format("Reading movie file: %s", movieFile));
		byte[] sourceBytes = FileUtils.readFileToByteArray(movieFile);
		Movie movie = null;

		if (sourceBytes == null || sourceBytes.length == 0)
		{
			logger.warn(String.format("Ignoring empty movie file: %s", movieFile));
		}
		else
		{
			String dtdName = FilenameUtils.getName(moviesDtdPath);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver((publicId, systemId) ->
			{
				InputSource is = null;

				if (systemId.contains(dtdName))
				{
					is = new InputSource(new FileInputStream(moviesDtdPath));
				}

				return is;
			});

			Document document = builder.parse(new ByteArrayInputStream(sourceBytes));


			Node w4fNode = XmlUtils.getNode(W4F_DOC, document.getChildNodes());

			if (w4fNode == null)
			{
				logger.warn(String.format("Invalid movie file. Missing %s node.", W4F_DOC));
			}
			else
			{
				Node movieNode = XmlUtils.getNode(MOVIE, w4fNode.getChildNodes());

				if (movieNode == null)
				{
					logger.warn(String.format("Invalid movie file. Missing %s node.", MOVIE));
				}
				else
				{
					logger.debug(String.format("Found Movie node in document. Parsing: %s", movieNode));
					movie = parseMovieNode(movieNode, sourceBytes);
				}
			}
		}

		return movie;
	}

	private Movie parseMovieNode(Node movieNode, byte[] sourceBytes)
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

		return new Movie(title, year, directedBy, genres, cast, sourceBytes);
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
}
