import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

	private static ServerSocket serverSocket;

	public static int nextID = 0;

	public static void main(String[] args) {

		checkParameter(args);
		
		StringBuilder sb = new StringBuilder();
		for(String s:args) {
			if(s!=null)
				sb.append(s + " ");
		}
		if(sb.length() > 0) {
			sb.insert(0, "Die Parameter ");
			sb.append("werden ignoriert!");
			System.out.println(sb.toString());
		}

		int port = 12345;
		
		try {
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			System.exit(1);
		}

		System.out.println("start");
		new AcceptThread(serverSocket).start();
	}
	
	public static void checkParameter(String[] args) {
		return;
	}

	public static void close() {
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
