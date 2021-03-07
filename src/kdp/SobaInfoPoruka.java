package kdp;

public class SobaInfoPoruka implements Poruka {
	private long vreme;
	private boolean playing;
	private int idSobe;

	public SobaInfoPoruka(long vreme, boolean playing, int idSobe) {
		this.vreme = vreme;
		this.playing = playing;
		this.idSobe = idSobe;
	}
	
	@Override
	public Tip getTip() {
		return Tip.SOBA_INFO;
	}

	public long getVreme() {
		return vreme;
	}
	
	public boolean isPlaying() {
		return playing;
	}
	
	public int getId() {
		return idSobe;
	}
}
