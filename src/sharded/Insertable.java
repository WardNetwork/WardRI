package sharded;

import model.Hash;

public interface Insertable<E> {
	
	public void addTransaction(E obj);
	
	public E getTransaction(Hash hash);
}
