package root;

import java.util.ArrayList;
import java.util.List;

public class RootChain {
	
	private List<Block> chain;
	
	public RootChain(){
		chain = new ArrayList<>();
	}
	
	public void appendBlock(Block block){
		block.setPreviousBlock(chain.get(chain.size()-1));
		chain.add(block);
	}
	
}
