package Main;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import model.Hash;

public class TransactionProof {

	private Hash hash;
	public String solution = null; //TODO private
	
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

//	public static void main(String[] args) {
//		TransactionProof bp = new TransactionProof(new Transaction("asdg", "lukas", 3).hashCodeSHA());
//		bp.solve();
//	}
	
	public void solve() {
		
		long timeStart = System.currentTimeMillis();
		
//		int iterations = 0;
		
		String guess;
		String solution;
		String randomStr;
		
		do {
		
		randomStr = generateRandomString();
		
		guess = randomStr + hash;

//		System.out.println(guess);
		
		solution = hash(guess);
		
//		System.out.println(guess);
		
//		iterations++;
		
		}while(!solution.startsWith(POW_CRITERIA));
		
		long timeEnd = System.currentTimeMillis();
		
		//TODO DEV
		System.out.println("Solution found, time: " + ((timeEnd-timeStart)/1000) + " seconds");
//		System.out.println("Solution found, iterations: " + iterations + ", seconds: " + ((timeEnd-timeStart)/1000));
//		System.out.println("Solution: " + guess);
//		System.out.println("Hashed Result: " + solution);
		
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
