package com.datastax.demo.xml.sampledata;

import com.datastax.demo.xml.dao.MovieDao;
import com.datastax.demo.xml.model.Movie;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class BulkDataLoader
{
	private static final Logger logger = LoggerFactory.getLogger(BulkDataLoader.class);

	private static final String XML_MOVIES_DIR_PATH = "sample_data/xml-movies";
	private static final String MOVIES_DTD = "movies.dtd";
	private static final String XML = "xml";

	public BulkDataLoader() { super(); }

	private void execute()
	{
		XmlMovieFileParser parser = new XmlMovieFileParser(String.format("%s/%s", XML_MOVIES_DIR_PATH, MOVIES_DTD));
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
					try
					{
						logger.debug(String.format("Parsing XML movie file: %s", movieFile));
						Movie movie = parser.parseMovieFile(movieFile);

						if (movie != null)
						{
							logger.debug(String.format("Inserting movie: %s", movie));
							dao.insertMovieAsync(movie);
						}
					}
					catch (Exception e)
					{
						logger.warn("Exception loading XML movie.", e);
					}
				}
				else
				{
					logger.warn(br.message);
				}
			}
		}
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

		@Override
		public String toString()
		{
			return "BooleanResult{result=" + result
					+ ", message=" + message + "}";
		}
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
