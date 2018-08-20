package voting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Epoch {
	
	long start;
	long duration;
	
	List<Consumer<Long>> listeners;
	Thread t;
	
	public Epoch(long duration){
		start = System.currentTimeMillis();
		this.duration = duration;
		listeners = new ArrayList<>();
	}
	
	public Epoch(long duration, Consumer<Long> listener){
		this(duration);
		listeners.add(listener);
	}
	
	public void addListener(Consumer<Long> listener){
		listeners.add(listener);
	}
	
	public void start(){
		
		t = new Thread(() -> {
			long start = System.currentTimeMillis();
			while(t.isAlive()){
				
				try {
					Thread.sleep(duration - (System.currentTimeMillis() - start));
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				
				start = System.currentTimeMillis();
				
				long epochIteration = (System.currentTimeMillis() - start) / duration;
				
				listeners.forEach(x -> x.accept(epochIteration));
				
			}
		});
		
	}
	
}
