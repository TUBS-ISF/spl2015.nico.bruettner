public class Connection {
	private String manipulateIncoming(String in, boolean isHandshake) {
		in = original(in,isHandshake);
		if(!isHandshake)
			Server.writeHistory(this.nick + ": " + in);
		return in;
	}
}