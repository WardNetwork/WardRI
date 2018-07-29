package Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conf.Configuration;
import model.Hash;
import model.HexString;
import model.Ledger;
import model.TangleTransaction;
import model.TransactionBase;

public class Tangle
{
    public volatile Map<String, TangleTransaction> transactionsMap;
    //TODO Eine Performante Variante der Transaction machen, wo keine Hash gespeichert sind sondern Objektreferenzen
    public Configuration configuration;
    List<Transaction> temp;
    public List<TangleTransaction> performantTransactions;
    
    Ledger currentLedgerState;
    
    Logger log = LoggerFactory.getLogger(Tangle.class);
    
    public Tangle() {
    	this.transactionsMap = new HashMap<String, TangleTransaction>();
    	this.performantTransactions = new ArrayList<>();
        this.temp = new ArrayList<Transaction>();
        this.currentLedgerState = new Ledger();
    }
    
    public Tangle(final Configuration conf) {
        this();
        this.configuration = conf;
    }
    
    public void printOutTangleStats() {
        double min = 0.0;
        double max = 0.0;
        double sum = 0.0;
        int numOver3 = 0;
        int num1 = 0;
        int numOfUnconfirmedNodes = 0;
        for (final TangleTransaction t : this.transactionsMap.values()) {
            final int confSize = t.getNodesWhichConfirmedMe().size();
            if (min > confSize) {
                min = confSize;
            }
            if (max < confSize) {
                max = confSize;
            }
            sum += confSize;
            if (confSize == 1) {
                ++num1;
            }
            if (confSize >= 3) {
                ++numOver3;
            }
            if (confSize == 0) {
                ++numOfUnconfirmedNodes;
            }
        }
        log.info("Minimal confirmations: " + min);
        log.info("Maximal confirmations: " + max);
        log.info("Nodes with 1 direct confirmation: " + num1);
        log.info("Nodes with 3 or more direct confirmation: " + numOver3);
        log.info("Number of unconfirmed nodes: " + numOfUnconfirmedNodes);
        log.info("Total nodes: " + this.transactionsMap.size());
        log.info("Avg confirmations: " + sum / this.transactionsMap.size());
    }
    
    public TangleTransaction addOwnTransaction(Transaction t){
    	
    	if(t.isSealed()){
    		
    		TangleTransaction performant = TangleTransaction.fromTransaction(t, this/*, false*/);
	    		
    		this.transactionsMap.put(performant.getTxHash().getHashString(), performant);
    		this.performantTransactions.add(performant);

            log.info("Added Tx" + t.DEBUGgetDEBUGId() + " " + t.getTxHash());
    		
            for(Hash confirmation : t.getConfirmed()){
            	
            	TangleTransaction confT = findTangleTransaction(confirmation);
            	
            	confT.getNodesWhichConfirmedMe().add(performant);
            	
            }

	        currentLedgerState.addTransaction(t);
	        TangleAlgorithms.addTransactionToCache(t);
	        
	        return performant;
    	}else{
    		log.error("Transaction is not sealed");
    	}
	
    	
    	return null;
    }
    
//    @Deprecated
//    public void addTransaction(final Transaction t) {
//        t.doPoW();
//        this.transactions.put(t.getTxHash().getHashString(), t);
//        log.info("Added Tx" + t.DEBUGgetDEBUGId() + " " + t.getTxHash());
//        log.info("New Transaction: " + t.toString());
//        
//        Ledger ledger = currentLedgerState;
//        Transaction elected = this.electValidTransaction(t, ledger);
//        
//        if (elected != null) {
//        	
//            this.confirmTransaction(t, elected);
//            System.out.println(String.valueOf(t.DEBUGgetDEBUGId()) + " confirmed " + elected.DEBUGgetDEBUGId());
//            final Transaction elected2 = this.electValidTransaction(t, ledger, elected);
//            if (elected2 != null) {
//                this.confirmTransaction(t, elected2);
//                System.out.println(String.valueOf(t.DEBUGgetDEBUGId()) + " confirmed " + elected2.DEBUGgetDEBUGId());
//                
//                //Successful appended
//                //TODO Checken, ob nicht bei den ersten zwei Txs auch die Aktion ausgeführt werden soll
//                
//            }
//        }
//        t.finish();
//        
//        currentLedgerState.addTransaction(t);
//        TangleAlgorithms.addTransactionToCache(t);
//    }
    
//    public void addExistingGenesisTransaction(Transaction genesis){  // Überdenken ob Sicherheitslücke
//    	this.transactions.put(genesis.getTxHash().getHashString(), genesis);
//    	TangleAlgorithms.addTransactionToCache(genesis);
//        currentLedgerState.addTransaction(genesis);
//    }
    
//    @Deprecated
//    public void addExistingTransaction(Transaction t) {
//        
//        if(validateTransaction(t, currentLedgerState)) {
//        	
//        	log.info("Added Tx" + t.DEBUGgetDEBUGId() + " " + t.getTxHash());
//        	
//            for (Transaction tx : t.getConfirmed()) {
//                this.confirmTransaction(t, tx);
//            }
//            t.finish();
//        	this.transactions.put(t.getTxHash().getHashString(), t);
//
//            TangleAlgorithms.addTransactionToCache(t);
//            currentLedgerState.addTransaction(t);
//        }
//        
//    }
    
//    public void finishTransaction(Transaction t) {
//    	
//    	t.finish();
//    	
//    	for(Hash h : t.getConfirmed()) {
//    		
//    		Transaction con = findTransaction(h);
//    		
//    		if(!con.isFinished()) {
//    			throw new IllegalArgumentException("Confirmed Transaction is not finished, aborting");
//    		}
//    		
//    		con.addConfimationNodeReference(t.getTxHash().getHashString());
//    	}
//    	
//    	if(!t.getTransactionProof().getHash().equals(t.getTxHash())) {
//    		throw new IllegalArgumentException("PowProof was created with invalid Hash");
//    	}
//    }
    
