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
	directed_by set<text>,
	genres set<text>,
	cast list<actor>,
	PRIMARY KEY ((title, year))
) WITH compaction = { 'class' :  'LeveledCompactionStrategy' };

TRUNCATE movies;