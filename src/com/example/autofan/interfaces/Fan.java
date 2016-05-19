package com.example.autofan.interfaces;

import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.autofan.HomeActivity;
import com.example.autofan.MainActivity;
import com.example.autofan.storage.Store;

import android.content.Context;
import android.content.Intent;

public class Fan {
	
	private int state;
	private int prevState;
	private String triedAction;
	private HomeActivity context;
	private String controllerServer;
	
	public Fan(HomeActivity context) {
		this.context = context;
		this.state = 0;
		
		this.setState();
		this.setControllerServer();
	}
	
	// Sets the current state of the fan (got from connecting first).
	public void setState() {
		String fetchedState = Store.get("state", context);
		
		// If state is empty, application could not connect to the controller.
		if(fetchedState.equals("empty")) {
			Intent mainIntent = new Intent(this.context, MainActivity.class);
			this.context.startActivity(mainIntent);
			return;
		}
		
		this.state = Integer.parseInt(fetchedState);
	}
	
	// Sets the state.
	public void setState(int state) {
		this.state = state;
	}

	// Sets the controller server we need to connect to.
	public void setControllerServer() {
		String connectedServer = Store.get("saved_ip", this.context);
		if(! Pattern.matches("^http://.*", connectedServer))
			connectedServer = "http://" + connectedServer;
		
		this.controllerServer = connectedServer;
	}
	
	
	/* ________________ Wrappers _____________ */
	public void turnOn() {
		this.changeState("ON");
	}
	
	public void turnOff() {
		this.changeState("OFF");
	}
	
	public void toggle() {
		this.changeState("TOGGLE");
	}
	/* ________________ Wrappers _____________ */
	
	
	// Method that actually changes the state of the fan.
	public void changeState(String action) {
		
		Fan.this.triedAction = action;
		RequestQueue q = Volley.newRequestQueue(this.context);
		
		// Set the payload for the request.
		JSONObject payload = new JSONObject();
		try {
			payload = new JSONObject("{action: \"" + action + "\"}");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Create the request.
		JsonObjectRequest req = new JsonObjectRequest(
			this.controllerServer + "/action",
			payload,
			new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject data) {
					if(data.has("state")) {
						try {
							Fan.this.prevState = Fan.this.state;
							Fan.this.setState( 
								Integer.parseInt(data.getString("state"))
							);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						}
						/**
						 * Do things we should do after chaning the fan state successfully.
						 */
						Fan.this.afterStateChange();
						return;
					}
								
				}
			},
			new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError arg0) {
					// Left it for now.
				}
			}
		);
		
		q.add(req);
	}
	
	// Handles any updates that should be done after changing the state of the fan.
	public void afterStateChange() {
		
		// It is safe to assume that the tried action has been performed right.
		switch(this.triedAction) {
			case "ON": 
				this.context.fanTurnedOn(); break;
			case "OFF":
				this.context.fanTurnedOff(); break;
			case "TOGGLE":
				this.context.fanToggled(this.state);
				
		}
		
	}
	
}
