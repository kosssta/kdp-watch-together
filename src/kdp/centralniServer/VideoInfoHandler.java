package kdp.centralniServer;

import java.io.IOException;

import kdp.Konekcija;
import kdp.Korisnik;
import kdp.VideoInfoPoruka;

public class VideoInfoHandler extends Thread {
	private CentralniServer cs;
	private Konekcija konekcija;
	private VideoInfoPoruka poruka;
	
	public VideoInfoHandler(CentralniServer cs, Konekcija konekcija, VideoInfoPoruka poruka) {
		this.cs = cs;
		this.konekcija = konekcija;
		this.poruka = poruka;
	}
	
	@Override
	public void run() {
		Korisnik kor = cs.getKorisnik(poruka.getUser());
		
		if (kor != null)
			kor.dodajStartVreme(poruka.getVideo(), poruka.getVlasnik(), poruka.getVreme());
		
		try {
			konekcija.close();
		} catch (IOException e) {
		}
	}
}
