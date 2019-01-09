import java.io.*;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;

public class SessionDecrypter {
	private SessionKey sessionKey;
	private IvParameterSpec ivSpec;
	
	SessionDecrypter(String key, String iv) {
		sessionKey = new SessionKey(key);
		ivSpec = new IvParameterSpec(Base64.getDecoder().decode(iv));
	}
	
	public CipherInputStream openCipherInputStream(InputStream input) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, sessionKey.getSecretKey(), ivSpec);
		
		return new CipherInputStream(input, cipher);
	}
}