import jaco.mp3.player.MP3Player;
import java.io.File;

public aspect Tonausgabe {

	static MP3Player Client.player;
	
	before() : execution(* Client.main(..)) {
		Client.player = new MP3Player(new File("sound.mp3"));
	}
	
	after() : call(* readLine()) && withincode(* Client.run()){
		Client.player.play();
	}
}