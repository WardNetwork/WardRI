package model;

public interface Storeable {

	public String store() ;
	
	public static Storeable fromStore(String s) {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	public Storeable readStore(String s);
	
}
