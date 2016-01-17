# DataStax XML Demo

This application demonstrates how to store searchable XML documents in DataStax Enterprise.

## Configure the Cluster

### Contact Points

To specify contact points use the `contactPoints` command line parameter. The value may contact multiple IPs in the format `IP,IP,IP`, without spaces. `-DcontactPoints=192.168.25.100,192.168.25.101`.

### Schema

To create the schema, run:

	mvn clean compile exec:java -Dexec.mainClass=com.datastax.demo.schema.SchemaSetup -DcontactPoints=localhost
	
To remove the schema, run:

	mvn clean compile exec:java -Dexec.mainClass=com.datastax.demo.schema.SchemaTeardown -DcontactPoints=localhost

### Solr Core

TODO To create the solr core, run:

	dsetool create_core datastax_xml_demo.movies reindex=true coreOptions=solr/rt.yaml schema=solr/movies_schema.xml solrconfig=solr/movies_solrconfig.xml

## Populate Sample Data

Thank you to the Department of Computer Sciences at the University of Wisconsin-Madison for the [XML movie data][niagara].

### Bulk Load

To bulk load the database with sample data, run:

	mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.xml.sampledata.BulkDataLoader" -DcontactPoints=localhost

### Web Service & Streaming Updates

Coming soon...

## Web Services

### Launch the Web Server

To start the web server, in another terminal run:

	mvn jetty:run

### Query Via the Web API

To retrieve a specific movie, use the following REST command:

	http://localhost:8080/datastax-xml-demo/rest/movie/{title}/{year}
	
	Example: http://localhost:8080/datastax-xml-demo/rest/movie/Network/1976
	
To search by genre, use the following REST command:

	http://localhost:8080/datastax-xml-demo/rest/search/genre/{genre}
	
	Example: http://localhost:8080/datastax-xml-demo/rest/search/genre/drama
	
[niagara]: http://research.cs.wisc.edu/niagara/data.html "Niagara XML movie data"
