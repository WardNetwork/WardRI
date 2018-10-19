package Main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import model.Hash;
import newMain.DAGObjectReference;
import sharded.DAGObject;

public class TangleVisualizer<E extends DAGObject<E>> {

	List<E> list;
	Layout<String, String> layout;
	BasicVisualizationServer<String,String> vv;
	public int minCumWeightForAcceptance = 2000;
	
	Map<E, Integer> map = new HashMap<>();
	
	public TangleVisualizer(List<E> list){
		
		this.list = list;
		
	}
	
	public void addEdge(E t1, Hash t2) {
		
		//layout.getGraph().addEdge(t1.DEBUGgetDEBUGId()+""+t2.DEBUGgetDEBUGId(), t1.DEBUGgetDEBUGId()+"", t2.DEBUGgetDEBUGId()+"");
		int tnum = map.get(t1);
		int t2num = map.get(get(t2));
		layout.getGraph().addEdge(tnum + " " + t2num, ""+tnum, ""+t2num, EdgeType.DIRECTED);
		if(vv.isShowing()) {
			vv.repaint();
			vv.updateUI();
		}
		
	}
	
	private E get(Hash h) {
		return list.stream().filter(x -> x.getTxId().equals(h)).findFirst().orElse(null);
	}
	
	public void addTransaction(E t){
		
		map.put(t, map.values().stream().max((x, y) -> Integer.compare(x, y)).orElse(0) + 1);
		
		for(DAGObjectReference<E> child : t.getConfirmed()){
			addEdge(t, child.getTxId());
		}
		
	}

	public void visualize(){
		
		if(list.size() == 0) {
			return;
		}
		
		DirectedSparseMultigraph<String, String> g = new DirectedSparseMultigraph<>();
		
		System.out.println("Started parsing the edges...");
		
		int count = 0;
		
		List<E> ordered = list.stream()
		.sorted((x, y) -> Long.compare(x.getCreatedTimestamp(), y.getCreatedTimestamp()))
		.collect(Collectors.toList());
		
		for(E t : ordered){
			map.put(t, ++count);
		}
		
		for(E t : list){
			
			int tnum = map.get(t);
			
			for(DAGObjectReference<E> t2 : t.getConfirmed()){
				
				int t2num = map.get(get(t2.getTxId()));
				
				g.addEdge(tnum + " " + t2num, ""+tnum, ""+t2num, EdgeType.DIRECTED);
				
			}
			
		}
		
		System.out.println("Edges finished, starting rendering");
		
		Map<String, E> transIds = new HashMap<>();
		
		for(E t : list) {
			transIds.put(t.getTxId().getHashString(), t);
		}
		
		Transformer<String,Paint> vertexPaint = new Transformer<String,Paint>() {
			 public Paint transform(String i) {
				 E t = transIds.get(i);
				 
				 if(t == null){
					 for(E temp : list){
						 //if(temp.getTxId().getHashString() == Integer.parseInt(i)){
							 transIds.put(i, temp);
							 t = temp;
						 //}
					 }
				 }
				 if(t == null)
					 return Color.RED;
				 
				 if((t.getTxId().getHashString()+"").equals(i)) {
					 if(0 /*t.cumW*/ > minCumWeightForAcceptance){
						 return Color.GREEN;
					 }else {
						 return Color.RED;
					 }
				 }
				 
				 return Color.BLUE;
			 }
		 }; 
		
	//	 Layout<String, String> layout = new DAGLayout<String, String>(g);
		 layout = new ISOMLayout<String, String>(g);
		 layout.setSize(new Dimension(1200,800)); // sets the initial size of the spacec
		 // The BasicVisualizationServer<V,E> is parameterized by the edge types
		 try {
			 vv = new BasicVisualizationServer<String,String>(layout);
			 vv.setPreferredSize(new Dimension(1200,800)); //Sets the viewing area size
		 }catch(Exception e) {
			 e.printStackTrace();
			 return;
		 }
		 	
		 vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		 vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
		 
		 JFrame frame = new JFrame("Simple Graph View");
		 frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		 frame.getContentPane().add(vv);
		 frame.pack();
		 frame.setVisible(true); 
		 
		 frame.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_R) {
					ISOMLayout<String, String> l = new ISOMLayout<>(layout.getGraph());
					vv.setGraphLayout(l);
					vv.repaint();
				}
			}
		});
		
	}
	
}
