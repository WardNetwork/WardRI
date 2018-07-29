package model;

import cern.colt.Arrays;

public class Hash extends HexString
{
    public static final int BYTE_LENGTH = 32;
    
    public Hash(byte[] hash) {
        super(hash);
    	if(hash.length != BYTE_LENGTH){
    		throw new IllegalArgumentException("Byte Length does not match SHA-256 Hash standard. Try HexString!  Value: " + new HexString(hash).toString());
    	}
    }
    
    public static Hash fromHashString(String hash) {
        return new Hash(HexString.fromHashString(hash).bytes);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HexString) {
            return super.equals(obj);
        }
        return super.equals(obj);
    }
}
