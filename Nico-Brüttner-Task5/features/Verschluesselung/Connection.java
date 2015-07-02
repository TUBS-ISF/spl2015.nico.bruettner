public class Connection {
	private String manipulateIncoming(String in, boolean isHandshake)
	{
		in = Server.decrypt(in);
		return original(in,isHandshake);
	}
	
	public void send(String str, boolean isHandshake) {
		str = Server.encrypt(str);
		original(str,isHandshake);
	}
}