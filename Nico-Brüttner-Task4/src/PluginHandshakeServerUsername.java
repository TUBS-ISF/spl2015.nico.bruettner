
public class PluginHandshakeServerUsername implements IPluginHandshakeServer {
	
	@Override
	public boolean handleHandshakeParameter(Connection from, String[] args) {
		for(int i = 0;i<args.length;i++) {
			if(args[i].equals("-u")) {
				if((i+1)<args.length) {
					String nick = args[i+1];
					if(nick.isEmpty())
						return false;
					
					Connection c = Connection.getClientByName(nick);
					if(c != null)
						return false;
					
					from.nick = nick;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String processArgs(String[] args) {
		// TODO Auto-generated method stub
		return null;
	}
}
