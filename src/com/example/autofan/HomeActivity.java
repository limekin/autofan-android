package com.example.autofan;


import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.autofan.interfaces.Fan;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

	private Fan connectedFan;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		Toolbar toolbar = (Toolbar) this.findViewById(R.id.my_toolbar);
		setSupportActionBar(toolbar);
		
		// Initialize the fan.
		this.connectedFan = new Fan(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
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
	
	public void turnOnFan(View view) {
		this.connectedFan.turnOn();
	}
	
	public void turnOffFan(View view) {
		this.connectedFan.turnOff();
	}
	public void toggleFan(View view) {
		this.connectedFan.toggle();
	}
	
	
	public void showSnack(String message) {
		// Hmm.
	}
	
	public void updateState(String currentState) {
		TextView stateText = (TextView) this.findViewById(R.id.current_state);
		stateText.setText(currentState);
	}
	
	/**
	 * ____________ Callbacks that will be called after performing actions. ______ 
	 */
	public void fanTurnedOn() {
		View container = this.findViewById(R.id.container);
		Snackbar snack = Snackbar.make(container,
			"Fan turned on.", 
			Snackbar.LENGTH_LONG
		);
		snack.show();
		
		// Now update the state.
		this.updateState("On");
		
	}
	
	public void fanTurnedOff() {
		
	}
	
	public void fanToggled() {
		
	}
}
