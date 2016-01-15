package com.datastax.demo.xml.sampledata;

import com.datastax.demo.xml.model.Actor;
import com.datastax.demo.xml.model.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.util.ArrayList;

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

	private void execute() throws XMLStreamException
	{
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
				Movie movie = parseMovieFile(movieFile.getPath());

				if (movie != null)
				{
					movieList.add(movie);
				}
			}


		}
	}

	private Movie parseMovieFile(String movieFilePath) throws XMLStreamException
	{
		logger.debug(String.format("Parsing movie file: %s", movieFilePath));

		XMLStreamReader reader = null;
		Movie movie = null;

		try
		{
			XMLInputFactory factory = XMLInputFactory.newInstance();
			reader = factory.createXMLStreamReader(
					ClassLoader.getSystemResourceAsStream(movieFilePath));

			while (reader.hasNext())
			{
				int event = reader.next();

				switch (event)
				{
					case XMLStreamConstants.START_DOCUMENT:
					{
						logger.debug(String.format("START_DOCUMENT: %s", reader.getLocalName()));
					}
					break;

					case XMLStreamConstants.END_DOCUMENT:
					{
						logger.debug(String.format("END_DOCUMENT: %s", reader.getLocalName()));
					}
					break;

					case XMLStreamConstants.START_ELEMENT:
					{
						logger.debug(String.format("START_ELEMENT: %s", reader.getLocalName()));

						if (W4F_DOC.equals(reader.getLocalName()))
						{
							movie = parseMovie(reader);
						}
					}
					break;

					case XMLStreamConstants.END_ELEMENT:
					{
						logger.debug(String.format("END_ELEMENT: %s", reader.getLocalName()));
					}
				}
			}
		}
		finally
		{
			if (reader != null)
			{
				reader.close();
			}
		}

		return movie;
	}

	private Movie parseMovie(XMLStreamReader reader) throws XMLStreamException
	{
		String title = null;
		int year = 1;
		String[] directedBy = null;
		String[] genres = null;
		Actor[] cast = null;

		while (reader.hasNext())
		{
			int event = reader.next();

			switch (event)
			{
				case XMLStreamConstants.START_ELEMENT:
				{
					if (W4F_DOC.equals(reader.getLocalName()))
				}
			}
		}

		Movie movie = new Movie(title, year, directedBy, genres, cast);
		return null;
	}

	/*
	while(reader.hasNext()){
19
      int event = reader.next();
20

21
      switch(event){
22
        case XMLStreamConstants.START_ELEMENT:
23
          if ("employee".equals(reader.getLocalName())){
24
            currEmp = new Employee();
25
            currEmp.id = reader.getAttributeValue(0);
26
          }
27
          if("employees".equals(reader.getLocalName())){
28
            empList = new ArrayList<>();
29
          }
30
          break;
31

32
        case XMLStreamConstants.CHARACTERS:
33
          tagContent = reader.getText().trim();
34
          break;
35

36
        case XMLStreamConstants.END_ELEMENT:
37
          switch(reader.getLocalName()){
38
            case "employee":
39
              empList.add(currEmp);
40
              break;
41
            case "firstName":
42
              currEmp.firstName = tagContent;
43
              break;
44
            case "lastName":
45
              currEmp.lastName = tagContent;
46
              break;
47
            case "location":
48
              currEmp.location = tagContent;
49
              break;
50
          }
51
          break;
52

53
        case XMLStreamConstants.START_DOCUMENT:
54
          empList = new ArrayList<>();
55
          break;
56
      }

	 */


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
	 * @param args None.
	 */
	public static void main(String[] args)
	{
		SampleDataLoader loader = new SampleDataLoader();

		try
		{
			loader.execute();
		}
		catch (XMLStreamException xmlse)
		{
			logger.error("Fatal error loading sample data.", xmlse);
		}
	}
}
