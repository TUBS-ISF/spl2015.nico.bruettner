public interface IPluginManipulate {
	void init();
	void close();
	String manipulateIncoming(Connection c, String in, boolean isHandshake);
	String manipulateOutgoing(Connection c, String out);
}