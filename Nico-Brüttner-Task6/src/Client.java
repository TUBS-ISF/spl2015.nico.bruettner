import java.io.BufferedReader;
import java.io.IOException;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client implements Runnable {
	
	protected PrintWriter out = null;

	protected BufferedReader in = null;

	protected Thread thread;
	

	public static void main(String args[]) throws IOException {
		if (args.length < 2)
			throw new RuntimeException("Syntax: Client <host> <port> <plugin parameter> ...");
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0;i<args.length;i++) {
			sb.append(args[i] + " ");
		}
		if(sb.length() == 0)
			sb.append(" ");

		new Client(args[0], Integer.parseInt(args[1]), sb.toString());
	}

	public Client(String host, int port, String parameter) {
		try {
			System.out.println("Connecting to " + host + " (port " + port + ")...");
			Socket s = new Socket(host, port);
			this.out = new PrintWriter(s.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
			if (!handshake(parameter)) {
				System.exit(0);
			}

			thread = new Thread(this);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void handleUserInput(String input) {
		input = input.trim();

		if (input.startsWith("/exit")) {
			System.exit(0);
		} else if(input.startsWith("/")) {
			send(input);
		} else {
			send("/msg " + input);
		}
	}

	public boolean handshake(String send) {
		send(send);
		
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
						break;
					
					if (!handleInput(input))
						break;
				} catch (EOFException e) {
					e.printStackTrace();
					System.out.println("4");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			thread = null;
			out.close();
			System.exit(1);
		}
	}

	private boolean handleInput(String input) {
		String[] split = splitInput(input);
		if (split == null || split.length == 0)
			return false;

		if ("/msg".equals(split[0])) {
			print(split[1] + ": " + split[2]);
		}
		else if ("/exit".equals(split[0])) {
			return false;
		} else if ("/err".equals(split[0])) {
			print("Fehler: " + split[1]);			
		}
		else {
			handleSpecialCommand(split);
		}
		return true;
	}
	
	private void handleSpecialCommand(String[] data) {
		return;
	}
	
	protected void print(String str) {
		return;
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
		this.out.println(line);
	}
}