    /**
     * Uses currentLedgerState
     */
    public boolean validateAndFixTangle(Transaction t){
    	boolean validT = validateTransaction(t, currentLedgerState);
    	
    	if(!validT){
    		
    		boolean ret = fixTangle(t);
    		if(!ret){
    			
    		}
    		
    	}
    	
    	return false;
    }
    
    public boolean fixTangle(Transaction t){
    	
    	List<TangleTransaction> list = this.performantTransactions.stream()
    			.filter(x -> x.getSender().equals(t.getSender()))
    			.filter(x -> x.getCreatedTimestamp() > t.getCreatedTimestamp())
    			.collect(Collectors.toList());
    	
		if(list.size() > 0){

			List<TangleTransaction> removed = new ArrayList<>();
			
			double balance = currentLedgerState.getBalance(t.getSender());
			double listSum = list.stream().mapToDouble(x -> x.getValue()).sum();
			if(listSum + balance > t.getValue()){
				
				List<TangleTransaction> listClone = new ArrayList<>(list);
				Collections.sort(listClone, (x, y) -> Long.compare(x.getCreatedTimestamp(), y.getCreatedTimestamp()));
				
				double balanceBefore = balance - listSum;
				double listSum2 = listSum;
				while(listSum2 + t.getValue() > balanceBefore){
					
					TangleTransaction temp = listClone.remove(listClone.size()-1);
					removed.add(temp);
					
					listSum2 = listClone.stream().mapToDouble(x -> x.getValue()).sum();
					
				}
				
				
			}
			
			boolean areAllEmpty = removed.stream().allMatch(x -> x.getNodesWhichConfirmedMe().isEmpty());
			
			//Repair
			for(TangleTransaction r : removed){
			
				if(r.getNodesWhichConfirmedMe().isEmpty() && areAllEmpty){
					this.performantTransactions.remove(r);
					this.transactionsMap.remove(r.getTxHash());
					
					currentLedgerState.correctTransaction(r);
					
				}else{
					
					//TODO value auf 0 setzten und die Transaction lassen, dass der Tangle valid bleibt?
					
					//TODO Was immer man dann machen will... Wahrscheinlich einfach alle anderen Wegmachen? Ist aber Angriffspunkt falls jemand dadurch den ganzen Tangle zerlegen kann :D
					
				}
			
			}
			
		}
		
		return false;
    }
    
    public Transaction electValidTransaction(Transaction t, Ledger ledger, Transaction... alreadychosen) {
        if (this.performantTransactions.size() <= 2) {
            return null;
        }
        Transaction temp = null;
        for (int i = 0; i < 100; ++i) {
            temp = TangleAlgorithms.electConfirmationTx(this, t);
            
            boolean continueB = false;
            for (Transaction chosen : alreadychosen) {
                if (chosen.getTxHash().getHashString().equals(temp.getTxHash().getHashString())) {
                	continueB = true;
                	break;
                }
            }
            if(continueB){  //Muss so umständlich gemacht werden, weils kein "double-continue" gibt
            	continue;
            }
            
            
            boolean txValid = this.validateTransaction(temp, ledger);
            log.info("Transactionelection: " + txValid + " Elected: " + temp.DEBUGgetDEBUGId());
            if (txValid) {
                return temp;
            }
        }
        log.warn("No tx elected");
        return null;
    }
    
    public boolean validateTangle() {
        boolean valid = true;
        for (TangleTransaction t : this.performantTransactions) {
        	
        	if(!t.isSealed()) {
        		log.error("Transaction not sealed!! line 222");
        	}
            Hash hash = t.hashCodeSHA();
            for (TangleTransaction link : t.getNodesWhichConfirmedMe()) {
                String h = link.getConfirmed().contains(findTangleTransaction(t.getTxHash())) ? t.getTxHash().getHashString() : null;
                if (h == null || !h.equals(hash.getHashString())) {
                	log.warn("Wrong hash between " + t.getTxHash() + " and " + link.getTxHash());
                    return false;
                }
            }
            
            TransactionProof proof = t.getTransactionProof();
            String solution = proof.getSolution();
            String powHash = TransactionProof.hash(String.valueOf(solution) + hash);
            if (!powHash.startsWith(TransactionProof.POW_CRITERIA)) {
            	log.info(powHash);
            	log.error("Wrong PoW hash at Tx " + t.getTxHash());
                return false;
            }
        }
        return valid;
    }
    
