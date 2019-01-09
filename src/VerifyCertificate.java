import java.io.*;
import java.lang.*;

import java.security.*;
import java.security.cert.*;

public class VerifyCertificate {
	public static void main(String[] args) throws Exception {}
	
	public static X509Certificate createCert(String file) {
		try {
			CertificateFactory cfactory = CertificateFactory.getInstance("X.509");
			FileInputStream fis = new FileInputStream(file);
			return (X509Certificate)cfactory.generateCertificate(fis);
		} catch(FileNotFoundException e) {
			System.out.println("File \"" + file + "\" not found.");
			return null;
		} catch(CertificateException e) {
			e.printStackTrace();
			return null;
		}
	}
}