
public aspect Admin {
	after(Connection c) : set(String Connection.nick) && withincode(Connection.new(..)) && this(c) {
		if(Connection.clients.size() == 0) {
			c.nick = "admin";
			Server.nextID--;
		}
	}
	
	boolean around(Connection conn,String command, String data) : execution(* Connection.handleSpecialCommand(..)) && this(conn) && args(command,data) {
		
		if("kick".equals(command))
		{
			if(!conn.nick.equals("admin")) {
				conn.send("/err  Du bist kein Admin!");
				return true;
			}
			Connection c = Connection.getClientByName(data);
			
			if (c != null) {
				c.close();
			} else {
				conn.send("/err  Client nicht gefunden!");
			}
			return true;
		}
		
		return proceed(conn,command,data);
	}
}