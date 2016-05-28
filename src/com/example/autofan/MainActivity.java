package com.example.autofan;

import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.autofan.dialogs.SearchDialog;
import com.example.autofan.storage.Store;
import com.example.autofan.tasks.BroadcastTask;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

	private SearchDialog searchDialog;
	private FansAdapter fansAdapter;
	private ArrayList<JSONObject> fans;
	private ListView viewFans;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle("AutoFan");
		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
	    setSupportActionBar(myToolbar);
	    
	    // Fan list initializations.
	    this.fans = new ArrayList<JSONObject>();
	    this.fansAdapter = new FansAdapter(this, fans);
	    this.viewFans = (ListView) this.findViewById(R.id.fanList);
	    this.viewFans.setAdapter(fansAdapter);
	    
	    // Add list item (fan) click listeners.
	    this.addListClickListener();
	    // Initialize the search dialog here.
	    this.searchDialog = new SearchDialog();
	    // Lets start the search.
	    this.startSearch(new View(this));
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
	
	// Sets a listener for handling click on a single fan controller.
	public void addListClickListener() {
		this.viewFans.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Map<String, String> selectedFan = (Map<String, String>) 
						parent.getItemAtPosition(position);
				String controllerAddress = "http://" + 
						selectedFan.get("address") + ":" + 
						selectedFan.get("port");
				Store.put("saved_ip", controllerAddress, MainActivity.this);
				
				Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
				MainActivity.this.startActivity(homeIntent);			}
		});
	}
	
	// Starts searching for the fan controller.
	public void startSearch(View view) {
		// Start broadcasting.
	    new BroadcastTask(this).execute();
	    
	    // Also show the search dialog.
	    searchDialog.show( this.getSupportFragmentManager(), "BURRP");
	}
	
	// Shows a Snackbar message.
	public void showSnack(String message) {
		View container = this.findViewById(R.id.container);
		Snackbar snack = Snackbar.make(
			container,
			message, 
			Snackbar.LENGTH_LONG
		);
		snack.show();
	}

	// Callback that will be called after completing broadcast task.
	public void broadcastCallback(ArrayList<JSONObject> controllers) {
		// Enough with dialog toss it off !
		this.searchDialog.dismiss();
		
		// Clear the current list.
		this.fans.clear();
		for(int i=0; i<controllers.size(); ++i) 
			this.fans.add(controllers.get(i));
		
		if(controllers.isEmpty()) {
			this.showSnack("No fans found on the network.");
		}
		
		// Tell the adapter that fan list have been changed.
		this.fansAdapter.notifyDataSetChanged();
	}
	
	// Returns the controller's address we got frombroadcasting.
	public String getHost() {
		return Store.get("saved_ip", this);
	}
	
	// Simply tries to fetch the state from the found controller server.
	public void connect () {
		// Prepp the request.
		RequestQueue q = Volley.newRequestQueue(this);
		String url = this.getHost() + "/state";
		JsonObjectRequest jsonReq = new JsonObjectRequest(
			Request.Method.GET, url, new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject data) {
					String message = "";
					
					if(data.has("state")) {
						message = "Connected to controller.";
						try {
							Store.put("state", data.getString("state"), MainActivity.this);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
						startActivity(homeIntent);
					} else
						message = "Got an unexpected response from controller.";
					
					MainActivity.this.showSnack(message);
				}
			},
			new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					String errorMessage = error.getMessage();
					if(errorMessage == null)
						errorMessage = "Could not connect to the server. Make sure that the host address is correct.";
					
					MainActivity.this.showSnack(errorMessage);
				}
			}
		);
		
		// Add request to request queue so that it sends it when can.
		q.add(jsonReq);	
	}
	
}
