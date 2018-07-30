package newMain;

import java.util.ArrayList;
import java.util.List;

import conf.Configuration;

public class RI {

	Configuration conf;
	DAG dag;
	List<Insertable> insertables;
	
	public RI(){
		insertables = new ArrayList<>();
	}
	
	public void init(Configuration conf){
		this.conf = conf;
		
		dag = new DAG();
		//DB
		//Network Relay (Weiterleitung)
		
		insertables.add(dag);
	}
	
	public List<Insertable> getInsertables(){
		return insertables;
	}
	
	public DAG getDAG(){
		return dag;
	}
}
