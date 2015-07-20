import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public aspect History {
	public static FileWriter Server.fw;
	public static BufferedWriter Server.bw;
	
	before() : execution(* Server.main(..)) {
		try {
			Server.fw = new FileWriter("serverHistory.txt");
			Server.bw = new BufferedWriter(Server.fw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	before() : execution(* Server.close(..)) {
		try {
			Server.bw.close();
			Server.fw.close();
		} catch (IOException e) {}
	}
	
	public static void Server.writeHistory(String str) {
		try {
			bw.write(str);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {}
	}
	
	after(Connection c) returning(String in) : call(* readLine()) && this(c) && withincode(* Connection.run()){
		Server.writeHistory(c.nick + ": " + in);
	}
}