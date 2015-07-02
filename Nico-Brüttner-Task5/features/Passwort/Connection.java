public class Connection {
	private boolean handleHandshakeParameter(String[] args) {
		if(!original(args))
			return false;
		
		for(int i = 0;i<args.length;i++) {
			if(args[i].equals("-p")) {
				if((i+1)<args.length) {
					if(Server.pass.equals(args[i+1]))
						return true;
					else
						return false;
				}
			}
		}
		return false;
	}
}