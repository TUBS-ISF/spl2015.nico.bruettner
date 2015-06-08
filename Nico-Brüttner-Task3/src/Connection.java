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
			//#if (Passwort || Nickname)
//@			String input = in.readLine();
			//#if (Verschluesselung)
//@			input = decrypt(input);
			//#endif
//@			String[] args = input.split(" ");
			//#if (Passwort && Nickname)
//@			int n = 2;
			//#else
//@			int n = 1;
			//#endif
//@			if (args.length != n) {
//@				send("err 0", true);
//@				close();
//@				return;
//@			} 
			//#endif

			//#if (Nickname)
//@			this.nick = args[0];
//@			if (this.nick.length() == 0) {
//@				send("err 2", true);
//@				close();
//@				return;
//@			}
			//#else
			this.nick = "client" + (Server.nextID++);
			//#endif
			
			//#if (Passwort)
//@			if (Server.pass.length() > 0) {
//@				if (!Server.pass.equals(args[n-1])) {
//@					send("err 1", true);
//@					close();
//@					return;
//@				}
//@			}
			//#endif

			send("ok", true);
			clients.add(this);
			this.start();
			// handshake ok


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
				//#if (Verschluesselung)
				input = decrypt(input);
				//#endif
				//#if (Spamfilter)
				input = replaceFilter(input);
				//#endif
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
	
	//#if (Spamfilter)
	private String replaceFilter(String str) {
		for(int i = 0;i<Server.filter.length;i++) {
			StringBuilder sb = new StringBuilder();
			for(int j = 0;j<Server.filter[i].length();j++) {
				sb.append("*");
			}
			str = str.replaceAll(Server.filter[i], sb.toString());
		}
		return str;
	}
	//#endif

	private boolean handleInput(String input) {
		//#if (PM)
		if (input.startsWith("/pm ")) {
			int n = input.indexOf(" ", 4);
			if (n == -1 || (n + 1) == input.length()) {
				return false;
			} else {
				String msg = input.substring(n + 1);
				String cl = input.substring(4, n);

				Connection c = getClientByName(cl);
				if (c != null) {
					c.send("/pm " + this.nick + "  " + msg, false);

					//#if (History)
					Server.writeHistory(this.nick + " -> " + c.nick + ": " + msg);
					//#endif
				} else {
					this.send("/err  Client nicht gefunden!", false);
				}
			}
		} else 
		//#endif
		if (input.startsWith("/msg ")) {
			input = input.substring(5);

			broadcast("/msg " + this.nick + "  " + input, this);
			//#if (History)
			Server.writeHistory(this.nick + ": " + input);
			//#endif
		}
		return true;
	}

	public void send(String str, boolean isHandshake) {
		//#if (Verschluesselung)
			str = encrypt(str);
		//#endif
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

	//#if (Verschluesselung)
	public String encrypt(String text) {
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

		return (String[]) split.toArray();
	}
}
