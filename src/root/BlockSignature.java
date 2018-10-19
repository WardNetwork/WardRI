package root;

import model.HexString;
import model.Ledger;
import newMain.CryptoUtil;

public class BlockSignature {

	public HexString signature;
	public HexString publicKey;
	
	public BlockSignature(HexString publicKey, HexString signature) {
		super();
		this.signature = signature;
		this.publicKey = publicKey;
	}

	public boolean validate(Ledger changes){
		
		byte[] bytes = CryptoUtil.hashSHA256(changes.toByteArray());
		boolean valid = CryptoUtil.validateSignature(signature, CryptoUtil.publicKeyFromString(publicKey.getHashString()), bytes);
		return valid;
		
	}
	
}
