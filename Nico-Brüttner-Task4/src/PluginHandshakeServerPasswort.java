
public class PluginHandshakeServerPasswort implements IPluginHandshakeServer {

	private static String parameter = "-p";
	private static String pass = null;

	@Override
	public String processArgs(String[] args) {
		for(int i = 0;i<args.length;i++) {
			if(args[i].equals(parameter)) {
				if((i+1)<args.length) {
					pass = args[i+1];
					args[i] = null;
					args[i+1] = null;
					return null;
				}
			}
		}
		return parameter;
	}

	@Override
	public boolean handleHandshakeParameter(Connection from, String[] args) {
		for(int i = 0;i<args.length;i++) {
			if(args[i].equals(parameter)) {
				if((i+1)<args.length) {
					if(pass.equals(args[i+1]))
					{
						args[i] = null;
						args[i+1] = null;
						return true;
					}
				}
			}
		}
		return false;
	}
}
