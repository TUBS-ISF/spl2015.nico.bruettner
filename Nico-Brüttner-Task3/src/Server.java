import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Server {

	//#if Verschluesselung
	public static SecretKeySpec secretKeySpec = null;
	//#endif
	/*public static boolean encrypt = false;
	public static boolean pm = false;
	public static boolean history = false;
	public static boolean sound = false;
	public static boolean needUsername = false;*/
	//#if Passwort
//@	public static String pass = "";
	//#endif
	//#if History
	private static FileWriter fw;
	private static BufferedWriter bw;
	//#endif
	private static ServerSocket serverSocket;
	
	//#if Spamfilter
	public static String[] filter = {"arsch","arschloch","idiot","trottel"};
	//#endif
	
	public static int nextID = 0;

	public static void main(String[] args) {
		//#if Passwort
//@		if(args.length != 1) {
//@			System.out.println("Syntax: Server <passwort>");
//@			System.exit(0);
//@		}
//@		pass = args[0];
		//#else
		if(args.length != 0)
			System.out.println("Parameter werden ignoriert!");
		//#endif

		int port = 12345;

		//#if Verschluesselung
		generateKey("geheim");
		//#endif
		
		
		try {
			//#if History
			fw = new FileWriter("serverHistory.txt");
			bw = new BufferedWriter(fw);
			//#endif
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			System.exit(1);
		}

		System.out.println("start");
		new AcceptThread(serverSocket).start();
	}

	//#if Verschluesselung
	private static void generateKey(String keyStr) {
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
	//#endif
	
	//#if History
	public static void writeHistory(String str) {
		try {
			bw.write(str);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {}

	}
	//#endif

	public static void close() {
		//#if History
		try {
			bw.close();
			fw.close();
		} catch (IOException e) {}
		//#endif

		try {
			if (!serverSocket.isClosed()) {
				serverSocket.close();
				Server.close();
			}
		} catch (Exception e) {
		}
	}
}

class AcceptThread extends Thread {
	private final ServerSocket serverSocket;

	public AcceptThread(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public void run() {
		try {
			while (true) {
				Socket cs = serverSocket.accept(); // warten auf
													// Client-Anforderung
				new Connection(serverSocket, cs);
				// starte den Handler-Thread zur Realisierung der
				// Client-Anforderung

			}
		} catch (Exception ex) {
			Server.close();
		}
	}
}
