package com.datastax.demo.xml.webservice;

import com.datastax.demo.xml.dao.MovieDao;
import com.datastax.demo.xml.model.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@WebService
@Path("/")
public class MovieWebService
{
	private static Logger logger = LoggerFactory.getLogger(MovieWebService.class);

	private MovieDao dao;

	public MovieWebService()
	{
		dao = MovieDao.build();
	}

	@GET
	@Path("/movie/{title}/{year}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMovie(@PathParam("title") String title, @PathParam("year") String yearStr)
	{
		logger.info(String.format("getMovie/%s/%s", title, yearStr));
		int year = Integer.parseInt(yearStr);
		Movie movie = dao.selectMovie(title, year);
		return Response.status(201).entity(movie).build();
	}

	@GET
	@Path("/search/title/{title}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchByTitle(@PathParam("title") String title)
	{
		logger.info(String.format("/search/title/%s", title));
		List<Movie> movies = dao.searchByTitle(title);
		return Response.status(201).entity(movies).build();
	}

	@GET
	@Path("/search/year/{year}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchByYear(@PathParam("year") String yearStr)
	{
		logger.info(String.format("/search/year/%s", yearStr));
		int year = Integer.parseInt(yearStr);
		List<Movie> movies = dao.searchByYear(year);
		return Response.status(201).entity(movies).build();
	}

	@GET
	@Path("/search/director/{director}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchByDirector(@PathParam("director") String director)
	{
		logger.info(String.format("/search/director/%s", director));
		List<Movie> movies = dao.searchByDirector(director);
		return Response.status(201).entity(movies).build();
	}

	@GET
	@Path("/search/genre/{genre}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchByGenre(@PathParam("genre") String genre)
	{
		logger.info(String.format("/search/genre/%s", genre));
		List<Movie> movies = dao.searchByGenre(genre);
		return Response.status(201).entity(movies).build();
	}

	@GET
	@Path("/search/director/{actor}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchByActor(@PathParam("actor") String actor)
	{
		logger.info(String.format("/search/actor/%s", actor));
		List<Movie> movies = dao.searchByActor(actor);
		return Response.status(201).entity(movies).build();
	}
}
