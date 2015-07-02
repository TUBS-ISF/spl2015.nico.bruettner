public class Client implements Runnable {
	private void handleSpecialCommand(String[] data) {
		if ("/pm".equals(data[0])) {
			print(data[1] + "(privat): " + data[2]);
		}
		else
			original(data);
	}
}