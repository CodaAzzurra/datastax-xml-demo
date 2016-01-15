package com.datastax.demo.xml.model;

import java.util.List;

public class Movie
{
	private String title;
	private int year;
	private List<String> directedBy;
	private List<String> genres;
	private List<Actor> cast;
	private String sourceXml;

	public Movie(String title, int year, List<String> directedBy, List<String> genres, List<Actor> cast, String sourceXml)
	{
		super();
		this.title = title;
		this.year = year;
		this.directedBy = directedBy;
		this.genres = genres;
		this.cast = cast;
		this.sourceXml = sourceXml;
	}

	public String getTitle() { return title; }

	public int getYear() { return year; }

	public List<String> getDirectedBy() { return directedBy; }

	public List<String> getGenres() { return genres; }

	public List<Actor> getCast() { return cast; }

	public String getSourceXml() { return sourceXml; }

	@Override
	public String toString()
	{
		return "Movie{title=" + title
				+ ", year=" + year
				+ ", directedBy=" + directedBy
				+ ", genres=" + genres
				+ ", cast=" + cast
				+ "}";
	}

}
