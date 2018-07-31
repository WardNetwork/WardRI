package newMain;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Interfaces.TangleInterface;
import conf.Configuration;
import database.DatabaseDAG;
import model.HexString;

public class RI {

	Configuration conf;
	DAG dag;
	DatabaseDAG dbdag;
	List<TangleInterface> tangleInterfaces;
	
	public RI(){
		tangleInterfaces = new ArrayList<>();
	}
	
	public void init(Configuration conf){
		this.conf = conf;
		
		dag = new DAG();
		dbdag = new DatabaseDAG();
		//Network Relay (Weiterleitung)
		
		tangleInterfaces.addAll(Arrays.asList(dag, dbdag));
	}
	
	public List<TangleInterface> getInsertables(){
		return tangleInterfaces;
	}
	
	public DAG getDAG(){
		return dag;
	}
	
	public HexString getPublicKey(){
		return null; //TODO unimplemented
	}
	
	public KeyPair getKeyPair(){
		return null; //TODO unimplemented
	}
}
