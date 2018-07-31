package Main;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.rpanic.GroupedNeighborPool;
import org.rpanic.NeighborPool;

import Interfaces.LocalTangleInterface;
import Interfaces.TangleInterface;
import Interfaces.TangleInterfaceDistributor;
import Interfaces.VisualizerTangleInterface;
import conf.Configuration;
import keys.Adam;
import keys.Base62;
import keys.KeyStore;
import model.HexString;
import model.Ledger;
import model.TangleTransaction;
import newMain.DAG;
import newMain.RI;
import newMain.TxInserter;

public class CommandLineWaiter
{
	static Scanner sc;
	
    public static void startCommandLineInput(RI ri, TangleVisualizer visualizer, GroupedNeighborPool pool) {
        
    	
        //TransactionIssuer issuer = new TransactionIssuer(t, distr, KeyStore.getPrivateKey(), KeyStore.getPublicKey());
        
        sc = new Scanner(System.in);
        
        
        while (true) {
            final String s = sc.nextLine();
            try {
                /*if (s.startsWith("tx")) {
                	
                    String[] arr = s.split(" ");
//                    String hexReciever = Base62.fromBase62(arr[1]).getBase16();
                    HexString reciever = HexString.fromString(arr[1]);
                    Transaction trans = new Transaction(t.configuration.getHexString("publickey"), reciever, Integer.parseInt(arr[2]));
                    
                    new TxInserter().issue(trans);
                    
                }*/
                if (s.startsWith("sh")) {
                	
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
                    distr.addTangleInterface(new VisualizerTangleInterface(v));
                }
                else if (s.startsWith("w")) { //weight
                    String[] arr = s.split(" ");
                    String tx = arr[1];
                    int txId = Integer.parseInt(tx);
                    TangleTransaction tr = t.performantTransactions.stream().filter(x -> x.DEBUGgetDEBUGId() == txId).collect(Collectors.toList()).get(0);
                    System.out.println("Cumulative Weight: " + tr.calculateCumulativeNodeWeight(t));
                }
                else if (s.startsWith("setmin")) { //setminweight of visualizer
                    ((VisualizerTangleInterface)distr.getInterface(VisualizerTangleInterface.class)).visualizer.minCumWeightForAcceptance = Integer.parseInt(s.split(" ")[1]);
                }
                else if (s.startsWith("self")) {
                    System.out.println(Base62.fromBase16(t.configuration.getHexString(Configuration.PUBLICKEY).getHashString()).getBase62());
                    System.out.println(t.configuration.getHexString(Configuration.PUBLICKEY).getHashString());
                }else if(s.startsWith("bal")) { //balance
                	System.out.println("Balance: " + distr.getInterface(LocalTangleInterface.class).getBalance(HexString.fromHashString(s.split(" ")[1])));
                	
                }else if(s.startsWith("tan")) {
                	t.printOutTangleStats();
                }
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
