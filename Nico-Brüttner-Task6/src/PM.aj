
public aspect PM {
	before(Client c,String[] data) : execution(* Client.handleSpecialCommand(..)) && this(c) && args(data) {
		if (data[0] != null && "/pm".equals(data[0])) {
			c.print(data[1] + "(privat): " + data[2]);
			data[0] = null;
		}
	}
	
	boolean around(Connection conn,String command, String data) : execution(* Connection.handleSpecialCommand(..)) && this(conn) && args(command,data) {
		
		if("pm".equals(command))
		{
			int n = data.indexOf(" ");
			if (n != -1 && (n + 1) != data.length()) {
				String msg = data.substring(n + 1);
				String cl = data.substring(0, n);

				Connection c = Connection.getClientByName(cl);
				if (c != null) {
					c.send("/pm " + conn.nick + "  " + msg);
				} else {
					conn.send("/err  Client nicht gefunden!");
				}
				return true;
			}
		}
		return proceed(conn,command,data);
	}
}