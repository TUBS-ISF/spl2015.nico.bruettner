public aspect GUI {
	ownGUI Client.gui;
	after(Client c) : execution(Client.new(..)) && this(c) {
		c.gui = new ownGUI();
		c.gui.init(c);
	}
	
	void around (String str,Client c): execution(* Client.print(..))&& args(str)&&this(c) {
		c.gui.print(str);
	}
}