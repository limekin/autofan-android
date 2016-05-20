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
		this.triedAction = "NONE";
		
		this.setState();
		this.setControllerServer();
	}
	
	// Sets the current state of the fan (got from connecting first).
	private void setState() {
		String fetchedState = Store.get("state", context);
		
		// If state is empty, application could not connect to the controller.
		if(fetchedState.equals("empty")) {
			Intent mainIntent = new Intent(this.context, MainActivity.class);
			this.context.startActivity(mainIntent);
			return;
		}
		
		this.state = Integer.parseInt(fetchedState);
		this.afterStateChange();
	}
	
	// Sets the state.
	public void setState(int state) {
		this.state = state;
		
		// DO anything we should after a state change.
		this.afterStateChange();
	}

	// Sets the controller server we need to connect to.
	private void setControllerServer() {
		String connectedServer = Store.get("saved_ip", this.context);
		if(! Pattern.matches("^http://.*", connectedServer))
			connectedServer = "http://" + connectedServer;
		
		this.controllerServer = connectedServer;
	}
	
	
	/* ________________ Wrappers (Sugars ?) _____________ */
	public void turnOn() throws JSONException {
		JSONObject actionData = new JSONObject();
		actionData.put("action", "ON");
		this.sendAction(actionData);
	}
	
	public void turnOff() throws JSONException {
		JSONObject actionData = new JSONObject();
		actionData.put("action", "OFF");
		this.sendAction(actionData);
	}
	
	public void toggle() throws JSONException {
		JSONObject actionData = new JSONObject();
		actionData.put("action", "TOGGLE");
		this.sendAction(actionData);
	}
	
	public void shiftSpeed(int state) throws JSONException {
		JSONObject actionData = new JSONObject();
		actionData.put("action", "SHIFT");
		actionData.put("state", state);
		this.sendAction(actionData);
	}
	
	public void shiftUp() throws JSONException {
		JSONObject actionData = new JSONObject();
		actionData.put("action", "UP");
		this.sendAction(actionData);
	}
	
	public void shiftDown() throws JSONException {
		JSONObject actionData = new JSONObject();
		actionData.put("action", "DOWN");
		this.sendAction(actionData);
	}
	/* ________________ Wrappers _____________ */
	
	
	// Method that actually changes the state of the fan.
	private void sendAction(JSONObject actionData) {
		
		try {
			Fan.this.triedAction = actionData.getString("action");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		RequestQueue q = Volley.newRequestQueue(this.context);
		
		// Create the request.
		JsonObjectRequest req = new JsonObjectRequest(
			this.controllerServer + "/action",
			actionData,
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
	private void afterStateChange() {
		
		// It is safe to assume that the tried action has been performed right.
		switch(this.triedAction) {
			case "ON": 
				this.context.fanTurnedOn(this.state); break;
			case "OFF":
				this.context.fanTurnedOff(); break;
			case "TOGGLE":
				this.context.fanToggled(this.state);
			case "SHIFT": 
				// Nothing to do.
				break;
			case "UP":
			case "DOWN":
				this.context.fanSideShifted(this.state); break;
			// We just fetched the current state of the fan.
			case "NONE":
				if(this.state > 0)
					this.context.fanTurnedOn(this.state);
				else
					this.context.fanTurnedOff();
				
		}
		
	}
	
}
