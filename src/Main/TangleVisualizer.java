package Main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import model.Hash;
import newMain.DAG;
import newMain.Transaction;
import newMain.TransactionReference;

public class TangleVisualizer {

	DAG dag;
	Layout<String, String> layout;
	BasicVisualizationServer<String,String> vv;
	public int minCumWeightForAcceptance = 2000;
	
	public TangleVisualizer(DAG dag){
		
		this.dag = dag;
		
	}
	
	public void addEdge(Transaction t1, Hash t2) {
		
		//layout.getGraph().addEdge(t1.DEBUGgetDEBUGId()+""+t2.DEBUGgetDEBUGId(), t1.DEBUGgetDEBUGId()+"", t2.DEBUGgetDEBUGId()+"");
		layout.getGraph().addEdge(t1.getTxId().getHashString()+""+t2.getHashString(), t1.getTxId()+"", t2.getHashString() +"");
		if(vv.isShowing())
			vv.updateUI();
		
	}
	
	public void addTransaction(Transaction t){
		
		for(TransactionReference child : t.getConfirmed()){
			addEdge(t, child.getTxId());
		}
		
	}

	public void visualize(){
		
		DirectedSparseMultigraph<String, String> g = new DirectedSparseMultigraph<>();
		
		System.out.println("Started parsing the edges...");
		
		for(Transaction t : dag.getTransactionList()){
			
			for(TransactionReference t2 : t.getConfirmed()){
				
				//TODO g.addEdge(t.DEBUGgetDEBUGId()+""+t2.DEBUGgetDEBUGId(), ""+t.DEBUGgetDEBUGId(), ""+t2.DEBUGgetDEBUGId(), EdgeType.DIRECTED);
				
			}
			
		}
		
		System.out.println("Edges finished, starting rendering");
		
		Map<String, TangleTransaction> transIds = new HashMap<>();
		
		for(TangleTransaction t : tangle.performantTransactions) {
			transIds.put(t.DEBUGgetDEBUGId()+"", t);
		}
		
		Transformer<String,Paint> vertexPaint = new Transformer<String,Paint>() {
			 public Paint transform(String i) {
				 TangleTransaction t = transIds.get(i);
				 
				 if(t == null){
					 for(TangleTransaction temp : tangle.performantTransactions){
						 if(temp.DEBUGgetDEBUGId() == Integer.parseInt(i)){
							 transIds.put(i, temp);
							 t = temp;
						 }
					 }
				 }
				 if(t == null)
					 return Color.RED;
				 
				 if((t.DEBUGgetDEBUGId()+"").equals(i)) {
					 if(t.calculateCumulativeNodeWeight(tangle) > minCumWeightForAcceptance){
						 return Color.GREEN;
					 }else {
						 return Color.RED;
					 }
				 }
				 
				 return Color.BLUE;
			 }
		 }; 
		
//		Layout<String, String> layout = new DAGLayout<String, String>(g);
		layout = new ISOMLayout<String, String>(g);
		 layout.setSize(new Dimension(1200,800)); // sets the initial size of the spacec
		 // The BasicVisualizationServer<V,E> is parameterized by the edge types
		 vv = new BasicVisualizationServer<String,String>(layout);
		 vv.setPreferredSize(new Dimension(1200,800)); //Sets the viewing area size
		 	
		 vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		 vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
		 
		 JFrame frame = new JFrame("Simple Graph View");
		 frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		 frame.getContentPane().add(vv);
		 frame.pack();
		 frame.setVisible(true); 
		
	}
	
}
