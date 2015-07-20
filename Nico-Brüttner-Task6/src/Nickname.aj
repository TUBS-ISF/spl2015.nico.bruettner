
public aspect Nickname {
	after(Connection c) returning(String[] args): call(String[] String.split(String)) && this(c){
		c.lastError = 2;
		
		for(int i = 0;i<args.length;i++) {
			if(args[i].equals("-u")) {
				if((i+1)<args.length) {
					String nick = args[i+1];
					if(nick.isEmpty())
						c.lastError = 3;
					
					Connection c2 = Connection.getClientByName(nick);
					if(c2 != null)
						c.lastError = 4;
					else {			
						c.nick = nick;
						c.lastError = -1;
					}
					break;
				}
			}
		}
		
		if(c.lastError != -1) {
			c.send("err "+c.lastError);
			c.close();
		}
	}
}