package test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Collectors;

import org.rpanic.ListenerThread;
import org.rpanic.NeighborPool;
import org.rpanic.NeighborRequestReponse;
import org.rpanic.TCPNeighbor;

import Interfaces.LocalTangleInterface;
import Interfaces.NetworkDAG;
import Interfaces.TangleInterfaceDistributor;
import Main.MainGenesisNode;
import Main.Tangle;
import Main.TangleSynchronizer;
import Main.Transaction;
import network.NeighborRequestResponseBuilder;

public class NetworkTangleSpeedTest {

	public static void main(String[] args) throws Exception{
		
		System.out.println("Starting NetworkTangleSpeedTest");
		
		final int transactions = 500;
		
//		System.setOut(new PrintStream(new PipedOutputStream()));
		
		TangleInterfaceDistributor gen = mainGenesis();
		
//		TangleInterfaceDistributor cl = mainHost();
		
		Thread.sleep(10000l);
		
		//***********************************************
		//TODO Für das Senden eine Queue schreiben, damit die Transaktionen in Reihenfolge reinkommen
		
		//TODO Eventuell auch eine Error Handling sache schreiben, falls es im Netzwerk doch einmal auftritt
		//***********************************************
		
//		Map<String, Transaction> txmap = ((LocalTangleInterface)gen.getInterfaceByName(LocalTangleInterface.class.getSimpleName())).tangle.transactions;
		
		for(int i = 0 ; i < transactions / 2 ; i++) {
			
			Transaction t = null;//new Transaction("raphael", "test", 1);
			
//			Transaction t2 = new Transaction("raphael", "test2", 1);
			
//			gen.addTranscation(t);
			gen.getInterfaceByName(LocalTangleInterface.class.getSimpleName()).addTranscation(t);
			
//			synchronized (txmap) {
//				for(String s : txmap.keySet()){
//					if(txmap.get(s).equals(t)){
//						txmap.remove(s);
//					}
//				}
//			}
			
			
//			System.out.println(ta.validateTangle() + " " + ta.transactions.size());
			
//			cl.addTranscation(t2);
			
//			Thread.sleep(100L);
			
		}
		System.err.println("Transactions calculated");
		
		long start = System.currentTimeMillis();
		
		for(Transaction t : ((LocalTangleInterface)gen.getInterfaceByName(LocalTangleInterface.class.getSimpleName())).tangle.transactions.values().stream().filter(x -> x.getSender().equals("raphael") && x.getReciever().equals("test")).collect(Collectors.toList())){
			
			gen.getInterfaceByName(NetworkDAG.class.getSimpleName()).addTranscation(t);

			Thread.sleep(3L);
		}
		
		System.err.println("Transactions sent");
		
		while(gen.getInterfaceByName(LocalTangleInterface.class.getSimpleName()).createLedger().getBalance(null/*"test"*/) < ((transactions/2) * 0.97)){// ||
//			cl.getInterfaceByName(LocalTangleInterface.class.getSimpleName()).createLedger().get("test2") < ((transactions/2) * 0.96) ) {
			
			
			Thread.sleep(10L);
		}
		
		long timediff = System.currentTimeMillis() - start;
		
		System.err.println("Time in Seconds: " + timediff/1000 + " (" + timediff + ")");
		
		double tps = gen.getInterfaceByName(LocalTangleInterface.class.getSimpleName()).createLedger().getBalance(null/*"test"*/);

		System.err.println("Total transactions: " + tps);
		
		tps = tps / timediff * 1000;
		
		System.err.println("Tps in Network: " + tps);
		
	}
	
	static Tangle ta;
	
	public static TangleInterfaceDistributor mainHost() throws UnknownHostException, InterruptedException {
		
		int selfPortI = 1338;
		
		System.out.println("Starting Node on Port " + selfPortI);
		
		TCPNeighbor entry = new TCPNeighbor(InetAddress.getByName("127.0.0.1"));
		entry.setPort(1337);
		
		TCPNeighbor selfN = new TCPNeighbor(InetAddress.getByName("127.0.0.1"));
		selfN.setPort(selfPortI);
		
		Tangle tangle = new Tangle();
		
//		TangleVisualizer visualizer = new TangleVisualizer(tangle);
		
		NeighborPool pool = new NeighborPool(entry, selfN, selfN.getPort());
		
		NeighborRequestReponse responser = NeighborRequestResponseBuilder.buildNewDefault(tangle, pool);
		
		ListenerThread.startListeningThreadTcp(selfN.getPort(), responser);
		
		pool.init();
		
		Thread.sleep(5000L);
		
		new TangleSynchronizer(tangle, entry, pool).synchronize();
		
		TangleInterfaceDistributor distr = new TangleInterfaceDistributor(tangle);
		
		distr.addTangleInterface(new LocalTangleInterface(tangle));
		distr.addTangleInterface(new NetworkDAG(tangle, pool));
		
		return distr;
		
	}
	
	public static TangleInterfaceDistributor mainGenesis() throws UnknownHostException {
		
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
		
	}
	
}
