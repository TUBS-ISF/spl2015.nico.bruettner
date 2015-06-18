
public class PluginManipulateServerFilter implements IPluginManipulate {

	public String[] filter = {"arsch","arschloch","idiot","trottel"};

	@Override
	public String manipulateIncoming(Connection c, String in, boolean isHandshake) {
		if(!isHandshake)
			return replaceFilter(in);
		else
			return in;
	}

	@Override
	public String manipulateOutgoing(Connection c, String out) {
		return out;
	}
	
	private String replaceFilter(String str) {
		for(int i = 0;i<filter.length;i++) {
			StringBuilder sb = new StringBuilder();
			for(int j = 0;j<filter[i].length();j++) {
				sb.append("*");
			}
			str = str.replaceAll(filter[i], sb.toString());
		}
		return str;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
