package sharded;

import java.security.KeyPair;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import Main.TangleAlgorithms;
import Main.TransactionProof;
import model.Hash;
import model.HexString;
import newMain.CryptoUtil;
import newMain.DAGObjectReference;
import newMain.Hashable;

public class ShardingTransactionCreator implements Hashable{

	HexString sender;
	long epochNum;
	ShardingLevel level;
	Object data;
	
	HexString signature;
	long createdTimestamp;
	Hash TxId;
	
	TransactionProof proof;
	
	Set<DAGObjectReference<ShardingLevelTransaction>> confirmed;
	
	public ShardingTransactionCreator(HexString sender, long epochNum, Object data, ShardingLevel level){
		createdTimestamp = System.currentTimeMillis();
		this.sender = sender;
		this.epochNum = epochNum;
		this.level = level;
		this.data = data;
	}

	public ShardingLevelTransaction create(){

		return new ShardingLevelTransaction(sender, data, createdTimestamp, epochNum, proof.getSolution(), signature, confirmed);
		
	}
	
	public ShardingTransactionCreator setConfirmed(){
		
		confirmed = electTransactions().stream().map(x -> new DAGObjectReference<>(x)).collect(Collectors.toSet());
		this.TxId = CryptoUtil.hashSHA256(createHashString());
		
		Logger.error("Elected ShardingLevel for " + this.TxId.getHashString() + ": " + confirmed.stream().map(x -> x.getTxId().getHashString()).reduce((x, y) -> x + ", " + y).orElse("none"));
		
		return this;
	}
	
	public ShardingTransactionCreator sign(KeyPair keypair){
		
		Hash txId = CryptoUtil.hashSHA256(createHashString());
		
		HexString signature = CryptoUtil.sign(keypair.getPrivate(), keypair.getPublic(), txId.getHash());
		
		this.signature = signature;
		
		return this;
	}
	
	public ShardingTransactionCreator solveProof(){
			
		this.proof = new TransactionProof(this.TxId);
		this.proof.solve();
		
		return this;
	}
	
	public Set<ShardingLevelTransaction> electTransactions(){
		
		Set<ShardingLevelTransaction> elected = new HashSet<>();
		
		List<ShardingLevelTransaction> list = level.getLocalDAG().getGenericDAG().getTransactionList();
		
		if (list.size() <= 2) {
            elected.addAll(list);
            return elected;
        }
		ShardingLevelTransaction temp = null;
        for (int i = 0; i < 100 && elected.size() < 2; ++i){
        	
            temp = TangleAlgorithms.electShardedConfirmationTx(level.getLocalDAG().getGenericDAG());
            
            if(elected.contains(temp)){
            	continue;
            }
            
            elected.add(temp);
        	
        }
        
        return elected;
        
	}
	
	public String createHashString(){
		String s = String.valueOf(this.createdTimestamp) + this.sender + this.data;
	    
		for(DAGObjectReference<ShardingLevelTransaction> h : confirmed.stream().sorted(DAGObjectReference.getComparator()).collect(Collectors.toList())){
			s += h.getTxId().getHashString();
		}
		
		return s;
	}
	
}
