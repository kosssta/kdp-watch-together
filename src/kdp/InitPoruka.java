package kdp;

public class InitPoruka implements Poruka {
	private String IP;
	private int port;
	
	public InitPoruka(String IP, int port) {
		this.IP = IP;
		this.port = port;
	}
	
	@Override
	public Tip getTip() {
		return Tip.INIT;
	}

	public String getIP() {
		return IP;
	}
	
	public int getPort() {
		return port;
	}
	
}
