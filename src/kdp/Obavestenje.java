package kdp;

public class Obavestenje implements Poruka {
	private String obavestenja;
	
	public Obavestenje(String obavestenja) {
		this.obavestenja = obavestenja;
	}
	
	@Override
	public Tip getTip() {
		return Tip.OBAVESTENJE;
	}

	public String getObavestenja() {
		return obavestenja;
	}
}
