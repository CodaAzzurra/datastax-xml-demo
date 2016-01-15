package com.datastax.demo.xml.webservice;

import java.util.List;

import com.datastax.demo.xml.VehicleDao;
import com.github.davidmoten.geo.LatLong;

public class TaxiService {

	private VehicleDao dao;
	
	public TaxiService(){
		String contactPointsStr = PropertyHelper.getProperty("contactPoints", "localhost");
		this.dao = new VehicleDao(contactPointsStr.split(","));
	}

	public List<Vehicle> getVehicleMovements(String vehicle, String dateString) {
		
		return dao.getVehicleMovements(vehicle, dateString);
	}
	
	public List<Vehicle> getVehiclesByTile(String tile){
		
		return dao.getVehiclesByTile(tile);
		
	}
	
	public List<Vehicle> searchVehiclesByLonLatAndDistance(int distance, LatLong latLong){
		return dao.searchVehiclesByLonLatAndDistance(distance, latLong);
	}
	
}
