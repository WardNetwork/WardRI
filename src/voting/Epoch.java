package voting;

import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import sharded.EpochListener;

public class Epoch {
	
	public static long DEFAULT_DURATION = 10000;
	
	long start;
	long duration;
	long lastStart;
	
	List<EpochListener> listeners;
	Thread t;
	
	public Epoch(long start, long duration){
		this.start = start;
		this.duration = duration;
		listeners = new ArrayList<>();
	}
	
	@Deprecated
	public Epoch(long duration){
		this(System.currentTimeMillis(), duration);
	}

	@Deprecated
	public Epoch(long duration, EpochListener listener){
		this(duration);
		listeners.add(listener);
	}
	
	public void addListener(EpochListener listener){
		listeners.add(listener);
	}
	
	public long getStartTime(){
		return start;
	}
	
	public long getBeginningTime(long epochNum){
		return start + (duration * epochNum);
	}
	
	public long getEndTime(long epochNum){
		return start + (duration * (epochNum+1))-1;
	}
	
	public void start(){
		
		Logger.debug("Epoch Start: " + System.currentTimeMillis());
		
		t = new Thread(() -> {
			lastStart = System.currentTimeMillis();
			long startOverhead = (System.currentTimeMillis() - start) % duration;
			lastStart -= startOverhead;
			while(t.isAlive()){
				
				try {
					long sleepTime = duration - (System.currentTimeMillis() - lastStart) - 1;
					if(sleepTime < 0){
						int i;
						for(i = 0 ; sleepTime < 0 ; i++){
							sleepTime += duration;
						}
						Logger.warn(i + " Epochs skipped!");
					}
					Thread.sleep(sleepTime);  
					//TODO Anders implementieren. So, dass das Schlafen relativ zum Start ist, nicht zum letzten, sonst setzen sich Fehler fort
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				
				lastStart = System.currentTimeMillis();
				
				long epochIteration = Math.round((System.currentTimeMillis() - (double)start) / (double)duration) - 1;

				Logger.debug("New Epoch! " + epochIteration + " at " + System.currentTimeMillis());
				listeners.forEach(x -> x.onEpochComplete(epochIteration, this));
				
			}
		});
		t.start();
		
	}
	
}
