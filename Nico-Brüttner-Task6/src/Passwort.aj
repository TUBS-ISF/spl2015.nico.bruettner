
public aspect Passwort {
	public static String Server.pass = null;
	
	before(String[] args) : execution(void Server.main(..)) && args(args) {
		for(int i = 0;i<args.length;i++) {
			System.out.println(i);
			System.out.println(args[i]);
			if("-p".equals(args[i])) {
				if((i+1)<args.length) {
					Server.pass = args[i+1];
					args[i] = null;
					args[i+1] = null;
					break;
				}
			}
		}
		if(Server.pass == null) {
			System.out.println("Parameter -p fehlt");
			System.exit(0);
		}
	}

	after(Connection c) returning(String[] args): call(String[] String.split(String)) && this(c){
		c.lastError = 0;
		
		for(int i = 0;i<args.length;i++) {
			if(args[i].equals("-p")) {
				if((i+1)<args.length) {
					if(!Server.pass.equals(args[i+1]))
						c.lastError = 1;
					else
						c.lastError = -1;
					break;
				}
			}
		}
		
		if(c.lastError != -1) {
			c.send("err "+c.lastError);
			c.close();
		}
	}
}