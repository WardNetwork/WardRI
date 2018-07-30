package newMain;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import model.Hash;
import model.HexString;

public class CryptoUtil {

	public static Hash hashSHA256(String object){
    	try {
            
        	MessageDigest digest = MessageDigest.getInstance("SHA-256");
        	
            digest.update(object.getBytes());
            byte[] arr = digest.digest();
            Hash hash = new Hash(arr);
            return hash;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
	}
	
	public static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";
	
	public static final String ENCODING_ALGORITHM = "EC";
    
    public static HexString sign(PrivateKey privateKey, PublicKey publicKey, byte[] data) {
        try {
            Signature dsa = Signature.getInstance(SIGNATURE_ALGORITHM);
            dsa.initSign(privateKey);
            dsa.update(data);
            byte[] signature = dsa.sign();
            return new HexString(signature);
        }
        catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean validateSignature(HexString signature, PublicKey publicKey, byte[] data) { //TODO integrieren
        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(data);
            boolean verified = sig.verify(signature.getHash());
            return verified;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static PublicKey publicKeyFromString(String pubKey){

        byte[] encodedPublicKey = HexString.fromHashString(pubKey).getHash();
        
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ENCODING_ALGORITHM);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            return publicKey;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }
    
    public static PrivateKey privateKeyFromString(String privKey){

        byte[] encodedPrivateKey = HexString.fromHashString(privKey).getHash();
        
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ENCODING_ALGORITHM);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            return privateKey;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static KeyPair keypairFromStrings(String publicK, String privateK) {
		
        byte[] encodedPublicKey = HexString.fromHashString(publicK).getHash();
        byte[] encodedPrivateKey = HexString.fromHashString(privateK).getHash();
        
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ENCODING_ALGORITHM);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            return new KeyPair(publicKey, privateKey);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	
}
