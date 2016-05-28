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

import org.json.JSONException;
import org.json.JSONObject;

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
	ArrayList<JSONObject>, 
	ArrayList<JSONObject>> {

	private Context context;
	private ArrayList<JSONObject> foundFans; 
	public BroadcastTask(Context context) {
		this.context = context;
		this.foundFans = new ArrayList<JSONObject>();	
	}
	
	@Override
	protected ArrayList<JSONObject> doInBackground(String... params) {
		try {
			return broadcast();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this.foundFans;
	}
	
	@Override
	protected void onPostExecute(ArrayList<JSONObject>  controllers) {
		((MainActivity) this.context).broadcastCallback( controllers);
	}
	
	// Broadcasts UDP packets to all the hosts in every network interface.
	private ArrayList<JSONObject> broadcast() throws IOException {
		// Wll contain all the found fans.
		ArrayList<JSONObject> foundFans = new ArrayList<JSONObject>();
		
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
						JSONObject controllerData =  
							sendUDP(broadcastAddress);
					
						// If we got a valid controller then we found a controller,
						// then add it to the found fans list.
						if(controllerData != null) 
							foundFans.add(controllerData);
					} catch(SocketTimeoutException e) {
						// Catched.
					}
				}
			}
		}
		
		return foundFans;
	}
	
	// Sends the udp packet to the given host for controller detection.
	private JSONObject sendUDP(InetAddress remoteAddress) throws IOException {
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
		
		// Create another packet that has large buffer size.
		byte[] responsePayload = new byte[1024];
		packet = new DatagramPacket(
			responsePayload, responsePayload.length
		);
		socket.setSoTimeout(3000);
		// Wait for the response.
		socket.receive(packet);
		
		// Packet received, now get the address and other details from it.
		String address = packet.getAddress().getHostName();
		String packetData = (String) (new String(packet.getData(), "UTF-8").
				subSequence(0, packet.getLength()));
		
		// Parse it into json.
		JSONObject controllerData = null;
		try {
			controllerData = new JSONObject(packetData);
			// Add the address to it.
			controllerData.put("address", address);	
			// Log some.
			Log.i("UDP", controllerData.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return controllerData;
	}
	
	// Only selected the unique controllers.
	private ArrayList<JSONObject> getUnique(ArrayList<JSONObject> controllers) {
		return null;
	}
}
