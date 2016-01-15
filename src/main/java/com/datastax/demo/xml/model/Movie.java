package com.datastax.demo.xml.model;

import com.github.davidmoten.geo.LatLong;

import java.util.Arrays;
import java.util.Date;

public class Movie
{
	private String title;
	private int year;
	private String[] directedBy;
	private String[] genres;
	private Actor[] cast;

	public Movie(String title, int year, String[] directedBy, String[] genres, Actor[] cast)
	{
		super();
		this.title = title;
		this.year = year;
		this.directedBy = directedBy;
		this.genres = genres;
		this.cast = cast;
	}

	public String getTitle() { return title; }

	public int getYear() { return year; }

	public String[] getDirectedBy() { return directedBy; }

	public String[] getGenres() { return genres; }

	public Actor[] getCast() { return cast; }

	@Override
	public String toString()
	{
		return "Movie{title=" + title
				+ ", year=" + year
				+ ", directedBy=" + Arrays.toString(directedBy)
				+ ", genres=" + Arrays.toString(genres)
				+ ", cast=" + Arrays.toString(cast)
				+ "}";
	}

}
