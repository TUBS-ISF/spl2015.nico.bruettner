import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;

class Connection extends Thread {
	public static List<Connection> clients = new ArrayList<Connection>();
	private final Socket client;
	private final ServerSocket serverSocket;

	private PrintWriter out = null;
	private BufferedReader in = null;
	private String nick = "";

	Connection(ServerSocket serverSocket, Socket client) {
		this.client = client;
		this.serverSocket = serverSocket;

		try {
			this.out = new PrintWriter(client.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));

			// handshake
			String input = in.readLine();
			String[] args = input.split(" ");
			String pass = "";
			if (args.length == 0) {
				send("err 0", true);
				close();
				return;
			} else {
				for (int i = 0; (i + 1) < args.length; i = i + 2) {
					if (args[i].equals("user")) {
						this.nick = args[i + 1];
					} else if (args[i].equals("pass")) {
						pass = args[i + 1];
					}
				}
				if (Server.pass.length() > 0) {
					if (!Server.pass.equals(pass)) {
						send("err 1", true);
						close();
						return;
					}
				}
				if (Server.needUsername && this.nick.length() == 0) {
					send("err 2", true);
					close();
					return;
				} else if (!Server.needUsername) {
					if (this.nick.length() > 0) {
						send("err 2", true);
						close();
						return;
					} else {
						this.nick = "client" + (Server.nextID++);
					}
				}

				StringBuilder sw = new StringBuilder();
				sw.append("ok ");
				if (Server.history)
					sw.append("-h ");
				if (Server.encrypt)
					sw.append("-e ");
				if (Server.pm)
					sw.append("-pm ");
				if (Server.sound)
					sw.append("-s ");

				sw.deleteCharAt(sw.length() - 1);
				send(sw.toString(), true);
				clients.add(this);
				this.start();
				// handshake ok
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void run() {
		StringBuffer sb = new StringBuffer();
		try {

			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));

			while (true) {
				String input = in.readLine();
				if (input == null)
					continue;
				System.out.println("Empfange von " + this.nick + ": " + input);
				if (Server.encrypt)
					input = decrypt(input);
				if (!handleInput(input))
					break;
			}

		} catch (Exception e) {
			System.out.println("IOException, Handler-run");
		} finally {
			if (!client.isClosed()) {
				System.out.println("****** Handler:Client close");
				try {
					client.close();
				} catch (IOException e) {
				}
			}
			clients.remove(this);
		}
	}

	private boolean handleInput(String input) {
		if (input.startsWith("/pm ")) {
			int n = input.indexOf(" ", 4);
			if (n == -1 || (n + 1) == input.length()) {
				return false;
			} else {
				String msg = input.substring(n + 1);
				String cl = input.substring(4, n);

				Connection c = getClientByName(cl);
				if (c != null) {
					c.send("/pm " + this.nick + " " + msg, false);

					Server.writeHistory(this.nick + " -> " + c.nick + ": " + msg);
				} else {
					this.send("/err Client nicht gefunden!", false);
				}
			}
		} else if (input.startsWith("/msg ")) {
			input = input.substring(5);

			broadcast("/msg " + this.nick + " " + input, this);

			Server.writeHistory(this.nick + ": " + input);
		}
		return true;
	}

	public void send(String str, boolean isHandshake) {
		if (Server.encrypt && !isHandshake)
			str = encrypt(str);
		this.out.println(str);
		System.out.println("Sende an " + this.nick + ": " + str);
	}

	public static void broadcast(String str, Connection except) {
		for (Connection c : Connection.clients) {
			if (c != except)
				c.send(str, false);
		}
	}

	public static Connection getClientByName(String name) {
		for (Connection c : Connection.clients) {
			if (c.nick.equals(name))
				return c;
		}
		return null;
	}

	public void close() {
		try {
			client.close();
		} catch (IOException e) {
		}
	}

	public String encrypt(String text) {
		if (!Server.encrypt)
			return text;
		// Verschluesseln
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, Server.secretKeySpec);
			byte[] encrypted = cipher.doFinal(text.getBytes());

			// bytes zu Base64-String konvertieren (dient der Lesbarkeit)
			String geheim = Base64.encodeBytes(encrypted);
			return geheim;
		} catch (Exception e) {
		}
		return null;
	}

	public String decrypt(String geheim) {
		if (!Server.encrypt)
			return geheim;
		// BASE64 String zu Byte-Array konvertieren
		try {
			byte[] crypted2 = Base64.decode(geheim);

			// Entschluesseln
			Cipher cipher2 = Cipher.getInstance("AES");
			cipher2.init(Cipher.DECRYPT_MODE, Server.secretKeySpec);
			byte[] cipherData2 = cipher2.doFinal(crypted2);
			String erg = new String(cipherData2);
			return erg;
		} catch (Exception e) {
			String a = "";
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

		return (String[]) split.toArray();
	}
}
