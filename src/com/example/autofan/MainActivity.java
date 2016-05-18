package com.example.autofan;

import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.autofan.dialogs.ConnectDialog;
import com.example.autofan.storage.Store;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

	private ConnectDialog connectDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle("AutoFan");
		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
	    setSupportActionBar(myToolbar);
	    
	    // Check if the controller IP is configured.
	    //checkIPSet();
	    setIP();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void ensureIPSet() {
		String IP = Store.get("IP", this);
		if(IP.equals("empty")) {
			
		}
	}
	
	public void saveIP() {
		TextView ipText = (TextView) this.findViewById(R.id.editText1);
		Store.put("saved_ip", ipText.getText().toString(), this);
	}
	
	public void setIP() {
		String savedIp = Store.get("saved_ip", this);
		if(savedIp == "empty") return;
		
		TextView ipText = (TextView) this.findViewById(R.id.editText1);
		ipText.setText(savedIp);
	}
	
	public String getHost() {
		TextView ipText = (TextView) this.findViewById(R.id.editText1);
		String host = ipText.getText().toString();
		
		if(Pattern.matches("^http://.*", host)) return host;
		
		return "http://" + host;
	}
	
	public boolean enteredIP() {
		TextView ipText = (TextView) this.findViewById(R.id.editText1);
		if(ipText.getText().toString().isEmpty()) return false;
		
		return true;
	}
	
	/* Event handlers for view component. */
	public void connect(View view) {
		
		if(! enteredIP()) {
			View container = MainActivity.this.findViewById(R.id.container);
			Snackbar snack = Snackbar.make(container,
				"Enter the host IP to connect to.", 
				Snackbar.LENGTH_LONG
			);
			snack.show();
			return;
		}
		//connectDialog = new ConnectDialog();
		//connectDialog.show(this.getSupportFragmentManager(), "connect_message");
		// Test server.
		RequestQueue q = Volley.newRequestQueue(this);
		String url = this.getHost() + "/state";
		
		//new JsonObjectRequest();
		JsonObjectRequest jsonReq = new JsonObjectRequest(
			Request.Method.GET, url, new Response.Listener<JSONObject>() {

				@Override
				public void onResponse(JSONObject data) {
					String message = "";
					
					if(data.has("state")) {
						message = "Connected to controller.";
						MainActivity.this.saveIP();
						try {
							Store.put("state", data.getString("state"), MainActivity.this);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
						startActivity(homeIntent);
					} else
						message = "Got an unexpected response from controller.";
					
					View container = MainActivity.this.findViewById(R.id.container);
					Snackbar snack = Snackbar.make(container,
						message, 
						Snackbar.LENGTH_LONG
					);
					snack.show();
				}
			},
			new Response.ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError error) {
					String errorMessage = error.getMessage();
					if(errorMessage == null)
						errorMessage = "Could not connect to the server. Make sure that the host address is correct.";
					
					View container = MainActivity.this.findViewById(R.id.container);
					Snackbar snack = Snackbar.make(container, errorMessage, Snackbar.LENGTH_LONG );
					snack.show();
					
				}
			}
		);
		q.add(jsonReq);	
		
	}
	
}
