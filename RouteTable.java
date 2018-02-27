package net.floodlightcontroller.practical;

import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.MacAddress;

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

/**
* A class for managing the route table of the OpenFlow routers for SCC365 router excercise.
* @author Jamie Bird and Lyndon Fawcett
* @version 2.0
*/
public class RouteTable{
	
	/*
	*  ____________________________________
	* | Destination | Next hop | Out port |
	* |_____________|__________|__________|
	* | 10.0.1.0/24 | 10.0.3.1 |    3     |
	* |_____________|__________|__________| 
	*/
	private class RouteEntry{
		IPv4Address		nextHop;
		IPv4AddressWithMask	dstNet;
		int			outPort;

		public RouteEntry(IPv4AddressWithMask dstNet, IPv4Address nextHop, int outPort){
			this.dstNet 	= dstNet;
			this.nextHop 	= nextHop;
			this.outPort	= outPort;
		}
	}

	protected static Logger log;

	private ArrayList<RouteEntry> routeTable;
	
	/**
	* Creates routing table
	*/
	public RouteTable(){
		routeTable = new ArrayList<RouteEntry>();
		log = LoggerFactory.getLogger(RouteTable.class);
	}
	
	/**
	* Add a newly learnt route to the routing table
	* @param	dstNet		Destination network
	* @param	nextHop		Next hop IP address
	* @param	outPort		Out port of next hop
	*/
	public void addRoute(IPv4AddressWithMask dstNet, IPv4Address nextHop, int outPort){
		routeTable.add(new RouteEntry(dstNet, nextHop, outPort));
	}
	
	/**
	* Check if the routing table contains a route to specified IP.
	* @param	ip		Desired destination
	*/
	public boolean hasRoute(IPv4Address ip){
		Iterator<RouteEntry> it = routeTable.iterator();
		while(it.hasNext()){
			RouteEntry r = it.next();
			SubnetUtils.SubnetInfo si = new SubnetUtils(r.dstNet.toString()).getInfo();
			if(si.isInRange(ip.toString()))
				return true;
		}
		return false;
	}

	/**
	* Get the destination network of the specified IP.
	* @param	ip	Desired destination
	* @return	Destination network if route exists, null otherwise. Recommed using hasRoute prior.
	*/
	public IPv4AddressWithMask dstNet(IPv4Address ip){
		Iterator<RouteEntry> it = routeTable.iterator();
		while(it.hasNext()){
			RouteEntry r = it.next();
			SubnetUtils.SubnetInfo si = new SubnetUtils(r.dstNet.toString()).getInfo();
			if(si.isInRange(ip.toString()))
				return r.dstNet;
		}
		return null;
	}

	/**
	* Get the next hop of the specified IP.
	* @param	ip	Desired destination
	* @return	Next hop if route exists, null otherwise. Recommed using hasRoute prior.
	*/
	public IPv4Address nextHop(IPv4Address ip){
		Iterator<RouteEntry> it = routeTable.iterator();
		while(it.hasNext()){
			RouteEntry r = it.next();
			SubnetUtils.SubnetInfo si = new SubnetUtils(r.dstNet.toString()).getInfo();
			if(si.isInRange(ip.toString()))
				return r.nextHop;
		}
		return null;
	}

	/**
	* Get the out port of the specified IP.
	* @param	ip	Desired destination.
	* @return	Out port if route exists, -1 otherwise. Recommend using hasRoute prior.
	*/
	public int outPort(IPv4Address ip){
		Iterator<RouteEntry> it = routeTable.iterator();
		while(it.hasNext()){
			RouteEntry r = it.next();
			SubnetUtils.SubnetInfo si = new SubnetUtils(r.dstNet.toString()).getInfo();
			if(si.isInRange(ip.toString()))
				return r.outPort;
		}
		return -1;
	}
	
	/**
	* Get the number of entries in the route table.
	* @return 	Number of entries
	*/
	public int entries(){
		return routeTable.size();
	}

}
