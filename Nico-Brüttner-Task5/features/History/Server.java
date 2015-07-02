import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Server {
	public static FileWriter fw;
	public static BufferedWriter bw;
	
	public static void main(String[] args) {
		try {
			fw = new FileWriter("serverHistory.txt");
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		original(args);
	}
	
	public static void close() {
		try {
			bw.close();
			fw.close();
		} catch (IOException e) {}
		
		original();
	}
	
	public static void writeHistory(String str) {
		try {
			bw.write(str);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {}
	}
}