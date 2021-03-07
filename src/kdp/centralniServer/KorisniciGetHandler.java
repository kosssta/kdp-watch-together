package kdp.centralniServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import kdp.Konekcija;
import kdp.KorisniciGet;
import kdp.Korisnik;

public class KorisniciGetHandler extends Thread {
	private Konekcija klijent;
	private List<Podserver> podserveri;

	public KorisniciGetHandler(Konekcija konekcija, List<Podserver> podserveri) {
		this.klijent = konekcija;
		this.podserveri = podserveri;
	}

	@Override
	public void run() {
		try {
			List<String> korisnici = new ArrayList<>();
			if (podserveri != null)
				for (Podserver p : podserveri) {
					if (p.getKorisnici() != null)
						for (Korisnik k : p.getKorisnici())
							korisnici.add(k.getUsername());
				}

			klijent.posaljiPoruku(new KorisniciGet(korisnici));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				klijent.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
