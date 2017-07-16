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
		
		//System.out.println("Hello I am timer.. About to do something");
		try {
			c.check();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}
