
public class PluginCommandServerPM implements IPluginCommandServer {

	@Override
	public int handleCommand(Connection from,String command, String data) {
		if("pm".equals(command))
		{
			int n = data.indexOf(" ");
			if (n == -1 || (n + 1) == data.length()) {
				return 0;
			} else {
				String msg = data.substring(n + 1);
				String cl = data.substring(0, n);

				Connection c = Connection.getClientByName(cl);
				if (c != null) {
					c.send("/pm " + from.nick + "  " + msg, false);
				} else {
					from.send("/err  Client nicht gefunden!", false);
				}
			}
			return 1;
		}
		return -1;
	}
}
