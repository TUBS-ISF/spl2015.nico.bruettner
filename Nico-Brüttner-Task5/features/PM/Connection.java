class Connection extends Thread {
	private boolean handleSpecialCommand(String command, String data) {
		if(!original(command,data) && "pm".equals(command))
		{
			int n = data.indexOf(" ");
			if (n != -1 && (n + 1) != data.length()) {
				String msg = data.substring(n + 1);
				String cl = data.substring(0, n);

				Connection c = Connection.getClientByName(cl);
				if (c != null) {
					c.send("/pm " + this.nick + "  " + msg, false);
				} else {
					this.send("/err  Client nicht gefunden!", false);
				}
				return true;
			}
		}
		return false;
	}
}