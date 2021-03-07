package kdp.podserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import kdp.Konekcija;
import kdp.Obavestenje;
import kdp.Soba;
import kdp.SobaPoruka;
import kdp.Status;

public class SobaHandler extends Thread {
	private Podserver podserver;
	private Konekcija klijent;
	private SobaPoruka poruka;

	public SobaHandler(Podserver podserver, Konekcija konekcija, SobaPoruka poruka) {
		this.podserver = podserver;
		this.klijent = konekcija;
		this.poruka = poruka;
	}

	@Override
	public void run() {
		if (poruka.getSoba().getId() != 0) {
			Soba soba = poruka.getSoba();
			//soba.obrisiGledaoce();
			if (podserver.postojiKorisnik(soba.getAdmin()))
				podserver.dodajSobu(soba);
			else
				for (String clan : soba.getClanovi())
					if (podserver.postojiKorisnik(clan)) {
						podserver.dodajSobu(soba);
						break;
					}

			podserver.dodajLog("Korisnik " + soba.getAdmin() + " je napravio/la sobu za video " + soba.getVideo());

			String obavestenje = "Korisnik " + soba.getAdmin() + " vas je dodao u sobu za video " + soba.getVideo();
			for (String clan : soba.getClanovi())
				if (podserver.postojiKorisnik(clan)) {
					Konekcija konekcija = podserver.getKonekcija(clan);
					if (konekcija != null) {
						try {
							konekcija.posaljiPoruku(new Obavestenje(obavestenje));
							if (!"OK".equals(((Status) konekcija.primiPoruku()).getStatus()))
								throw new IOException();
						} catch (IOException | ClassNotFoundException e) {
							podserver.ulogujKorisnika(clan, null);
							podserver.dodajObavestenje(clan, obavestenje);
						}
					} else
						podserver.dodajObavestenje(clan, obavestenje);
				}

			try {
				klijent.posaljiPoruku(new Status("OK"));
			} catch (IOException e) {
			} finally {
				try {
					klijent.close();
				} catch (IOException e) {
				}
			}
		} else {
			if (poruka.getObavestenje()) {
				try (Konekcija cs = new Konekcija(podserver.getCentralniServerIP(),
						podserver.getCentralniServerPort())) {
					cs.posaljiPoruku(poruka);
					poruka = (SobaPoruka) cs.primiPoruku();

					if (poruka.getSoba().getId() != 0) {
						klijent.posaljiPoruku(new Status("OK"));
					} else
						klijent.posaljiPoruku(new Status("Greska"));
				} catch (IOException | ClassNotFoundException e) {
					try {
						klijent.posaljiPoruku(new Status("Greska"));
					} catch (IOException e1) {
					}
				} finally {
					try {
						klijent.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				podserver.dodajSobu(poruka.getSoba());
			}
		}
	}
}
