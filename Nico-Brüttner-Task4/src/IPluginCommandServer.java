
public interface IPluginCommandServer {
	int handleCommand(Connection from, String command, String data);
}
