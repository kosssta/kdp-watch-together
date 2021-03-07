package kdp;

import java.io.Serializable;
import java.util.HashMap;

public class Korisnik implements Serializable {
	private String username;
	private String password;
	private Konekcija konekcija;
	private String obavestenja;
	private HashMap<String, Double> startVreme;

	public Korisnik(String username, String password) {
		this.username = username;
		this.password = password;
		this.startVreme = new HashMap<>();
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public Konekcija getKonekcija() {
		return konekcija;
	}

	public void setKonekcija(Konekcija konekcija) {
		this.konekcija = konekcija;
	}

	public synchronized String getObavestenja() {
		String ret = obavestenja;
		obavestenja = null;
		return ret;
	}

	public synchronized void dodajObavestenje(String obavestenja) {
		if (this.obavestenja != null)
			this.obavestenja += "\n" + obavestenja;
		else
			this.obavestenja = obavestenja;
	}

	public boolean proveri(String password) {
		return password.equals(this.password);
	}

	public void dodajStartVreme(String nazivVidea, String vlasnikVidea, double vreme) {
		startVreme.put(vlasnikVidea + "/" + nazivVidea, vreme);
	}

	public double getStartVreme(String nazivVidea, String vlasnikVidea) {
		String naziv = vlasnikVidea + "/" + nazivVidea;
		if (!startVreme.containsKey(naziv))
			startVreme.put(naziv, 0.0);
		return startVreme.get(naziv);
	}
}
