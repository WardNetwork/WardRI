package Main;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import model.Hash;

public class TransactionProof {

	private Hash hash;
	private String solution = null;
	
	public static final String POW_CRITERIA = ""; //TODO DEV: "0000"
	
	public TransactionProof(Hash hash) {
		
		this.hash = hash;
		
	}
	
	public TransactionProof(Hash hash, String solution) {
		this(hash);
		this.solution = solution;
	}
	
	public String getSolution() {
		return solution;
	}
	
	public void solve() {
		
		long timeStart = System.currentTimeMillis();
		
		String guess;
		String solution;
		String randomStr;
		
		do {
		
			randomStr = generateRandomString();
			
			guess = randomStr + hash;
			
			solution = hash(guess);
		
		}while(!solution.startsWith(POW_CRITERIA));
		
		long timeEnd = System.currentTimeMillis();
		
		System.out.println("Solution found, time: " + ((timeEnd-timeStart)/1000) + " seconds");
		
		this.solution = randomStr;
		
		
	}
	
	public Hash getHash(){
		return hash;
	}
	
	public String getResult() {
		return hash(solution + hash);
	}
	
	public boolean validateProof(){
		return getResult().startsWith(POW_CRITERIA);
	}
	
	private String generateRandomString() {
		
		Random r = new Random();
		String s = "";
		
		for(int i = 0 ; i < 30/3 ; i++) {
			
			s += (char)(r.nextInt(26) + 'a');
			s += (char)(r.nextInt(26) + 'a');
			s += r.nextInt(10);
			
		}
		
		return s;
		
	}

	public static String hash(String s) {
		
		try {
			
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			
			digest.update(s.getBytes());
			byte[] arr = digest.digest();
			
			StringBuffer stringBuffer = new StringBuffer();
	        for (int i = 0; i < arr.length; i++) {
	            stringBuffer.append(Integer.toString((arr[i] & 0xff) + 0x100, 16)
	                    .substring(1));
	        }
	        return stringBuffer.toString();
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
}
