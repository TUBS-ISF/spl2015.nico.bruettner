import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public aspect Verschluesselung {
	private static SecretKeySpec Client.secretKeySpec = null;
	private static SecretKeySpec Server.secretKeySpec = null;
	
	private static void Client.generateKey(String keyStr) {
		try {
			// byte-Array erzeugen
			byte[] key = (keyStr).getBytes("UTF-8");
			// aus dem Array einen Hash-Wert erzeugen mit MD5 oder SHA
			MessageDigest sha = MessageDigest.getInstance("MD5");
			key = sha.digest(key);
			// nur die ersten 128 bit nutzen
			key = Arrays.copyOf(key, 16);
			// der fertige Schluessel
			secretKeySpec = new SecretKeySpec(key, "AES");
		} catch (Exception e) {
			secretKeySpec = null;
		}
	}
	private static void Server.generateKey(String keyStr) {
		try {
			// byte-Array erzeugen
			byte[] key = (keyStr).getBytes("UTF-8");
			// aus dem Array einen Hash-Wert erzeugen mit MD5 oder SHA
			MessageDigest sha = MessageDigest.getInstance("MD5");
			key = sha.digest(key);
			// nur die ersten 128 bit nutzen
			key = Arrays.copyOf(key, 16);
			// der fertige Schluessel
			secretKeySpec = new SecretKeySpec(key, "AES");
		} catch (Exception e) {
			secretKeySpec = null;
		}
	}
	
	public static String Client.encrypt(String text) {
		// Verschluesseln
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			byte[] encrypted = cipher.doFinal(text.getBytes());
	
			// bytes zu Base64-String konvertieren (dient der Lesbarkeit)
			String geheim = Base64.encodeBytes(encrypted);
			return geheim;
		} catch (Exception e) {
		}
		return null;
	}
	public static String Server.encrypt(String text) {
		// Verschluesseln
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			byte[] encrypted = cipher.doFinal(text.getBytes());
	
			// bytes zu Base64-String konvertieren (dient der Lesbarkeit)
			String geheim = Base64.encodeBytes(encrypted);
			return geheim;
		} catch (Exception e) {
		}
		return null;
	}
	
	public static  String Client.decrypt(String geheim) {
		// BASE64 String zu Byte-Array konvertieren
		try {
			byte[] crypted2 = Base64.decode(geheim);
	
			// Entschluesseln
			Cipher cipher2 = Cipher.getInstance("AES");
			cipher2.init(Cipher.DECRYPT_MODE, secretKeySpec);
			byte[] cipherData2 = cipher2.doFinal(crypted2);
			String erg = new String(cipherData2);
			return erg;
		} catch (Exception e) {
			String a = "";
		}
		return null;
	}
	public static  String Server.decrypt(String geheim) {
		// BASE64 String zu Byte-Array konvertieren
		try {
			byte[] crypted2 = Base64.decode(geheim);
	
			// Entschluesseln
			Cipher cipher2 = Cipher.getInstance("AES");
			cipher2.init(Cipher.DECRYPT_MODE, secretKeySpec);
			byte[] cipherData2 = cipher2.doFinal(crypted2);
			String erg = new String(cipherData2);
			return erg;
		} catch (Exception e) {
			String a = "";
		}
		return null;
	}
	
	before() : execution(* Client.main(..)) {
		Client.generateKey("geheim");
	}
	before() : execution(* Server.main(..)) {
		Server.generateKey("geheim");
	}
	
	void around(String s, Object o) : execution(* send(..)) && args(s) && this(o){
		if(o instanceof Client)
			s = Client.encrypt(s);
		else
			s = Server.encrypt(s);
		
		proceed(s,o);
	}
	
	String around(Object o):call(* readLine())  && this(o){
		String s = proceed(o);
		
		if(o instanceof Client)
			s = Client.decrypt(s);
		else
			s = Server.decrypt(s);
		
		return s;
	}
}