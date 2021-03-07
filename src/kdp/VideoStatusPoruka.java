package kdp;

public class VideoStatusPoruka implements Poruka {
	private long vreme;
	private boolean playing;
	private long timestamp;
	
	private int idSobe;
	private String admin;
	private String korisnik;
	private boolean stop;
	
	public VideoStatusPoruka(long vreme, boolean playing) {
		this.vreme = vreme;
		this.playing = playing;
		this.timestamp = System.currentTimeMillis();
	}
	
	public VideoStatusPoruka(long vreme, boolean playing, boolean stop) {
		this.vreme = vreme;
		this.playing = playing;
		this.stop = stop;
		this.timestamp = System.currentTimeMillis();
	}
	
	public VideoStatusPoruka(long vreme, boolean playing, int idSobe, String admin, String korisnik) {
		this.vreme = vreme;
		this.playing = playing;
		this.idSobe = idSobe;
		this.admin = admin;
		this.korisnik = korisnik;
		this.timestamp = System.currentTimeMillis();
	}
	
	public VideoStatusPoruka(long vreme, boolean playing, int idSobe, String admin, String korisnik, boolean stop) {
		this.vreme = vreme;
		this.playing = playing;
		this.idSobe = idSobe;
		this.admin = admin;
		this.korisnik = korisnik;
		this.stop = stop;
		this.timestamp = System.currentTimeMillis();
	}
	
	@Override
	public Tip getTip() {
		return Tip.VIDEO_STATUS;
	}

	public long getVreme() {
		return vreme;
	}

	public boolean isPlaying() {
		return playing;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	public int getIdSobe() {
		return idSobe;
	}
	
	public String getAdmin() {
		return admin;
	}
	
	public String getKorisnik() {
		return korisnik;
	}
	
	public void setVreme(long vreme) {
		this.vreme = vreme;
	}
	
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}
	
	public boolean isStop() {
		return stop;
	}
}
