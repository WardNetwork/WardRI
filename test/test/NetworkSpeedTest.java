package test;

import java.util.ArrayList;
import java.util.List;

import conf.Configuration;
import keys.Base62;
import model.HexString;
import newMain.RI;
import newMain.Transaction;
import newMain.TxCreator;
import newMain.TxInserter;
import sync.LocalDAGSynchronizer;

public class NetworkSpeedTest {

	public static void main(String[] args) throws Exception{
		
		System.out.println("Starting NetworkTangleSpeedTest");
		
		//System.setOut(new PrintStream(new PipedOutputStream()));
		
		//Thread.sleep(10000l);
		
		//***********************************************
		//DONE Für das Senden eine Queue schreiben, damit die Transaktionen in Reihenfolge reinkommen
		
		//DONE Eventuell auch eine Error Handling sache schreiben, falls es im Netzwerk doch einmal auftritt
		//***********************************************
		
		List<RI> ris = new ArrayList<>();
		
		for(int i = 0 ; i < 1 ; i++){
			
			Configuration conf = new Configuration();
			conf.put(Configuration.SELF, "127.0.0.1");
			conf.put(Configuration.SELFPORT, 1340+(i * 2));
			conf.put(Configuration.NEIGHBOR, "127.0.0.1:" + (i == 0 ? 1337 : 1339 + i));
			
			RI ri = mainHost(conf);
			ris.add(ri);
			
		}

		Thread.sleep(2000L);
		
		System.err.println("Starting bandwith test");
		
		long start = System.currentTimeMillis();
		
		RI ri = ris.get(ris.size()-1);
		
		final int transactions = 100;
		
		List<Transaction> txs = new ArrayList<>();
		
		for(int i = 0 ; i < transactions ; i++){
			
	        Transaction t = new TxCreator(ri).create(HexString.fromHashString(Base62.fromBase62("6JsvOCaQRhQR57R2l6ycqgloRH2bZ0secRNWIdVwb2MfqUnbgNsqRTUeUz6RnXLV1vUY26").getBase16()), 0d, null);
	        
	        txs.add(t);
	        
	        Thread.sleep(1L);
	        
		}
		
		System.err.println("Creation completed: " + (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		
		for(Transaction t : txs){
	        new TxInserter(ri).insert(t);
		}
		
		
		System.err.println("Bandwith test end");
		
		
		
		long timediff = System.currentTimeMillis() - start;
		
		System.err.println("Time in Seconds: " + timediff/1000 + " (" + timediff + ")");
		
		double tps = transactions;//gen.getInterfaceByName(LocalTangleInterface.class.getSimpleName()).createLedger().getBalance(null/*"test"*/);

		System.err.println("Total transactions: " + tps);
		
		tps = tps / timediff * 1000;
		
		System.err.println("Tps in Network: " + tps);
		
	}
	
	//static Tangle ta;
	
	public static RI mainHost(Configuration conf){
		
		RI ri = new RI();
        ri.init(conf);
        
        try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        //new LocalDAGSynchronizer(ri, ri.getShardedPool().getRandomNeighbor(), ri.getShardedPool()).synchronize();
        //CommandLineWaiter.startCommandLineInput(ri, visualizer, ri.getShardedPool());
        
        return ri;
		
	}
	
	/*public static TangleInterfaceDistributor mainGenesis() throws UnknownHostException {
		
		int selfPortI = 1337;
		
		System.out.println("Starting GenesisNode on Port " + selfPortI);
		
		TCPNeighbor selfN = new TCPNeighbor(InetAddress.getByName("127.0.0.1"));
		selfN.setPort(selfPortI);
		
		Tangle tangle = new Tangle();
		ta = tangle;
//		TangleVisualizer visualizer = new TangleVisualizer(tangle);
		
		NeighborPool pool = new NeighborPool(null, selfN, selfN.getPort());

		NeighborRequestReponse responser = NeighborRequestResponseBuilder.buildNewDefault(tangle, pool);
		
		ListenerThread.startListeningThreadTcp(selfN.getPort(), responser);
		
		pool.init();
		
		System.out.println("Genesisnode successfully initialized");
		
		MainGenesisNode.genesisTranscation(tangle, pool, null, null);
		
		TangleInterfaceDistributor distr = new TangleInterfaceDistributor(tangle);
		
		distr.addTangleInterface(new LocalTangleInterface(tangle));
		distr.addTangleInterface(new NetworkDAG(tangle, pool));
		
		return distr;
		
	}*/
	
}
