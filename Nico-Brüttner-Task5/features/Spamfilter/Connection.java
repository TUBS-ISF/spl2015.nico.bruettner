public class Connection {
	public String[] filter = {"arsch","arschloch","idiot","trottel"};
	
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
	
	private String manipulateIncoming(String in, boolean isHandshake) {
		in = original(in,isHandshake);
		if(!isHandshake)
			return replaceFilter(in);
		else
			return in;
	}
}