package newMain;

import java.util.Comparator;

import model.Hash;
import sharded.DAGObject;

public class DAGObjectReference<E extends DAGObject<E>> {
	
	Hash hash;
	E t;
	
	public DAGObjectReference(Hash hash){
		if(hash == null){
			throw new UnsupportedOperationException("Hash can´t be null");
		}
		this.hash = hash;
	}
	
	public DAGObjectReference(E t){
		if(t == null){
			throw new UnsupportedOperationException("Transaction can´t be null");
		}
		this.t = t;
	}

	public Hash getTxId() {
		if(hash != null){
			return hash;
		}else if(t != null){
			hash = t.getTxId();
			return hash;
		}else{
			return null;
		}
	}
	
	//TODO Just temporarily
	public E getTransaction(DAG dag){
		if(t != null){
			return t;
		}else if(hash != null){
			E tx = (E)dag.findTransaction(hash);
			if(tx != null){
				t = tx;
				return t;
			}else{
				return null;
			}
		}else{
			return null;
		}
	}

	public E getTransaction(GenericDAG<E> dag) {
		if(t != null){
			return t;
		}else if(hash != null){
			E tx = dag.findTransaction(hash);
			if(tx != null){
				t = tx;
				return t;
			}else{
				return null;
			}
		}else{
			return null;
		}
	}
	
	public static <E extends DAGObject<E>> Comparator<DAGObjectReference<E>> getComparator(){
		return (x, y) -> {
			if(y == null || x == null){
				return 0;
			}
			return x.getTxId().getHashString().compareTo(y.getTxId().getHashString());
		};
	}
}
