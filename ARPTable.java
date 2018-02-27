package net.floodlightcontroller.practical;

import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;;

import java.util.ArrayList;
import java.util.Iterator;

/**
* A class for managing the ARP table of the OpenFlow routers for SCC 365 router practical.
* @author Jamie Bird and Lyndon Fawcett
* @version 2.0
*/
public class ARPTable{

	/*
	*  ______________________________
	* |    IP    |         MAC       |
	* |__________|___________________|
	* | 10.0.1.1 | 00:00:00:00:00:01 |
	* |__________|___________________|
	*/
	private class ARPEntry{
		IPv4Address 	ip;
		MacAddress	mac; 
	
		public ARPEntry(IPv4Address ip, MacAddress mac){
			this.ip 	= ip;
			this.mac 	= mac;
		}
	}

	private ArrayList<ARPEntry> arpTable;

	/**
	* Create the ARP table.
	*/
	public ARPTable(){
		arpTable = new ArrayList<ARPEntry>();
	}

	/**
	* Add a newly learnt ARP entry.
	* @param	ip	IP address
	* @param	mac	MAC address
	*/
	public void addARP(IPv4Address ip, MacAddress mac){
		arpTable.add(new ARPEntry(ip, mac)); 
	}

	/**
	* Check if the ARP table contains an entry for a specified IP. 
	* @param	ip	IP address
	*/
	public boolean hasARP(IPv4Address ip){
		Iterator<ARPEntry> it = arpTable.iterator();
		while(it.hasNext()){
			ARPEntry a = it.next();
			if(a.ip.compareTo(ip) == 0)
				return true;
		} 
		return false;
	}

	/**
	* Get the MAC address of the specified IP.
	* @param	ip	IP address
	* @return	MAC address if ARP entry found, null otherwise. Recommed using has ARP prior.
	*/
	public MacAddress mac(IPv4Address ip){
		Iterator<ARPEntry> it = arpTable.iterator();
                while(it.hasNext()){
                        ARPEntry a = it.next();
                        if(a.ip.compareTo(ip) == 0)
                                return a.mac;
                }
                return null;
	}
}
