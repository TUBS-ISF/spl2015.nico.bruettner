import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class PluginManipulateServerHistory implements IPluginManipulate {

	private FileWriter fw;
	private BufferedWriter bw;
	
	@Override
	public void init() {
		try {
			fw = new FileWriter("serverHistory.txt");
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void close() {
		try {
			bw.close();
			fw.close();
		} catch (IOException e) {}
	}

	@Override
	public String manipulateIncoming(Connection c, String in, boolean isHandshake) {
		if(!isHandshake)
			writeHistory(c.nick + ": " + in);
		return in;
	}

	@Override
	public String manipulateOutgoing(Connection c, String out) {
		return out;
	}

	private void writeHistory(String str) {
		try {
			bw.write(str);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {}

	}
}
