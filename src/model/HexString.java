package model;

import java.util.Arrays;

import keys.Base62;

public class HexString
{
    volatile byte[] bytes;
    
    public HexString(byte[] hash) {
        this.bytes = hash;
    }
    
    public byte[] getHash() {
        return this.bytes;
    }
    
    public String getHashString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("0x");
        for (int i = 0; i < this.bytes.length; ++i) {
            stringBuffer.append(Integer.toString((this.bytes[i] & 0xFF) + 256, 16).substring(1));
        }
        //TODO Dev
        if(stringBuffer.length() % 2 != 0 || stringBuffer.length() < 30) {
        	System.out.println("ERROR");
        }
        return stringBuffer.toString();
    }
    
    @Override
    public String toString() {
        return this.getHashString();
    }
    
    
    /**
     * Converts both Base 16 and Base 62 String to HexString object
     * @param s
     * @return
     */
    public static HexString fromString(String s) {
    	if(isHexString(s)) {
    		return fromHashString(s);
    	}else {
    		return fromCompressed(s);
    	}
    }
    
    public static HexString fromHashString(String s) {
    	
    	if(s.length() < 32) {
    		System.out.println(s);  //TODO DEbug
    	}
    	
    	if(s.startsWith("0x")) {
    		s = s.substring(2);
    	}
    	
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return new HexString(data);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof HexString) {
            final HexString o = (HexString)obj;
            return Arrays.equals(o.bytes, this.bytes);
        }
        return false;
    }
    
    public String getCompressed() {
        return Base62.fromBase16(this.getHashString()).getBase62();
    }
    
    public static HexString fromCompressed(String s) {
        return fromHashString(Base62.fromBase62(s).getBase16());
    }
    
    public static boolean isHexString(String s) {
    	return s.startsWith("0x");
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.bytes == null) ? 0 : Arrays.hashCode(this.bytes));
        return result;
    }
}
