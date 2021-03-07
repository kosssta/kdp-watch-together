package kdp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class VideoNaziviPoruka implements Poruka {
	private List<VideoNaziv> nazivi;
	private List<Soba> sobe;
	private String korisnik;
	
	public VideoNaziviPoruka(String korisnik) {
		this.korisnik = korisnik;
		nazivi = new ArrayList<VideoNaziv>();
		sobe = new ArrayList<Soba>();
	}

	public void dodajNaziv(String naziv, String username) {
		nazivi.add(new VideoNaziv(naziv, username));
	}

	public void dodajSobu(Soba soba) {
		sobe.add(soba);
	}
	
	public int getVideosSize() {
		return nazivi.size();
	}

	public String getNaziv(int i) {
		return nazivi.get(i).naziv;
	}

	public String getUser(int i) {
		return nazivi.get(i).korisnik;
	}
	
	public List<Soba> getSobe() {
		return sobe;
	}
	
	public String getKorisnik() {
		return korisnik;
	}

	@Override
	public Tip getTip() {
		return Tip.VIDEO_NAZIVI;
	}

}

class VideoNaziv implements Serializable {
	public String naziv;
	public String korisnik;

	public VideoNaziv(String naziv, String korisnik) {
		this.naziv = naziv;
		this.korisnik = korisnik;
	}
}
