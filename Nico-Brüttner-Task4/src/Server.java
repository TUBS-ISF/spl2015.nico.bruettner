import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

	private static ServerSocket serverSocket;
	
	public static List<IPluginManipulate> manipulatePlugins = new ArrayList<IPluginManipulate>();
	public static List<IPluginHandshakeServer> handshakePlugins = new ArrayList<IPluginHandshakeServer>();
	public static List<IPluginCommandServer> commandPlugins = new ArrayList<IPluginCommandServer>();

	public static int nextID = 0;

	public static void main(String[] args) {
		
		//#if Verschluesselung
		manipulatePlugins.add(new PluginManipulateVerschluesselung());
		//#endif
		
		//#if History
		manipulatePlugins.add(new PluginManipulateServerHistory());
		//#endif
		
		//#if Spamfilter
		manipulatePlugins.add(new PluginManipulateServerFilter());
		//#endif
		
		//#if PM
		commandPlugins.add(new PluginCommandServerPM());
		//#endif
		
		//#if Passwort
//@		handshakePlugins.add(new PluginHandshakeServerPasswort());
		//#endif
		
		//#if Nickname
//@		handshakePlugins.add(new PluginHandshakeServerUsername());
		//#endif
				
		
		for(IPluginHandshakeServer p:handshakePlugins) {
			String result = p.processArgs(args);
			if(result != null) {
				System.out.println("Parameter " + result + " fehlt!");
				System.exit(0);
			}
		}
		for(IPluginManipulate p:manipulatePlugins) {
			p.init();
		}
		
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

	public static void close() {

		for(IPluginManipulate p:manipulatePlugins) {
			p.close();
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
