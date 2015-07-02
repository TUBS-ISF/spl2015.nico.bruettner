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
	public String nick = "";

	Connection(ServerSocket serverSocket, Socket client) {
		this.client = client;

		try {
			this.out = new PrintWriter(client.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String input = in.readLine();
			
			input = manipulateIncoming(input, true);
			
			String[] splitted = input.split(" ");
			if(!handleHandshakeParameter(splitted)) {
				send("err 0", true);
				close();
				return;
			}
			
			if(this.nick == null || this.nick.isEmpty())
				this.nick = "client" + (Server.nextID++);

			send("ok", true);
			clients.add(this);
			this.start();
			// handshake ok


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private boolean handleHandshakeParameter(String[] args) {
		return true;
	}

	public void run() {
		try {

			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));

			while (true) {
				String input = in.readLine();
				if (input == null)
					continue;
				System.out.println("Empfange von " + this.nick + ": " + input);
				
				input = manipulateIncoming(input,false);
				
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
	
	private String manipulateIncoming(String in, boolean isHandshake)
	{
		return in;
	}


	private boolean handleInput(String input) {
		int n = input.indexOf(" ");
		if (n == -1 || (n + 1) == input.length()) {
			return false;
		}
		
		String command = input.substring(0, n);
		String data = input.substring(n+1);
		if(!command.startsWith("/"))
			return false;
		else
			command = command.substring(1);
		
		if ("msg".equals(command)) {
			broadcast("/msg " + this.nick + "  " + data, this);
			return true;
		}
		
		if(handleSpecialCommand(command, data))
			return true;
		
		/*for(IPluginCommandServer p:Server.commandPlugins) {
			int result = p.handleCommand(this, command, data);
			if(result == -1)
				continue;
			else if(result == 1)
				return true;
			else if(result == 0)
				return false;
		}*/
		
		this.send("/err  Befehl wird nicht unterstützt!", false);
		return true;
	}
	
	private boolean handleSpecialCommand(String command, String data) {
		return false;
	}

	public void send(String str, boolean isHandshake) {
		
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
