public interface IPluginHandshakeServer {
	String processArgs(String[] args);
	boolean handleHandshakeParameter(Connection from, String[] args);
}