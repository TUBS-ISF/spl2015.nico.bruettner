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

	private PrintWriter out = null;
	private BufferedReader in = null;
	public String nick;
	int lastError = -1;

	Connection(ServerSocket serverSocket, Socket client) {
		this.client = client;

		try {
			this.out = new PrintWriter(client.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String input = in.readLine();
			
			String[] splitted = input.split(" ");
			
			if(this.lastError != -1)
				return;
			
			if(this.nick == null || this.nick.isEmpty())
				this.nick = "client" + (Server.nextID++);

			send("ok");
			clients.add(this);
			this.start();
			// handshake ok


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void run() {
		try {

			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));

			while (true) {
				String input = in.readLine();

				System.out.println("Empfange von " + this.nick + ": " + input);
				
				boolean result = handleInput(input);
				if (!result)
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
		int n = input.indexOf(" ");
		String command = null;
		String data = null;
		if (n == -1) {
			command = input;
			data = "";
		}
		else {
			command = input.substring(0, n);
			data = input.substring(n+1);
		}

		if(!command.startsWith("/") || command.length() <= 1)
			return false;
		else
			command = command.substring(1);
		
		if ("msg".equals(command)) {
			broadcast("/msg " + this.nick + "  " + data, this);
			return true;
		}
		
		if(handleSpecialCommand(command, data))
			return true;
		
		this.send("/err  Befehl wird nicht unterstützt!");
		return true;
	}
	
	private boolean handleSpecialCommand(String command, String data) {
		return false;
	}

	public void send(String str){
		
		this.out.println(str);
		System.out.println("Sende an " + this.nick + ": " + str);
	}

	public static void broadcast(String str, Connection except) {
		for (Connection c : Connection.clients) {
			if (c != except)
				c.send(str);
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
}
