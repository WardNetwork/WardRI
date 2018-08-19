package Main;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.rpanic.GroupedNeighborPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Interfaces.VisualizerTangleInterface;
import keys.Base62;
import model.HexString;
import model.Ledger;
import newMain.DAG;
import newMain.RI;
import newMain.Transaction;
import newMain.TxCreator;
import newMain.TxInserter;

public class CommandLineWaiter
{
	static Scanner sc;
	
	static Logger log = LoggerFactory.getLogger(CommandLineWaiter.class);
	
    public static void startCommandLineInput(RI ri, TangleVisualizer visualizer, GroupedNeighborPool pool) {
        
    	
        //TransactionIssuer issuer = new TransactionIssuer(t, distr, KeyStore.getPrivateKey(), KeyStore.getPublicKey());
        
        sc = new Scanner(System.in);
        
        
        while (true) {
            final String s = sc.nextLine();
            try {
                if (s.startsWith("tx")) {
                	
                    String[] arr = s.split(" ");
                    
                    HexString reciever = HexString.fromString(arr[1]);
                    Transaction trans = new TxCreator(ri).create(reciever, Integer.parseInt(arr[2]), null);
                    
                    new TxInserter(ri).insert(trans);
                    
                }
            	if(s.startsWith("st")){ //Status
            		
            		System.out.println(ri.getDAG().getTransactionList().size() + " transactions: " + ri.getDAG().getTransactionList().toString());
            		
            	}
                else if (s.startsWith("sh")) {
                	
                    DAG dag = ri.getDAG();
                    Ledger ledger = dag.createLedger();
                    Map<String, Double> ledger2 = new HashMap<>();
                    for (HexString h : ledger.getMap().keySet()) {
                        ledger2.put(h.getCompressed(), ledger.getBalance(h));
                    }
                    System.out.println(ledger.toString());
                    System.out.println(ledger2.toString());
                    
                }
                else if (s.startsWith("v")) { //visualize
                    TangleVisualizer v = new TangleVisualizer(ri.getDAG());
                    v.visualize();
                    ri.getInsertables().add(new VisualizerTangleInterface(v));
                }
                /*else if (s.startsWith("w")) { //weight
                    String[] arr = s.split(" ");
                    String tx = arr[1];
                    int txId = Integer.parseInt(tx);
                    TangleTransaction tr = t.performantTransactions.stream().filter(x -> x.DEBUGgetDEBUGId() == txId).collect(Collectors.toList()).get(0);
                    System.out.println("Cumulative Weight: " + tr.calculateCumulativeNodeWeight(t));
                }*/
                else if (s.startsWith("setmin")) { //setminweight of visualizer
                    visualizer.minCumWeightForAcceptance = Integer.parseInt(s.split(" ")[1]);
                }
                else if (s.startsWith("self")) {
                    System.out.println(Base62.fromBase16(ri.getPublicKey().getHashString()).getBase62());
                    System.out.println(ri.getPublicKey().getHashString());
                }else if(s.startsWith("bal")) { //balance
                	System.out.println("Balance: " + ri.getDAG().getBalance(HexString.fromHashString(s.split(" ")[1])));
                	
                }else if(s.startsWith("net") || s.startsWith("pool")){
                	
                	System.out.println(ri.getShardedPool().list.toString());
                	
                }/*else if(s.startsWith("tan")) {
                
                	t.printOutTangleStats();
                }*/
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void close() {
    	sc.close();
    }
}
