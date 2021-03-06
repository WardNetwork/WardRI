package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import newMain.Transaction;
import newMain.TransactionReference;
import keys.KeyStore;
import model.Hash;
import model.HexString;

public class ObjectSerializer
{
    final String fieldDelimiter = " ";
    final String listDelimiter = ",";
    
    public String serialize(Hash h) {
        return h.getHashString();
    }
    
    public Hash parseHash(String s) {
        return Hash.fromHashString(s);
    }
    
    public String serialize(Transaction t) {
    	
        final List<String> str = new ArrayList<String>();
        
        str.add(t.getSender().toString());
        str.add(t.getReciever().toString());
        str.add(t.getValue() + "");
        str.add(t.getCreatedTimestamp() + "");
        
        if(t.getPowProof() == null || t.getPowProof().getSolution() == null) {
        	System.out.println("Transactionproof is null");
        }
        
        str.add(t.getPowProof().getSolution());
        
        str.add(t.getSignature().getHashString());
        
        for (TransactionReference confirmed : t.getConfirmed().stream().sorted(TransactionReference.getComparator()).collect(Collectors.toList())) {
            
            str.add(confirmed.getTxId().getHashString());
            
        }
        
        return String.join(" ", str);
    }
    
    public Transaction parseTransaction(String s) {
    	try{
	        String[] tokenized = s.split(" ");
	        
	        if(tokenized[2].startsWith("0x")){
	        	System.out.println(s);
	        }
	        
	        HexString sender = HexString.fromHashString(tokenized[0]);
	        HexString reciever = HexString.fromHashString(tokenized[1]);
	        double value = Double.parseDouble(tokenized[2]);
	        long createdTimestamp = Long.parseLong(tokenized[3]);
	        
	        String powSolution = tokenized[4];
	        HexString signature = HexString.fromHashString(tokenized[5]);
	        Set<TransactionReference> confirmed = new HashSet<>();
	        for (int i = 6; i < tokenized.length; ++i) {
	            String confirmation = tokenized[i];
	            confirmed.add(new TransactionReference(Hash.fromHashString(confirmation)));
	        }
	        
	        Transaction t = new Transaction(sender, reciever, value, null /* TODO */, createdTimestamp, powSolution, signature, confirmed);
	        
	        return t;
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return null;
    }
    
    public <K, V> Map<K, V> parseMap(final String s, Function<String, K> keyConverter, Function<String, V> valueConverter) {
        if (keyConverter == null) {
            keyConverter = (x -> (K)x);
        }
        if (valueConverter == null) {
            valueConverter = (x -> (V)x);
        }
        final String[] split = s.split(",");
        final Map<K, V> map = new HashMap<K, V>();
        //for (int length = (array = split).length, i = 0; i < length; ++i) {
        for (int i = 0; i < split.length; i++) {
            final String entry = split[i];
            final String[] splitEntry = entry.split(" ");
            map.put(keyConverter.apply(splitEntry[0]), valueConverter.apply(splitEntry[1]));
        }
        return map;
    }
    
    public <K, V> String serialize(final Map<K, V> map) {
        final List<String> ret = new ArrayList<String>();
        for (final Map.Entry<K, V> entry : map.entrySet()) {
            ret.add(entry.getKey() + " " + entry.getValue());
        }
        return String.join(",", ret);
    }
}
