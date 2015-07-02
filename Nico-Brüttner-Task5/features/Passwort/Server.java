public class Server {
	
	public static String pass = null;
	public static void main(String[] args) {
		for(int i = 0;i<args.length;i++) {
			System.out.println(i);
			System.out.println(args[i]);
			if("-p".equals(args[i])) {
				if((i+1)<args.length) {
					pass = args[i+1];
					args[i] = null;
					args[i+1] = null;
					break;
				}
			}
		}
		if(pass == null) {
			System.out.println("Parameter -p fehlt");
			System.exit(0);
		}
		original(args);
	}
}