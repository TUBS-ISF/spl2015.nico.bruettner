import java.util.Scanner;

public class Client implements Runnable {

	protected Scanner scanner;
	
	
	protected void initUI() {
		this.scanner = new Scanner(System.in);
		
		String input;
		while ((input = this.scanner.nextLine()) != null) {
			handleUserInput(input);
		}
	}
	
	protected void print(String str) {
		System.out.println(str);
	}
}