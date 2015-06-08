import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Client implements Runnable {
	
	//#if Verschluesselung
	public static SecretKeySpec secretKeySpec = null;
	//#endif
	
	//#if Spamfilter
	public String[] filter = {"arsch","arschloch","idiot","trottel"};
	//#endif
	
	//#if GUI
	private GUI gui;
	//#endif
	
	protected PrintWriter out = null;

	protected BufferedReader in = null;

	protected Thread thread;

	protected Scanner scanner;
	
	public static void main(String args[]) throws IOException {
		
		String user = "";
		String pass = "";
		//#if (Passwort && Nickname)
//@		if (args.length != 4)
//@			throw new RuntimeException("Syntax: Client <host> <port> <username> <password>");
//@		user = args[2];
//@		pass = args[3];
		//#elif (Nickname)
//@		if (args.length != 3)
//@			throw new RuntimeException("Syntax: Client <host> <port> <username>");
//@		user = args[2];
		//#elif (Passwort)
//@		if (args.length != 3)
//@			throw new RuntimeException("Syntax: Client <host> <port> <password>");
//@		pass = args[2];
		//#else
		if (args.length != 2)
			throw new RuntimeException("Syntax: Client <host> <port>");
		//#endif

		Client client = new Client(args[0], Integer.parseInt(args[1]), user, pass);
		
		
		
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

	public Client(String host, int port, String user, String pass) {
		try {
			System.out.println("Connecting to " + host + " (port " + port + ")...");
			Socket s = new Socket(host, port);
			this.out = new PrintWriter(s.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			//#if Konsole
//@			this.scanner = new Scanner(System.in);
			//#endif
			
			//#if Verschluesselung
			generateKey("geheim");
			//#endif
			
			if (!handshake(user, pass)) {
				System.exit(0);
			}

			thread = new Thread(this);
			thread.start();

			//#if Konsole
//@			String input;
//@			while ((input = this.scanner.nextLine()) != null) {
//@				handleUserInput(input);
//@			}
			//#else
			final Client c = this;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						gui = new GUI(c);
						gui.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			//#endif
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void handleUserInput(String input) {
		input = input.trim();
		//#if (Spamfilter)
		input = replaceFilter(input);
		//#endif
		if (input.startsWith("/pm ")) {
			//#if PM
			int n = input.indexOf(" ", 4);
			if (n == -1 || (n + 1) == input.length()) {
				System.out.println("Syntaxfehler!");
			} else
				send(input);
			//#else
//@					System.out.println("Private Nachrichten sind nicht verf?gbar.");
			//#endif
		} else if (input.startsWith("/exit")) {
			System.exit(0);
		} else {
			send("/msg " + input);
		}
	}

	public boolean handshake(String user, String pass) {
		user = user.trim();
		pass = pass.trim();
		//#if (Passwort && Nickname)
//@		send(user + " " + pass);
		//#elif (Passwort)
//@		send(pass);
		//#elif (Nickname)
//@		send(user);
		//#endif
		
		String input;
		try {
			input = in.readLine();
			//#if (Verschluesselung)
			input = decrypt(input);
			//#endif
		} catch (IOException e) {
			return false;
		}
		String[] args = input.split(" ");

		if (args.length == 0) {
			System.out.println("Fehler beim Handshake");
			return false;
		}
		if (!"ok".equals(args[0])) {
			System.out.println("Handshake fehlgeschlagen - " + args[1]);
			return false;
		}

		return true;
	}
	
	//#if (Spamfilter)
	private String replaceFilter(String str) {
		for(int i = 0;i<filter.length;i++) {
			StringBuilder sb = new StringBuilder();
			for(int j = 0;j<filter[i].length();j++) {
				sb.append("*");
			}
			str = str.replaceAll(filter[i], sb.toString());
		}
		return str;
	}
	//#endif

	/**
	 * main method. waits for incoming messages.
	 */
	public void run() {
		try {
			Thread thisthread = Thread.currentThread();
			while (thread == thisthread) {
				try {
					String input = in.readLine();
					if (input == null)
						continue;
					//#if (Verschluesselung)
					input = decrypt(input);
					//#endif
					if (!handleInput(input))
						break;
				} catch (EOFException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			thread = null;
			out.close();
		}
	}

	private boolean handleInput(String input) {
		String[] split = splitInput(input);
		if (split == null || split.length == 0)
			return false;

		if ("/msg".equals(split[0])) {
			print(split[1] + ": " + split[2]);
			// Beep
		}
		//#if (PM)
		else if ("/pm".equals(split[0])) {
			print(split[1] + "(privat): " + split[2]);
			// Beep
		}
		//#endif
		else if ("/exit".equals(split[0])) {
			// System.exit(0);
			return false;
		} else if ("/err".equals(split[0])) {
			print("Fehler: " + split[1]);			
		}
		return true;
	}
	
	public void print(String str) {
		//#if Konsole
//@		System.out.println(str);
		//#elif GUI
		gui.addText(str);
		//#endif
	}

	//#if (Verschluesselung)
	public String encrypt(String text) {
		// Verschluesseln
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			byte[] encrypted = cipher.doFinal(text.getBytes());

			String geheim = Base64.encodeBytes(encrypted);
			return geheim;
		} catch (Exception e) {
		}
		return null;
	}

	public String decrypt(String geheim) {
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
		}
		return null;
	}
	//#endif

	public String[] splitInput(String input) {
		if (input == null || input.length() == 0)
			return null;
		List<String> split = new ArrayList<String>();
		int last = 0;
		for (int i = 0; i < input.length(); i++) {
			if ((i + 1) < input.length() && input.charAt(i) == ' ') {
				if (input.charAt(i + 1) == ' ') {
					split.add(input.substring(last, i));
					split.add(input.substring(i + 2));
					last = -1;
					break;
				} else {
					split.add(input.substring(last, i));
					i++;
					last = i;
				}

			}
		}

		if (last != -1)
			split.add(input.substring(last));

		return split.toArray(new String[split.size()]);
	}

	public void send(String line) {
		//#if (Verschluesselung)
		line = encrypt(line);
		//#endif
		System.out.println("Sende: " + line);
		this.out.println(line);
	}
}
