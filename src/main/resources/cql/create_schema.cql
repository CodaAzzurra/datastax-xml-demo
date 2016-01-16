CREATE KEYSPACE IF NOT EXISTS datastax_xml_demo WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': '1' };

USE datastax_xml_demo;

CREATE TYPE IF NOT EXISTS actor(
	first_name text,
	last_name text
);

CREATE TABLE IF NOT EXISTS movies(
	title text,
	year int,
	directed_by list<text>,
	genres list<text>,
	cast list<frozen <actor>>,
	source_bytes blob,
	PRIMARY KEY ((title, year))
) WITH compaction = { 'class' :  'LeveledCompactionStrategy' };

TRUNCATE movies;
