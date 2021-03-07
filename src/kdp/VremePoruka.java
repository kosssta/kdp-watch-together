package kdp;

public class VremePoruka implements Poruka {
	private double vreme;
	
	public VremePoruka(double vreme) {
		this.vreme = vreme;
	}
	
	@Override
	public Tip getTip() {
		return Tip.VREME;
	}
	
	public double getVreme() {
		return vreme;
	}
}
