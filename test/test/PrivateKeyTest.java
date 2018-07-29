package test;

import keys.KeyStore;

public class PrivateKeyTest {

	public static void main(String[] args) {
		
//		KeyPair kp = 
		
		System.out.println("Private: " + KeyStore.getPrivateString());
		System.out.println("Public: " + KeyStore.getPublicString());
		
//		KeyPair kp2 = KeyStore.generateKeyPair();
		
//		Transaction t = new Transaction(, "rec", 1);
		
//		t.sign(kp.getPrivate());
		
//		byte[] sign = t.getSignature();
		
//		System.out.println(t.validateSignature(kp.getPublic()));
		
//		System.out.println(t.validateSignature(kp2.getPublic()));
		
	}
	
}
