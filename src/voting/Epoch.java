package voting;

import java.util.ArrayList;
import java.util.List;

import sharded.EpochListener;

public class Epoch {
	
	long start;
	long duration;
	long lastStart;
	
	List<EpochListener> listeners;
	Thread t;
	
	public Epoch(long duration){
		start = System.currentTimeMillis();
		this.duration = duration;
		listeners = new ArrayList<>();
	}
	
	public Epoch(long duration, EpochListener listener){
		this(duration);
		listeners.add(listener);
	}
	
	public void addListener(EpochListener listener){
		listeners.add(listener);
	}
	
	public long getBeginningTime(){
		return lastStart;
	}
	
	public void start(){
		
		t = new Thread(() -> {
			lastStart = System.currentTimeMillis();
			while(t.isAlive()){
				
				try {
					Thread.sleep(duration - (System.currentTimeMillis() - lastStart));
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				
				lastStart = System.currentTimeMillis();
				
				long epochIteration = (System.currentTimeMillis() - lastStart) / duration;
				
				listeners.forEach(x -> x.onEpochComplete(epochIteration, this));
				
			}
		});
		
	}
	
}
