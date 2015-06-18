public class PluginCommandClientPM implements IPluginCommandClient {

	@Override
	public boolean handleCommand(Client app, String[] data) {
		if ("/pm".equals(data[0])) {
			app.print(data[1] + "(privat): " + data[2]);

			return true;
		}
		return false;
	}
}