package cs.tcd.ie;

import java.io.IOException;
import java.util.TimerTask;

public class ACKTaskTimer extends TimerTask {
	
	Client c;
	
	public ACKTaskTimer(Client c) {
		super();
		this.c = c;
	}
	
	public void run() {
		
		c.check();
	}
	
	

}
