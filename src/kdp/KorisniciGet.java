package kdp;

import java.util.ArrayList;
import java.util.List;

public class KorisniciGet implements Poruka {
	private List<String> korisnici = new ArrayList<String>();
	
	public KorisniciGet(List<String> korisnici) {
		this.korisnici = korisnici;
	}
	
	@Override
	public Tip getTip() {
		return Tip.KORISNICI_GET;
	}

	public List<String> getKorisnici() {
		return korisnici;
	}
	
	public void dodajKorisnika(String korisnik) {
		korisnici.add(korisnik);
	}
}
