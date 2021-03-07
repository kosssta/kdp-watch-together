package kdp;

public class VideoProveraPoruka implements Poruka {
	private String nazivVidea;
	private String username;
	private boolean bezProvereDalje;
	
	public VideoProveraPoruka(String naziv, String username) {
		init(naziv, username, false);
	}	

	public VideoProveraPoruka(String naziv, String username, boolean provera) {
		init(naziv, username, provera);
	}
	
	private void init(String naziv, String username, boolean provera) {
		this.nazivVidea = naziv;
		this.username = username;
		this.bezProvereDalje = provera;
	}
	
	@Override
	public Tip getTip() {
		return Tip.VIDEO_PROVERA;
	}
	
	public String getNazivVidea() {
		return nazivVidea;
	}

	public String getUsername() {
		return username;
	}
	
	public boolean bezProvereDalje() {
		return bezProvereDalje;
	}
}