    public boolean validateTransaction(TangleTransaction t) {
        Ledger ledger = this.createLedger2(t);
        return this.validateTransaction(t, ledger);
    }
    
    public Ledger createLedger2() {
        return this.createLedger2(null);
    }
    
    public Ledger createLedger2(TangleTransaction transaction) {
    	
    	TangleTransaction genesis = this.getGenesisTransaction();
        Ledger ledger = new Ledger(genesis);
        List<TangleTransaction> alreadyChecked = new ArrayList<TangleTransaction>();
        Comparator<TangleTransaction> comparator = (t1, t2) -> Long.compare(t1.getCreatedTimestamp(), t2.getCreatedTimestamp());
        PriorityQueue<TangleTransaction> q = new PriorityQueue<TangleTransaction>(comparator);
        
        q.add(genesis);
        alreadyChecked.add(genesis);
        while (!q.isEmpty()) {
        	
            TangleTransaction n = q.remove();
            
            if (transaction != null && n.getTxHash().getHashString().equals(transaction.getTxHash().getHashString())) {
                return ledger;
            }
            if (this.validateTransaction(n, ledger)) {
            	ledger.addTransaction(n);
            }
            else {
//            	if(!n.equals(genesis)) {
	            	log.debug(n.getSender().getHashString());
	                for (HexString h : ledger.getMap().keySet()) {
	                	log.debug(String.valueOf(h.getHashString()) + " ");
	                	log.debug(String.valueOf(h.equals(n.getSender())) + " " + h.equals(n.getReciever()));
	                }
	                this.validateTransaction(n, ledger); //TODO Why?
//            	}
            }
            List<TangleTransaction> children = n.getNodesWhichConfirmedMe();//this.findAllTransactions(n.nodesWhichConfirmedMe);
            Collections.sort(children, comparator);
            for (TangleTransaction child : children) {
                if (!alreadyChecked.contains(child)) {
                    q.add(child);
                    alreadyChecked.add(child);
                }
            }
        }
        return ledger;
    }
    
    public boolean validateTransaction(TransactionBase t, Ledger ledgerSoFar) {
    	//TODO Debug
    	if(t.getTransactionProof() == null) {
    		log.error("Transactionproof is null!");
    	}
    	
    	
        boolean valid = t.getTransactionProof().validateProof();
        
        return ledgerSoFar.validTransaction(t) && valid;
    }
    
    public TangleTransaction getGenesisTransaction() {
    	TangleTransaction oldest = null;
        for (TangleTransaction x : this.performantTransactions) {
            if (oldest == null || oldest.getCreatedTimestamp() > x.getCreatedTimestamp()) {
                oldest = x;
            }
        }
        return oldest;
    }
    
    public TangleTransaction getNewestTransaction() {
    	TangleTransaction newest = null;
        for (TangleTransaction x : this.performantTransactions) {
            if (newest == null || newest.getCreatedTimestamp() < x.getCreatedTimestamp()) {
                newest = x;
            }
        }
        return newest;
    }
    
    public List<TangleTransaction> getTips() {
        final List<TangleTransaction> list = new ArrayList<TangleTransaction>();
        
        for ( TangleTransaction t : this.performantTransactions) {
        	
            if (t.getNodesWhichConfirmedMe().size() == 0) {
                list.add(t);
            }
        }
        
        return list;
    }
//    
//    public TangleTransaction getTransaction(TangleTransaction t){
//    	return transactionsMap.get(t.getTxHash());
//    }
    
//    public List<Transaction> findAllTransactions(Iterable<String> hashs) {
//        final List<Transaction> list = new ArrayList<Transaction>();
//        for (final String id : hashs) {
//            list.add(this.transactions.get(id));
//        }
//        return list;
//    }
//    
//    public List<Transaction> findAllTransactionsHex(final Iterable<Hash> hashs) {
//        final List<String> list = new ArrayList<String>();
//        for (final HexString h : hashs) {
//            list.add(h.getHashString());
//        }
//        return this.findAllTransactions(list);
//    }
//    
//    public Transaction findTransaction(String hash) {
//        return this.transactions.get(hash);
//    }
//    
//    public Transaction findTransaction(HexString hash) {
//        return this.findTransaction(hash.getHashString());
//    }
    
    public TangleTransaction findTangleTransaction(Hash hash){
    	return performantTransactions.stream().filter(x -> x.getTxHash().equals(hash)).collect(Collectors.toList()).get(0);
    }
    
//    @Deprecated
//    public void confirmTransaction(Transaction transaction, Transaction appended) {
//        if (appended == null) {
//            return;
//        }
//        transaction.confirm(appended);
//        System.out.println("Tx" + transaction.getTxHash() + " confirmed Tx" + appended.getTxHash());
//    }
    
    public Hash calculateTxHash(final Transaction t) {
        return t.hashCodeSHA();
    }
    
    public List<TangleTransaction> getTransactions(){
    	return performantTransactions;
    }
    
    public Map<String, TangleTransaction> getTransactionMap() {
        return this.transactionsMap;
    }
}
