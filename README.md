# DataStax XML Demo

This application demonstrates how to store searchable XML documents in DataStax Enterprise.

#### Contact Points

To specify contact points use the `contactPoints` command line parameter. The value may contact multiple IPs in the format `IP,IP,IP`, without spaces. `-DcontactPoints=192.168.25.100,192.168.25.101`.

#### Schema

To create the schema, run:

	mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaSetup" -DcontactPoints=localhost

#### Solr Core

To create the solr core, run:

	dsetool create_core datastax_taxi_app.current_location reindex=true coreOptions=src/main/resources/solr/rt.yaml schema=src/main/resources/solr/geo.xml solrconfig=src/main/resources/solr/solrconfig.xml

#### Sample Data

To populate the database with sample data, run:

	mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.xml.Populator" -DcontactPoints=localhost

Thank you to the Department of Computer Sciences at the University of Wisconsin-Madison for the [XML movie data][niagara].

####

To start the web server, in another terminal run:

	mvn jetty:run

[niagara]: http://research.cs.wisc.edu/niagara/data.html "Niagara XML movie data"

	
To find all movements of a vehicle use http://localhost:8080/datastax-taxi-app/rest/getmovements/{vehicle}/{date} e.g.

	http://localhost:8080/datastax-taxi-app/rest/getmovements/1/20160112

Or

	select * from vehicle where vehicle = '1' and day='20160112';

To find all vehicle movement, use the rest command http://localhost:8080/datastax-taxi-app/rest/getvehicles/{tile} e.g.

	http://localhost:8080/datastax-taxi-app/rest/getvehicles/gcrf

or 

	CQL - select * from current_location where solr_query = '{"q": "tile1:gcrf"}' limit 1000;


To find all vehicles within a certain distance of a latitude and longitude, http://localhost:8080/datastax-taxi-app/rest/search/{lat}/{long}/{distance}

	http://localhost:8080/datastax-taxi-app/rest/search/52.53956077140064/-0.20225833920426117/5
	
Or

	select * from current_location where solr_query = '{"q": "*:*", "fq": "{!geofilt sfield=lat_long pt=52.53956077140064,-0.20225833920426117 d=5}"}' limit 1000;
 	
To remove the tables and the schema, run the following.

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaTeardown"
    
    
