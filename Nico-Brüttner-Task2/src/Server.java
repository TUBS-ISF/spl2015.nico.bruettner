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

	public static SecretKeySpec secretKeySpec = null;
	public static boolean encrypt = false;
	public static boolean pm = false;
	public static boolean history = false;
	public static boolean sound = false;
	public static boolean needUsername = false;
	public static String pass = "";
	private static FileWriter fw;
	private static BufferedWriter bw;
	private static ServerSocket serverSocket;

	public static int nextID = 0;

	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String s = args[i];
			if ("-h".equals(s))
				history = true;
			else if ("-e".equals(s))
				encrypt = true;
			else if ("-pm".equals(s))
				pm = true;
			else if ("-s".equals(s))
				sound = true;
			else if ("-u".equals(s))
				needUsername = true;
			else if ("-p".equals(s)) {
				if ((i + 1) < args.length) {
					i++;
					pass = args[i];
				} else
					pass = "";
			}
		}

		int port = 12345;

		if (encrypt)
			generateKey("geheim");

		try {
			if (history) {
				fw = new FileWriter("history.txt");
				bw = new BufferedWriter(fw);
			}
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			System.exit(1);
		}

		System.out.println("start");
		new AcceptThread(serverSocket).start();
	}

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

	public static void writeHistory(String str) {
		if (!history)
			return;

		try {
			bw.write(str);
			bw.newLine();
		} catch (IOException e) {
		}

	}

	public static void close() {
		if (history) {
			try {
				bw.close();
				fw.close();
			} catch (IOException e) {
			}
		}

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