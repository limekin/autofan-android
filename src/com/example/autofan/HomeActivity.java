package com.example.autofan;


import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import org.json.JSONException;

import com.example.autofan.interfaces.Fan;
import com.example.autofan.storage.Store;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

	private Fan connectedFan;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		Toolbar toolbar = (Toolbar) this.findViewById(R.id.my_toolbar);
		setSupportActionBar(toolbar);
		setTitle("AutoFan");
		
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
		int id = item.getItemId();
		
		switch(id) {
			case R.id.action_settings:
				return true;
			case R.id.disconnect:
				this.disconnect(); break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	// Disconnecsts the device from the controller,
	// and sends the user to the main page.
	public void disconnect() {
		// Lets clear the saved ip.
		Store.put("saved_ip", "empty", this);
		
		// Show main activity.
		Intent mainIntent = new Intent(this, MainActivity.class);
		startActivity(mainIntent);
	}
	
	// Handles the click event for speed shifts.
	public void performSpeedShift(View view) {
		String toState = (String) view.getTag();
		try {
			this.connectedFan.shiftSpeed(Integer.parseInt(toState));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	// Handles the click event for UP and DOWN buttons.
	public void performAdjacentShift(View view) {
		try {
			switch(view.getId()) {
				case R.id.button4:
					this.connectedFan.shiftUp(); break;
				case R.id.button5:
					this.connectedFan.shiftDown();
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	// Handles the click event for ON, OFF and TOGGLE buttons.
	public void performStateChange(View buttonView) {
		try {
			switch(buttonView.getId()) { 
				case R.id.button1:	
					this.connectedFan.toggle(); break;
				case R.id.button2:
					this.connectedFan.turnOn(); break;
				case R.id.button3:
					this.connectedFan.turnOff();
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
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
	
	// Changes the status (state ?) of the fan shown on the UI.
	public void updateStateOnView(String currentState) {
		TextView stateText = (TextView) this.findViewById(R.id.current_state);
		stateText.setText(currentState);
	}
	
	// Changes the speed to the current speed.
	public void updateSpeedOnView(int state) {
		RadioGroup speeds  = (RadioGroup) this.findViewById(R.id.radioGroup1);
		for(int i=0; i<speeds.getChildCount(); ++i) {
			RadioButton speed = (RadioButton) speeds.getChildAt(i);
			if(state == i+1) {
				speed.setChecked(true);
				return;
			}
		}
	}
	
	// Changes enabled value of all fan controls except ON, OFF and TOGGLE.
	public void changeEnabled(boolean to) {
		RadioGroup speeds  = (RadioGroup) this.findViewById(R.id.radioGroup1);
		for(int i=0; i<speeds.getChildCount(); ++i) {
			RadioButton speed = (RadioButton) speeds.getChildAt(i);
			speed.setEnabled(to);
		}
		
		Button up = (Button) this.findViewById(R.id.button4);
		Button down = (Button) this.findViewById(R.id.button5);
		up.setEnabled(to);
		down.setEnabled(to);
	}
	
	/**
	 * ____________ Callbacks that will be called after performing actions. ______ 
	 */
	public void fanTurnedOn(int state) {
		this.showSnack("Fan turned on.");
		
		// Now update the state.
		this.updateStateOnView("On");
		// Also update the speed.
		this.updateSpeedOnView(state);
		// Make other controls enabled.
		this.changeEnabled(true);
		
	}
	
	public void fanTurnedOff() {
		this.showSnack("Fan turned off.");
		
		// Now update the state.
		this.updateStateOnView("Off");
		// Make other controls disabled.
		this.changeEnabled(false);
	}
	
	public void fanToggled(int state) {
		// Delegate to turn off or turn on.
		if(state > 0) 
			this.fanTurnedOn(state);
		else 
			this.fanTurnedOff();	
	}
	
	public void fanSideShifted(int toState) {
		this.updateSpeedOnView(toState);
	}
}
