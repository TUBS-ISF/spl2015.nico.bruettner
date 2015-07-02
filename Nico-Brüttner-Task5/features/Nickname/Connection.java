public class Connection {
	private boolean handleHandshakeParameter(String[] args) {
		if(!original(args))
			return false;
		
		for(int i = 0;i<args.length;i++) {
			if(args[i].equals("-u")) {
				if((i+1)<args.length) {
					String nick = args[i+1];
					if(nick.isEmpty())
						return false;
					
					Connection c = Connection.getClientByName(nick);
					if(c != null)
						return false;
					
					this.nick = nick;
					return true;
				}
			}
		}
		return false;
	}
}