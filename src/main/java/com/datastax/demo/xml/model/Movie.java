package com.datastax.demo.xml.model;

import java.util.List;

public class Movie
{
	private String title;
	private Integer year;
	private List<String> directedBy;
	private List<String> genres;
	private List<Actor> cast;
	private byte[] sourceBytes;

	public Movie(String title, Integer year, List<String> directedBy, List<String> genres, List<Actor> cast, byte[] sourceBytes)
	{
		super();
		this.title = title;
		this.year = year;
		this.directedBy = directedBy;
		this.genres = genres;
		this.cast = cast;
		this.sourceBytes = sourceBytes;
	}

	public String getTitle() { return title; }

	public Integer getYear() { return year; }

	public List<String> getDirectedBy() { return directedBy; }

	public List<String> getGenres() { return genres; }

	public List<Actor> getCast() { return cast; }

	public byte[] getSourceBytes() { return sourceBytes; }

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
