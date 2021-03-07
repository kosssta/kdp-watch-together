package kdp;

public class LoginPoruka implements Poruka {
	private Tip tip;
	private Korisnik korisnik;
	private boolean klijent;

	public LoginPoruka(String username, String password, Tip tip) {
		this.korisnik = new Korisnik(username, password);
		this.tip = tip;
	}
	
	public LoginPoruka(Korisnik korisnik, Tip tip) {
		this.korisnik = korisnik;
		this.tip = tip;
	}
	
	public LoginPoruka(String username, String password, Tip tip, boolean klijent) {
		this.korisnik = new Korisnik(username, password);
		this.tip = tip;
		this.klijent = klijent;
	}

	public String getUsername() {
		return korisnik.getUsername();
	}
	
	public String getPassword() {
		return korisnik.getPassword();
	}
	
	public Korisnik getKorisnik() {
		return korisnik;
	}
	
	public boolean isKlijent() {
		return klijent;
	}
	
	@Override
	public Tip getTip() {
		return tip;
	}

}
