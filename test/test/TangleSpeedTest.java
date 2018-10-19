package test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import Interfaces.DAGInsertable;
import Interfaces.LocalTangleInterface;
import Main.CommandLineWaiter;
import Main.MainGenesisNode;
import Main.Tangle;
import conf.Configuration;
import model.HexString;
import newMain.Transaction;

public class TangleSpeedTest {

	public static void main(String[] args) throws Exception{
		
		PrintStream o = System.out;
		
		System.setOut(new PrintStream(new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				
			}
		}));
		
		MainGenesisNode.main(args);
		
		Tangle t = MainGenesisNode.t;
		
		final TangleInterfaceDistributor distributor = new TangleInterfaceDistributor(t, new DAGInsertable[] { new LocalTangleInterface(t)/*, new NetworkTangleInterface(t, pool)*/ });
		
		HexString sender = t.configuration.getHexString(Configuration.PUBLICKEY);
		
		HexString reciever = HexString.fromHashString(MainGenesisNode.startReciever);
		
		int transactionsCount = 500000;
//		
//		Transaction genesis = null;//new Transaction("satoshi", "raphael", 100000);
//		
//		t.addTransaction(genesis);
//		for(int i= 0 ; i < 4 ; i++){
//			Transaction t2 = null;//new Transaction("raphael", "satohsi", 1);
//			
////			t.transactions.add(t2);
//			t.transactions.put(t2.getTxHash().getHashString(), t2);
//		
//			t.confirmTransaction(t2, genesis);
//		}
		
		System.err.println("Start");
		
		long start = System.currentTimeMillis();

		for(int i = 0 ; i < transactionsCount ; i++){
			
			distributor.addTransaction(new Transaction(sender, reciever, 1));
			
			if(i % 100 == 0){
				System.err.println("Tick");
			}
		}
		
		long timeDiff = System.currentTimeMillis() - start;
		
		System.err.println("Time for " + transactionsCount + " Txs: " + timeDiff/1000D);
		
		System.err.println("Tps: " + transactionsCount/((double)timeDiff/1000D));
		
		System.setOut(o);
		
		CommandLineWaiter.startCommandLineInput(t, null, null);
		
	}
	
}
