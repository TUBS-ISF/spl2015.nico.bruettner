import java.util.Scanner;

public aspect Konsole {
	private Scanner Client.scanner;
	
	after(Client c) : execution(Client.new(..)) && this(c) {
		c.scanner = new Scanner(System.in);
		
		String input;
		while ((input = c.scanner.nextLine()) != null) {
			c.handleUserInput(input);
		}
	}
	

	
	void around(String str): execution(void Client.print(String))&& args(str) {
		System.out.println(str);
	}
}