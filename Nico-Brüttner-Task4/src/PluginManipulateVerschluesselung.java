import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class PluginManipulateVerschluesselung implements IPluginManipulate {

	private static SecretKeySpec secretKeySpec = null;
	
	@Override
	public void init() {
		generateKey("geheim");
	}

	@Override
	public void close() {
		secretKeySpec = null;
	}

	@Override
	public String manipulateIncoming(Connection c, String in, boolean isHandshake) {
		return decrypt(in);
	}

	@Override
	public String manipulateOutgoing(Connection c, String out) {
		return encrypt(out);
	}

	private void generateKey(String keyStr) {
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
	
	private String encrypt(String text) {
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
	
	private String decrypt(String geheim) {
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
}
