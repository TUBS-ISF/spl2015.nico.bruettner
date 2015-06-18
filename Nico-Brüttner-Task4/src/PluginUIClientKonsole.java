import java.util.Scanner;


public class PluginUIClientKonsole  implements IPluginUIClient{

	protected Scanner scanner;
	
	@Override
	public void init(Client main) {
		// TODO Auto-generated method stub
		this.scanner = new Scanner(System.in);
		
		String input;
		while ((input = this.scanner.nextLine()) != null) {
			main.handleUserInput(input);
		}
	}

	@Override
	public void print(String str) {
		System.out.println(str);
	}

}
