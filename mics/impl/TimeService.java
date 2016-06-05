package bgu.spl.mics.impl;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
public class TimeService extends MicroService {

	private int fSpeed; // milis in each tick
	private int fDuration; // ticks until termination
	private int fCurrentTick;
	private TimerUpdate fTimerTask;
	private Timer fTimer;
	private CountDownLatch fLatchObject;
	private final Logger LOGGER = Logger.getLogger(TimeService.class.getName());
	public TimeService(int speed, int duration, CountDownLatch latchObject) {
		super("timer");
		fSpeed = speed;
		fDuration = duration;
		fCurrentTick = 1;
		fTimerTask = new TimerUpdate();
		fTimer = new Timer();
		fLatchObject = latchObject;
    }
	
	private class TimerUpdate extends TimerTask
	{
		@Override
		public void run() {
			LOGGER.info("Timer tick " + fCurrentTick + " sending broadcast");
			sendBroadcast(new TickBroadcast(fCurrentTick));
			
			if(fCurrentTick == fDuration)
			{
				LOGGER.info("Timer tick duration reached. Sending termination broadcast");
				sendBroadcast(new TerminationBroadcast());
			}
			
			fCurrentTick++;
		}	
	}
	@Override
	protected void initialize() {
		LOGGER.info("Timer init started");
		
		subscribeBroadcast(TerminationBroadcast.class, new Callback<TerminationBroadcast>(){
			@Override
			public void call(TerminationBroadcast newTerminationMessage){
				LOGGER.info(getName() + ": Received TerminationBroadcast. currentTick is " + fCurrentTick + ". Terminating...");
				fTimerTask.cancel();
				fTimer.cancel();
				terminate();
			}
		});
		
		fCurrentTick = 1;
		
		try {
			LOGGER.info("Timer waiting for all threads to finish initialization");
			fLatchObject.await();
			LOGGER.info("All threads initialized. Timer starting broadcast ticks");
		} catch (InterruptedException e) 
		{
			return;
		}
		
		fTimer.schedule(fTimerTask,0, fSpeed);
	}
}
