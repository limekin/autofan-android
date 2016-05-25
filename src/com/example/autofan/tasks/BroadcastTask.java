package com.example.autofan.tasks;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.example.autofan.MainActivity;
import com.example.autofan.Settings;
import com.example.autofan.storage.Store;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;



// Used to broadcast upd messges to the hosts in the network.
public class BroadcastTask extends AsyncTask<
	String, 
	ArrayList<Map<String, String>>, 
	ArrayList<Map<String, String>>> {

	private Context context;
	private ArrayList<Map<String, String>> foundFans; 
	public BroadcastTask(Context context) {
		this.context = context;
		this.foundFans = new ArrayList<Map<String, String>>();	
	}
	
	@Override
	protected ArrayList<Map<String, String>> doInBackground(String... params) {
		try {
			return broadcast();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this.foundFans;
	}
	
	@Override
	protected void onPostExecute(ArrayList<Map<String, String>>  controllers) {
		((MainActivity) this.context).broadcastCallback( controllers);
	}
	
	// Broadcasts UDP packets to all the hosts in every network interface.
	private ArrayList<Map<String, String>> broadcast() throws IOException {
		// Wll contain all the found fans.
		ArrayList<Map<String, String>> foundFans = new ArrayList<Map<String, String>>();
		
		// Get all the network interfaces of the system.
		Enumeration< NetworkInterface > interfaces = 
				NetworkInterface.getNetworkInterfaces();
		NetworkInterface singleInterf;
		
		while( interfaces.hasMoreElements() ) {
			// Get the next interface.
			singleInterf = interfaces.nextElement();
			Log.i("UDP", "Found network interface : " + singleInterf.getName());
			
			InterfaceAddress address;
			
			// Get all the addresses assigned to the network.
			List<InterfaceAddress> addresses = 
					singleInterf.getInterfaceAddresses();
			
			for(int i=0; i<addresses.size(); ++i) {
				address = addresses.get(i);
				InetAddress broadcastAddress = address.getBroadcast();
				// If broadcast address is available, send udp packet.
				if(broadcastAddress != null) {
					Log.i(
						"UDP", 
						"Found broadcast address: " + broadcastAddress.getHostName()
					);
					try {
						Map<String, String> controller =  
							sendUDP(broadcastAddress);
					
						// If we got a valid controller then we found a controller,
						// then add it to the found fans list.
						if(controller != null) 
							foundFans.add(controller);
					} catch(SocketTimeoutException e) {
						// Catched.
					}
				}
			}
		}
		
		return foundFans;
	}
	
	// Sends the udp packet to the given host for controller detection.
	private Map<String, String> sendUDP(InetAddress remoteAddress) throws IOException {
		// Open a socket at random port.
		DatagramSocket socket = new DatagramSocket();
		
		// Prepp the packet to send.
		byte[] payload = Settings.DISCOVERY_PAYLOAD.getBytes();
		DatagramPacket packet = new DatagramPacket(
			payload, payload.length,
			remoteAddress, Settings.DISCOVERY_PORT
		);
		
		// Send the packet.
		socket.send(packet);
		
		// Now wait for response.
		socket.setSoTimeout(2000);
		socket.receive(packet);
		
		// Packet received, now get the port and address form it.
		Map<String, String> controllerServer = new HashMap<String, String>();
		controllerServer.put("address", packet.getAddress().getHostName());
		controllerServer.put("port", (String) (new String(packet.getData()).subSequence(0, packet.getLength())));
		
		// Log some.
		Log.i("UDP", "Got address: " + controllerServer.get("address"));
		Log.i("UDP", "Got port: " + controllerServer.get("port"));
		Log.i("UDP", String.valueOf(packet.getLength()));
		
		return controllerServer;
	}
}
