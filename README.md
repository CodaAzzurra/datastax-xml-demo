# DataStax XML Demo

**This application demonstrates how to store searchable XML documents in DataStax Enterprise.**

Disclaimer: this is merely an educational demonstration. It is not intended for production. For brevity and clarity I have made no attempt at security; I have not defended against bad inputs; I have not profiled and optimized. In other words, please see the [LICENSE][license] and use at your own risk.

On the other hand, I hope this will show one way to store, index, and search XML documents in DataStax Enterprise. Check out the [Technical Notes][wiki-technical] wiki page for a discussion on the design and implementation choices. Please write me with your thoughts and questions.

## Configure the Cluster

Please see the [Prepare the Cluster][wiki-preparecluster] wiki for information on setting up a DataStax Enterprise cluster. A note on DSE versions: I originally developed and tested this project against [DataStax Enterprise 4.8.4][dse484]. I have updated it for [DataStax Enterprise 4.8.10][dse4810]. It may also run well on other versions.

#### Contact Points

To specify contact points, use the `contactPoints` command line parameter. The value may contact multiple IPs in the format `IP,IP,IP`, without spaces. For example: `-DcontactPoints=192.168.25.100,192.168.25.101`.

#### Schema

To create the schema, run:

	mvn clean compile exec:java -Dexec.mainClass=com.datastax.demo.schema.SchemaSetup -DcontactPoints=localhost
	
To remove the schema, run:

	mvn clean compile exec:java -Dexec.mainClass=com.datastax.demo.schema.SchemaTeardown -DcontactPoints=localhost

#### Solr Core

To create the solr core, run:

	dsetool create_core datastax_xml_demo.movies reindex=true coreOptions=solr/rt.yaml schema=solr/movies_schema.xml solrconfig=solr/movies_solrconfig.xml

## Populate Sample Data

Thank you to the Department of Computer Sciences at the University of Wisconsin-Madison for the [XML movie data][niagara].

#### Bulk Load

To bulk load the database with sample data, run:

	mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.xml.sampledata.BulkDataLoader" -DcontactPoints=localhost

#### Web Service & Streaming Updates

Coming soon...

## Web Services

#### Launch the Web Server

To start the web server, in another terminal run:

	mvn jetty:run

#### Query Via the Web API

To **retrieve a specific movie**, use the following REST command:

	http://localhost:8080/datastax-xml-demo/rest/movie/{title}/{year}
	
	Example:
	http://localhost:8080/datastax-xml-demo/rest/movie/Network/1976
  
  
To **search by title**, use the following REST command:

	http://localhost:8080/datastax-xml-demo/rest/search/title/{title}
	
	Example:
	http://localhost:8080/datastax-xml-demo/rest/search/title/Strangelove
  
  
To **search by year**, use the following REST command:

	http://localhost:8080/datastax-xml-demo/rest/search/year/{year}
	
	Example:
	http://localhost:8080/datastax-xml-demo/rest/search/year/1981
  
  
To **search by director**, use the following REST command:

	http://localhost:8080/datastax-xml-demo/rest/search/director/{director}
	
	Example:
	http://localhost:8080/datastax-xml-demo/rest/search/director/%22James%20Cameron%22
  
  
To **search by genre**, use the following REST command:

	http://localhost:8080/datastax-xml-demo/rest/search/genre/{genre}
	
	Example:
	http://localhost:8080/datastax-xml-demo/rest/search/genre/drama
  
  
To **search by actor**, use the following REST command:

	http://localhost:8080/datastax-xml-demo/rest/search/actor/{actor}
	
	Example:
	http://localhost:8080/datastax-xml-demo/rest/search/actor/Eastwood

[dse484]: https://docs.datastax.com/en/datastax_enterprise/4.8/datastax_enterprise/RNdse.html#relnotes48__484 "DataStax Enterprise 4.8.4"
[dse4810]: https://docs.datastax.com/en/datastax_enterprise/4.8/datastax_enterprise/RNdse.html#relnotes48__4810 "DataStax Enterprise 4.8.10"
[license]: LICENSE "License"
[niagara]: http://research.cs.wisc.edu/niagara/data.html "Niagara XML movie data"
[wiki-preparecluster]: https://github.com/DC4DS/datastax-xml-demo/wiki/Prepare-the-Cluster "Prepare the Cluster"
[wiki-technical]: https://github.com/DC4DS/datastax-xml-demo/wiki/Technical-Notes "Technical Notes"
