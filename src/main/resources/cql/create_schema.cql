CREATE KEYSPACE IF NOT EXISTS datastax-xml-demo
	WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': '1' };

USE datastax-xml-demo;

CREATE TYPE IF NOT EXISTS actor
(
	first_name text,
	last_name text
)

CREATE TABLE IF NOT EXISTS movies(
	title text,
	year int,
	directed_by list<text>,
	genres list<text>,
	cast list<actor>,
	source_xml text,
	PRIMARY KEY ((title, year))
) WITH compaction = { 'class' :  'LeveledCompactionStrategy' };

TRUNCATE movies;