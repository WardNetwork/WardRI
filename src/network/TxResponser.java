package network;

import java.net.Socket;

import org.rpanic.Responser;

import Main.Tangle;
import Main.TangleVisualizer;
import Main.Transaction;
import Main.TransactionIntegrater;

public class TxResponser implements Responser<String, Socket>
{
    Tangle tangle;
    TangleVisualizer visualizer;
    TransactionIntegrater integrater;
    
    public TxResponser(Tangle tangle, TransactionIntegrater integrater) {
        this.visualizer = null;
        this.tangle = tangle;
        this.integrater = integrater;
    }
    
    @Override
    public boolean acceptable(String responseType) {
        return responseType.equals("tx");
    }
    
    @Override
    public synchronized void accept(String response, Socket socket) {
        Transaction t = new Transaction();
        t.readStore(response.replaceAll("tx ", ""), tangle);
        this.addTxToTangle(t);
    }
    
    public void addTxToTangle(Transaction t) {
    	integrater.integrate(t);
    }
    
//    public void addTxToTangle(Transaction t) {
//        boolean conflicted = false;
//        for ( Transaction toConfirm : t.getConfirmed()) {
//            if(toConfirm == null) {
//                System.out.println("tx not available");
//                conflicted = true;
//            }
//            else {
//                if (conflicted) {
//                    continue;
//                }
//                t.confirm(toConfirm);
//            }
//        }
//        if (!conflicted) {
//            this.tangle.transactions.put(t.getTxHash().getHashString(), t);
//            if (this.visualizer != null) {
//                this.visualizer.addTransaction(t);
//            }
//            System.out.println("New Transaction: " + t);
//        }
//    }
    
    public void registerVisualizer(TangleVisualizer v) {
        this.visualizer = v;
    }
}
