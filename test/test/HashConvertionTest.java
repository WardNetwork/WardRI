package test;

import Main.Transaction;
import model.HexString;

public class HashConvertionTest {

	public static void main(String[] args) {
		
		
//		System.out.println(0 % 200);
//		HexString h = new HexString(new byte[] {13,65,127});
		HexString hh = HexString.fromHashString("0x3032301006072a8648ce3d020106052b81040006031e00044bcf8446fcd64238cfa5cbb10c5c12d2d2a8a0af7831276909542e56");
		HexString h = HexString.fromCompressed("6JsvOCaQRhQR57R2l6ycqgloRH2bZ0se7CxRuNIzourWddD7RgzmCZppolNC7lP6aDRExM");

		Transaction t = new Transaction(hh, h, 4);
		Transaction t2 = new Transaction(hh, h, 2);
				
		t.confirm(t2);
		
		System.out.println("hash: " + t.hashCodeSHA());
//		System.out.println("hash: " + t.hashCodeSHADEBUG());
		
		
				
		String s = h.getHashString();
		
		String comp = h.getCompressed();
		
		System.out.println(("6JsvOCaQRhQR57R2l6ycqgloRH2bZ0se7CxRuNIzourWddD7RgzmCZppolNC7lP6aDRExM".equals(comp)));
		
		System.out.println(s);
		
		System.out.println(hh.getCompressed());
		
		HexString.fromHashString(s);
		
		
		
		HexString a = HexString.fromHashString("0x3022020f00bca434847bbc1c17f0e15dfb38c2020f00a963b1e8e4675efd8b11efc85a37");
	
		HexString b = HexString.fromHashString("0x3020020e0cfb75b26066d34d2fe61fa0e719020e56a6b87df776704f55cccee10146");
		
		System.out.println(a.getHash().length);
		System.out.println(b.getHash().length);
		
		
	}
	
}
