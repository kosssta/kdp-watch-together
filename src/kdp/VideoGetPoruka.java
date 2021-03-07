package kdp;

public class VideoGetPoruka implements Poruka {
	private String naziv;
	private String vlasnik;
	private String korisnik;
	private long velicina;
	private boolean postojiFajl = true;
	
	public VideoGetPoruka(String naziv, String vlasnik, String korisnik) {
		this.naziv = naziv;
		this.vlasnik = vlasnik;
		this.korisnik = korisnik;
	}
	
	public VideoGetPoruka(String naziv, String vlasnik, String korisnik, boolean postojiFajl) {
		this.naziv = naziv;
		this.vlasnik = vlasnik;
		this.korisnik = korisnik;
		this.postojiFajl = postojiFajl;
	}

	public String getNaziv() {
		return naziv;
	}
	
	public String getVlasnik() {
		return vlasnik;
	}
	
	public long getVelicina() {
		return velicina;
	}
	
	public void setVelicina(long velicina) {
		this.velicina = velicina;
	}
	
	public String getKorisnik() {
		return korisnik;
	}
	
	@Override
	public Tip getTip() {
		return Tip.VIDEO_GET;
	}
	
	public boolean getPostojiFajl() {
		return postojiFajl;
	}
}
