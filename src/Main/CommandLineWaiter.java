package Main;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.pmw.tinylog.Logger;
import org.rpanic.GroupedNeighborPool;

import Interfaces.ShardedVisualizerInterface;
import Interfaces.VisualizerTangleInterface;
import keys.Base62;
import model.HexString;
import model.Ledger;
import newMain.DAG;
import newMain.RI;
import newMain.Transaction;
import newMain.TxCreator;
import newMain.TxInserter;
import sharded.ShardingLevel;
import sharded.ShardingLevelTransaction;

public class CommandLineWaiter
{
	static Scanner sc;
	
    public static void startCommandLineInput(RI ri, GroupedNeighborPool pool) {
        
    	
        //TransactionIssuer issuer = new TransactionIssuer(t, distr, KeyStore.getPrivateKey(), KeyStore.getPublicKey());
        
        sc = new Scanner(System.in);
        
        
        while (true) {
            final String s = sc.nextLine();
            try {
                if (s.startsWith("tx")) {
                	
                    String[] arr = s.split(" ");
                    
                    HexString reciever = HexString.fromString(arr[1]);
                    Transaction trans = new TxCreator(ri).create(reciever, Integer.parseInt(arr[2]), null);
                    
                    boolean success = new TxInserter(ri).insert(trans);
                    
					Logger.info(success ? 
							"Tx with TxId " + trans.getTxId().getHashString() + " succssfully inserted - broadcasting it to the network now" : 
							"Tx could not be created");
                    
                }
            	if(s.startsWith("st")){ //Status
            		
            		Logger.info(ri.getDAG().getTransactionList().size() + " transactions: " + ri.getDAG().getTransactionList().toString());
            		
            	}
                else if (s.startsWith("sh")) {
                	
                    DAG dag = ri.getDAG();
                    Ledger ledger = dag.createLedger();
                    Map<String, Double> ledger2 = new HashMap<>();
                    for (HexString h : ledger.getMap().keySet()) {
                        ledger2.put(h.getCompressed(), ledger.getBalance(h));
                    }
                    Logger.info(ledger.toString());
                    Logger.info(ledger2.toString());
                    
                }
                else if (s.startsWith("v")) { //visualize
                	
                	String[] arr = s.split(" ");
                	
                	TangleVisualizer visualizer = null;
                	
            		int level = arr.length >= 2 ? Integer.parseInt(arr[1]) : 0;
            		if(level > 0) {
            			ShardingLevel l = ri.getShardingLevel(level);
            			
            			if(l.getLocalDAG().getGenericDAG().getTransactionList().size() < 2) {
            				Logger.info("There have to at least 2 Epochs to display the DAG");
            			}else {
	            			visualizer = new TangleVisualizer<ShardingLevelTransaction>(l.getLocalDAG().getGenericDAG().getTransactionList());
	            			
	                        l.addInsertable(new ShardedVisualizerInterface((TangleVisualizer<ShardingLevelTransaction>) visualizer));
            			
            			}
            		}else {
            			visualizer = new TangleVisualizer<Transaction>(ri.getDAG().getTransactionList());
                        ri.addInsertable(new VisualizerTangleInterface((TangleVisualizer<Transaction>) visualizer));
            		}
            		if(visualizer != null) {
                		visualizer.visualize();
            		}
            		
                }
                /*else if (s.startsWith("w")) { //weight
                    String[] arr = s.split(" ");
                    String tx = arr[1];
                    int txId = Integer.parseInt(tx);
                    TangleTransaction tr = t.performantTransactions.stream().filter(x -> x.DEBUGgetDEBUGId() == txId).collect(Collectors.toList()).get(0);
                    System.out.println("Cumulative Weight: " + tr.calculateCumulativeNodeWeight(t));
                }
                else if (s.startsWith("setmin")) { //setminweight of visualizer
                    visualizer.minCumWeightForAcceptance = Integer.parseInt(s.split(" ")[1]);
                }*/
                else if (s.startsWith("self")) {
                	Logger.info(Base62.fromBase16(ri.getPublicKey().getHashString()).getBase62());
                    Logger.info(ri.getPublicKey().getHashString());
                }else if(s.startsWith("bal")) { //balance
                	String[] arr = s.split(" ");
                	HexString hex =  ( arr.length > 1 ? HexString.fromHashString(arr[1]) : ri.getPublicKey() ) ;
                	Double balance = ri.getDAG().getBalance(hex);
                	if(balance == 0d) {
                		//TODO B62 hinzufügen
                	}
                	Logger.info("Balance: " + balance);
                	
                	
                }else if(s.startsWith("net") || s.startsWith("pool")){
                	
                	Logger.info(ri.getShardedPool().list.toString());
                	
                }else if(s.startsWith("info")){
                	
                	Logger.info("Levels: " + ri.getShardingLevels().size());
                	
                	Logger.info("Baselevel: " + ri.getDAG().getTransactionList().size() + " Transactions");
                	Logger.info("Peers: " + ri.getShardedPool().list.toString());
            		
                	
                	for(int i = 1 ; i <= ri.getShardingLevels().size() ; i++){
                		
                		ShardingLevel l = ri.getShardingLevel(i);
                		
                		Logger.info("Level " + i + ": " + l.id);
                		Logger.info("\tNum Txs: " + l.getLocalDAG().getGenericDAG().getTransactionList().size());
                		
                		Logger.info("Peers: " + l.getNeighborPool().list.toString());
                		
                	}
                	
                }else if(s.startsWith("dump")) {
                	
                	String[] arr = s.split(" ");
                	int level = arr.length > 1 ? Integer.parseInt(arr[1]) : 0;
                	String map = arr.length > 2 ? arr[2] : "data"; //data, id
                	if(level > 0) {
                		ShardingLevel l = ri.getShardingLevel(level);
                		
                		Stream<ShardingLevelTransaction> stream = l.getLocalDAG()
        				.getGenericDAG().getTransactionList()
        				.stream()
        				.sorted((x, y) -> Long.compare(x.getCreatedTimestamp(), y.getCreatedTimestamp()))
        				.limit(50);
                		
                		Stream<String> mapped = null;
                		
                		if(map.equals("data")) {
                			mapped = stream.map(x -> x.getData().toString());
                		}else if(map.equals("id")) {
                			mapped = stream.map(x -> x.getTxId().getHashString());
                		}
                		
                		List<String> list = mapped.collect(Collectors.toList());
                		
                		Logger.info(list.toString());
                	}else {
                		
                		List<String> list = ri.getDAG().getTransactionList()
                				.stream()
                				.sorted((x, y) -> Long.compare(x.getCreatedTimestamp(), y.getCreatedTimestamp()))
                				.limit(50)
                				.map(x -> x.getSender().toString() + " " + x.getValue())
                				.collect(Collectors.toList());
                		
                		Logger.info(list.toString());
                	}
                	
                }
            	/*}else if(s.startsWith("tan")) {
                
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
