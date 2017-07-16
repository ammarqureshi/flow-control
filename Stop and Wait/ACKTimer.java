package cs.tcd.ie;

import java.util.Timer;
import java.util.TimerTask;

public class ACKTimer {
	
	Timer t;
	Client c;
	
	public ACKTimer(int msec, Client c) {
		
		this.c = c;
		t = new Timer();
		t.scheduleAtFixedRate(new ACKTaskTimer(c), msec, msec);	
	}

	

}