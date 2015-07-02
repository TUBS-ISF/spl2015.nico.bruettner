import java.io.IOException;

public class Client implements Runnable {

	private GUI gui;
	
	public void initUI() {
		gui = new GUI();
		gui.init(this);
	}
	
	public void print(String str) {
		gui.print(str);
	}
}