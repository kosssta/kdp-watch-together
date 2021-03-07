package kdp;

public class SobaPoruka implements Poruka {
	private Soba soba;
	private boolean obavestenje;
	
	public SobaPoruka(Soba soba) {
		this.soba = soba;
		this.obavestenje = true;
	}
	
	public SobaPoruka(Soba soba, boolean obavestenje) {
		this.soba = soba;
		this.obavestenje = obavestenje;
	}
	
	@Override
	public Tip getTip() {
		return Tip.SOBA;
	}
	
	public Soba getSoba() {
		return soba;
	}
	
	public boolean getObavestenje() {
		return obavestenje;
	}
}
