package representative;

import java.io.IOException;

import org.pmw.tinylog.Logger;

import model.Hash;
import model.HexString;
import network.ObjectSerializer;
import newMain.CryptoUtil;
import newMain.RI;
import sharded.ShardingLevel;
import sharded.ShardingLevelTransaction;
import sharded.ShardingTransactionCreator;

public class RepresentativeHashCommitter {

	int level, epochNum;
	RI ri;
	//String hash;
	
	public RepresentativeHashCommitter(int level, RI ri, int epochNum) {
		super();
		this.level = level;
		this.ri = ri;
		this.epochNum = epochNum;
		Logger.debug("init");
	}
	
	public void commit(String hash){
		
		ShardingLevel l = ri.getShardingLevel(level);
		
		HexString sig = CryptoUtil.sign(ri.getKeyPair().getPrivate(), ri.getKeyPair().getPublic(), Hash.fromHashString(hash).getHash());
		
		//tx hash shardId epochNum pkRep signature (+ PoW)
		
		//String msg = "tx " + hash + " " + l.id + " " + epochNum + " " + ri.getPublicKey().getHashString() + " " + sig.getHashString();
		
		ShardingLevelTransaction t = new ShardingTransactionCreator(ri.getPublicKey(), epochNum, hash, l)
									.setConfirmed()
									.solveProof()
									.sign(ri.getKeyPair())
									.create();
		
		Logger.debug("Created ShardingTx: " + new ObjectSerializer().serialize(t));
		
		String msg = "tx " + l.id + " " + new ObjectSerializer().serialize(t);
		
		try {
			Thread.sleep(3000L);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		l.getNeighborPool().broadcast(msg);
		try {
			l.getResponsers().acceptResponse(null, msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
