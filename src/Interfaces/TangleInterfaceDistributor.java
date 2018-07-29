// 
// Decompiled by Procyon v0.5.30
// 

package Interfaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Main.Tangle;
import Main.Transaction;

public class TangleInterfaceDistributor
{
    private List<TangleInterface> interfaces;
    
    public TangleInterfaceDistributor(final Tangle tangle, final TangleInterface... interfaces) {
        this.interfaces = new ArrayList<TangleInterface>();
        for (final TangleInterface in : interfaces) {
            this.interfaces.add(in);
        }
    }
    
    public void addTranscation(final Transaction t) {
        this.interfaces.forEach(x -> x.addTranscation(t));
    }
    
    public TangleInterface getInterfaceByName(final String name) {
        for (final TangleInterface in : this.interfaces) {
            if (in.getClass().getSimpleName().equals(name)) {
                return in;
            }
        }
        return null;
    }
    
    public List<TangleInterface> getInterfaces(){
    	return interfaces;
    }
    
    public TangleInterface getInterface(final Class<? extends TangleInterface> classy) {
        for (final TangleInterface in : this.interfaces) {
            if (in.getClass().getSimpleName().equals(classy.getSimpleName())) {
                return in;
            }
        }
        return null;
    }
    
    public void addTangleInterface(final TangleInterface in) {
        this.interfaces.add(in);
    }
}
