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
	public static void main(String args[]) throws IOException {
		if (args.length < 2)
			throw new RuntimeException("Syntax: Client <host> <port> (-u username) (-p password)");

		String user = "";
		String pass = "";
		for (int i = 2; i < args.length; i++) {
			String s = args[i];
			if ("-p".equals(s)) {
				if ((i + 1) < args.length) {
					i++;
					pass = args[i];
				} else
					pass = "";
			}
			if ("-u".equals(s)) {
				if ((i + 1) < args.length) {
					i++;
					user = args[i];
				} else
					user = "";
			}
		}

		Client client = new Client(args[0], Integer.parseInt(args[1]), user, pass);
		// new Gui("Chat " + args[0] + ":" + args[1], client);
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

	public static SecretKeySpec secretKeySpec = null;
	public static boolean encrypt = false;
	public static boolean pm = false;
	public static boolean history = false;
	public static boolean sound = false;

	protected PrintWriter out = null;

	protected BufferedReader in = null;

	protected Thread thread;

	protected Scanner scanner;

	public Client(String host, int port, String user, String pass) {
		try {
			System.out.println("Connecting to " + host + " (port " + port + ")...");
			Socket s = new Socket(host, port);
			this.out = new PrintWriter(s.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			this.scanner = new Scanner(System.in);

			if (!handshake(user, pass)) {
				System.exit(0);
			}

			thread = new Thread(this);
			thread.start();

			String input;
			while ((input = this.scanner.nextLine()) != null) {
				input = input.trim();
				if (input.startsWith("/pm ")) {
					if (pm) {
						int n = input.indexOf(" ", 4);
						if (n == -1 || (n + 1) == input.length()) {
							System.out.println("Syntaxfehler!");
						} else if (encrypt) {
							String msg = input.substring(n + 1);
							String cmd = input.substring(0, n + 1);
							msg = encrypt(msg);
							send(cmd + msg);
						} else
							send(input);
					} else {
						System.out.println("Private Nachrichten sind nicht verfügbar.");
					}
				} else if (input.startsWith("/exit")) {
					System.exit(0);
				} else {
					if (encrypt)
						send("/msg " + encrypt(input));
					else
						send("/msg " + input);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean handshake(String user, String pass) {
		user.trim();
		pass.trim();
		StringBuilder sw = new StringBuilder();
		if (user.length() > 1) {
			sw.append("user " + user);
		}
		if (pass.length() > 1) {
			if (sw.length() > 1)
				sw.append(" ");
			sw.append("pass " + pass);
		}
		if (sw.length() > 1)
			this.out.println(sw.toString());
		else
			this.out.println("login");
		String input;
		try {
			input = in.readLine();
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

		for (int i = 1; i < args.length; i++) {
			String s = args[i];
			if ("-h".equals(s))
				history = true;
			else if ("-e".equals(s))
				encrypt = true;
			else if ("-pm".equals(s))
				pm = true;
			else if ("-s".equals(s))
				sound = true;
		}
		if (encrypt)
			generateKey("geheim");

		return true;
	}

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
			if (encrypt)
				split[2] = decrypt(split[2]);
			System.out.println(split[1] + ": " + split[2]);
			// Beep
		} else if ("/pm".equals(split[0])) {
			if (encrypt)
				split[2] = decrypt(split[2]);
			System.out.println(split[1] + "(privat): " + split[2]);
			// Beep
		} else if ("/exit".equals(split[0])) {
			// System.exit(0);
			return false;
		} else if ("/err".equals(split[0])) {
			System.out.println("Fehler: " + split[1]);
		}
		return true;
	}

	public String encrypt(String text) {
		if (!encrypt)
			return text;
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

	public String decrypt(String geheim) {
		if (!encrypt)
			return geheim;
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
		System.out.println("Sende: " + line);
		out.println(line);
	}
}