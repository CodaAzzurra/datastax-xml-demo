package com.datastax.demo.xml.sampledata;

import com.datastax.demo.utils.XmlUtils;
import com.datastax.demo.xml.model.Actor;
import com.datastax.demo.xml.model.Movie;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class XmlMovieFileParser
{
	private static final Logger logger = LoggerFactory.getLogger(XmlMovieFileParser.class);

	private static final String ENCODING = "ISO-8859-1";

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
		Movie movie;

		try
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

			ByteArrayInputStream inputStream = new ByteArrayInputStream(sourceBytes);
			InputStreamReader reader = new InputStreamReader(inputStream, ENCODING);
			InputSource inputSource = new InputSource(reader);
			inputSource.setEncoding(ENCODING);
			Document document = builder.parse(inputSource);

			try
			{
				logger.debug(XmlUtils.nodeToString(document));
			}
			catch (TransformerException te)
			{
				logger.debug("Unable to debug XML document.", te);
			}

			Element rootElement = document.getDocumentElement();

			if (!W4F_DOC.equalsIgnoreCase(rootElement.getTagName()))
			{
				throw new SAXException(String.format("Invalid movie file. Missing %s element.", W4F_DOC));
			}

			Element movieElement = getFirstChildElementByTagName(rootElement, MOVIE);
			movie = parseMovieElement(movieElement, sourceBytes);

		}
		catch (Exception e)
		{
			throw new SAXException("Invalid XML movie file.", e);
		}

		return movie;
	}

	private Movie parseMovieElement(Element movieElement, byte[] sourceBytes)
	{
		String title = null;
		Integer year = null;
		List<String> directedBy = null;
		List<String> genres = null;
		List<Actor> cast = null;

		Element titleElement = getFirstChildElementByTagName(movieElement, TITLE);
		title = titleElement.getTextContent();

		Element yearElement = getFirstChildElementByTagName(movieElement, YEAR);
		year = Integer.parseInt(yearElement.getTextContent());

		Element directedByElement = getFirstChildElementByTagName(movieElement, DIRECTED_BY);
		directedBy = parseDirectedByElement(directedByElement);

		Element genresElement = getFirstChildElementByTagName(movieElement, GENRES);
		genres = parseGenresElement(genresElement);

		Element castElement = getFirstChildElementByTagName(movieElement, CAST);
		cast = parseCastElement(castElement);

		return new Movie(title, year, directedBy, genres, cast, sourceBytes);
	}

	private List<String> parseDirectedByElement(Element directedByElement)
	{
		ArrayList<String> directors = new ArrayList<>();
		NodeList directorElements = directedByElement.getElementsByTagName(DIRECTOR);

		for (int i = 0; i < directorElements.getLength(); i++)
		{
			Element directorElement = (Element) directorElements.item(i);
			directors.add(directorElement.getTextContent());
		}

		return directors;
	}

	private List<String> parseGenresElement(Element genresElement)
	{
		ArrayList<String> genres = new ArrayList<>();
		NodeList genreElements = genresElement.getElementsByTagName(GENRE);

		for (int i = 0; i < genreElements.getLength(); i++)
		{
			Element genreElement = (Element) genreElements.item(i);
			genres.add(genreElement.getTextContent());
		}

		return genres;
	}

	private List<Actor> parseCastElement(Element castElement)
	{
		ArrayList<Actor> actors = new ArrayList<>();
		NodeList actorElements = castElement.getElementsByTagName(ACTOR);

		for (int i = 0; i < actorElements.getLength(); i++)
		{
			Element actorElement = (Element) actorElements.item(i);

			Element firstNameElement = getFirstChildElementByTagName(actorElement, FIRST_NAME);
			String firstName = firstNameElement.getTextContent();

			Element lastNameElement = getFirstChildElementByTagName(actorElement, LAST_NAME);
			String lastName = lastNameElement.getTextContent();

			Actor actor = new Actor(firstName, lastName);
			actors.add(actor);
		}

		return actors;
	}

	private static Element getFirstChildElementByTagName(Element parent, String tagName)
	{
		NodeList nodeList = parent.getElementsByTagName(tagName);
		return (Element) nodeList.item(0);
	}
}
