package kdp;

public class VideoInfoPoruka implements Poruka {
	private String username;
	private String vlasnik;
	private String video;
	private double vreme;
	
	public VideoInfoPoruka(String username, String vlasnik, String video, double vreme) {
		this.username = username;
		this.vlasnik = vlasnik;
		this.video = video;
		this.vreme = vreme;
	}
	
	@Override
	public Tip getTip() {
		return Tip.VIDEO_INFO;
	}

	public String getUser() {
		return username;
	}
	
	public String getVlasnik() {
		return vlasnik;
	}
	
	public String getVideo() {
		return video;
	}
	
	public double getVreme() {
		return vreme;
	}
}
