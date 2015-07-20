
public aspect Spamfilter {
	public String[] Connection.filter = {"arsch","arschloch","idiot","trottel"};
	
	private String Connection.replaceFilter(String str) {
		for(int i = 0;i<filter.length;i++) {
			StringBuilder sb = new StringBuilder();
			for(int j = 0;j<filter[i].length();j++) {
				sb.append("*");
			}
			str = str.replaceAll(filter[i], sb.toString());
		}
		return str;
	}
	
	boolean around(String s,Connection c): call(boolean Connection.handleInput(String)) && args(s) && this(c) {
		s = c.replaceFilter(s);
		return proceed(s,c);
	}
}