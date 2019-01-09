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
		mySecretKey = new SecretKeySpec(Base64.getDecoder().decode(encodedKey), "AES");
	}
	
	public SecretKey getSecretKey() {
		return mySecretKey;
	}
	
	public String encodeKey() {
		return Base64.getEncoder().encodeToString(mySecretKey.getEncoded());
	}
}