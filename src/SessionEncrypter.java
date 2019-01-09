import java.io.*;
import java.util.Base64;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;

public class SessionEncrypter {
	private SessionKey sessionKey;
	private IvParameterSpec ivSpec;
	
	SessionEncrypter(int keylength) {
		SecureRandom random = new SecureRandom();
		ivSpec = new IvParameterSpec(random.generateSeed(16));
		sessionKey = new SessionKey(keylength);
	}
	
	SessionEncrypter(String key, String iv) {
		sessionKey = new SessionKey(key);
		ivSpec = new IvParameterSpec(Base64.getDecoder().decode(iv));
	}
	
	public String encodeKey() {
		return sessionKey.encodeKey();
	}
	
	public String encodeIV() {
		return Base64.getEncoder().encodeToString(ivSpec.getIV());
	}
	
	public CipherOutputStream openCipherOutputStream(OutputStream output) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, sessionKey.getSecretKey(), ivSpec);
		
		return new CipherOutputStream(output, cipher);
	}
}