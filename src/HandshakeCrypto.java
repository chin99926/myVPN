import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.cert.CertificateFactory;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;

public class HandshakeCrypto {
	
	public static byte[] encrypt(byte[] plaintext, Key key)
		throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(plaintext);
	}
	
	public static byte[] decrypt(byte[] ciphertext, Key key)
		throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(ciphertext);
	}
	
	public static PrivateKey getPrivateKeyFromKeyFile(String keyfile) {
		try {
			Path p = Paths.get(keyfile);
			byte[] privKeyBytes = Files.readAllBytes(p);
			
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privKeyBytes);
			return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
		} catch(IOException e) {
			System.out.println("File \"" + keyfile + "\" not found.");
			return null;
		} catch(NoSuchAlgorithmException e) {
			// Not possible
			return null;
		} catch(InvalidKeySpecException e) {
			System.out.println("File \"" + keyfile + "\" does not contain a valid key file.");
			return null;
		}
	}
}