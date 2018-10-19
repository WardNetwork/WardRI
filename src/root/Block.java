package root;

import java.util.List;

import model.Hash;
import model.Ledger;

public class Block {
	
	Hash blockHash;
	long epochNum;
	
	List<BlockSignature> signatures;
	Ledger ledgerChanges;
	
	Block previousBlock = null;
	
	public Block(Ledger ledgerChanges, long epochNum, List<BlockSignature> signatures){
		this.ledgerChanges = ledgerChanges;
		this.epochNum = epochNum;
		this.signatures = signatures;
	}
	
	protected void setPreviousBlock(Block previous){
		if(previousBlock == null)
			this.previousBlock = previous;
	}
	
}
