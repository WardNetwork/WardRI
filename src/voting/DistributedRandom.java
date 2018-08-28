package voting;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;

import model.Hash;

public class DistributedRandom {
	
	/**
	 * @param weights - Weights has to be sorted correctly!
	 */
	public static <E> E random(Hash seed, Map<E, Double> weights){
		
		SecureRandom r = new SecureRandom(seed.getHash());
		
		double sumWeights = weights.values().stream().mapToDouble(x -> x).sum();
		
		double res = r.nextDouble() * sumWeights;

		//Find selected One
		
		Iterator<E> iterator = weights.keySet().stream().iterator();
		
		E temp = iterator.next();
		
		for(double d = weights.get(temp) ; d <= sumWeights + weights.get(temp); d += weights.get(temp)){
			
			if(d >= res){
				
				return temp;
			}
			
			temp = iterator.next();
		}
		
		return null;
	}
	
}
