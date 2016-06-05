package bgu.spl.mics.impl;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
	private int fCurrentTick;
	
	public TickBroadcast(int currentTick){
		fCurrentTick = currentTick;
	}
	
	public int getTick(){
		return fCurrentTick;
	}
}
