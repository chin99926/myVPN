import java.util.Base64;
import java.util.Arrays;

import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.KeyGenerator;

public class SessionKey {
	static SecretKey mySecretKey;
	static SecretKeySpec myKeySpec;
	static KeyGenerator keyGen;
	
	SessionKey(Integer keylength) {
		try {
			keyGen = KeyGenerator.getInstance("AES");
		} catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		if(keylength <= 128) keyGen.init(128);
		else if(keylength <= 192) keyGen.init(192);
		else keyGen.init(256);
		mySecretKey = keyGen.generateKey();
	}
	
	SessionKey(String encodedKey) {
		byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
		mySecretKey = new SecretKeySpec(keyBytes, "AES");
	}
	
	public static void main(String[] args) {
		// empty main function...
		/*
		try {
			keyGen = KeyGenerator.getInstance("AES");
		} catch(NoSuchAlgorithmException e) {
			// no exception handling...
		}
		keyGen.init(128);
		mySecretKey = keyGen.generateKey();
		encoder = Base64.getEncoder();
		
		byte[] byteKey = mySecretKey.getEncoded();
		String encodedKey = encoder.encodeToString(byteKey);
		
		printByteArray(byteKey);
		System.out.print('\n');
		System.out.println(encodedKey);
		*/
	}
	
	public SecretKey getSecretKey() {
		return mySecretKey;
	}
	
	public String encodeKey() {
		return Base64.getEncoder().encodeToString(mySecretKey.getEncoded());
	}
	
	private static void printByteArray(byte[] bytes){
		for (byte b1 : bytes){
			String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
			System.out.print(s1);
		}
	}
}