package kdp.podserver;

import java.io.IOException;

import javax.swing.SwingUtilities;

import kdp.Konekcija;
import kdp.LoginPoruka;
import kdp.Obavestenje;
import kdp.Poruka;
import kdp.Poruka.Tip;
import kdp.Soba;
import kdp.Status;

public class LoginHandler extends Thread {
	private Podserver podserver;
	private Konekcija klijent;
	private LoginPoruka poruka;

	public LoginHandler(Podserver podserver, Konekcija konekcija, LoginPoruka poruka) {
		this.podserver = podserver;
		this.klijent = konekcija;
		this.poruka = poruka;
	}

	@Override
	public void run() {
		if (poruka.getTip() == Tip.REGISTRACIJA)
			registracija();
		else if (poruka.getTip() == Tip.PRIJAVA)
			if (poruka.isKlijent())
				loginKlijent();
			else
				login();

		if (poruka.getTip() != Tip.PRIJAVA || !poruka.isKlijent())
			try {
				klijent.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private void registracija() {
		try {
			if (podserver.dodajKorisnika(poruka.getKorisnik())) {
				klijent.posaljiPoruku(new Status("Registracija uspesna"));
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						podserver.dodajLog("Registrovan korisnik: " + poruka.getUsername());
					}
				});
				Object p = klijent.primiObjekat();
				while (p instanceof Soba) {
					Soba s = (Soba) p;
					if (s.isAdmin(poruka.getUsername()) || s.isClan(poruka.getUsername()))
						podserver.dodajSobu(s);
					p = klijent.primiObjekat();
				}
			} else {
				klijent.posaljiPoruku(new Status("Greska"));
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void login() {
		try {
			if (podserver.postojiKorisnik(poruka.getUsername())) {
				if (podserver.proveriKorisnika(poruka.getUsername(), poruka.getPassword())) {
					klijent.posaljiPoruku(new Status("OK"));

					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							podserver.dodajLog("Ulogovan korisnik: " + poruka.getUsername());
						}
					});
				} else
					klijent.posaljiPoruku(new Status("Pogresna lozinka"));
			} else {
				klijent.posaljiPoruku(new Status("Korisnicko ime ne postoji"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loginKlijent() {
		podserver.ulogujKorisnika(poruka.getUsername(), klijent);
		String obavestenja = podserver.getObavestenja(poruka.getUsername());
		try {
			klijent.posaljiPoruku(new Obavestenje(obavestenja));
		} catch (IOException e) {
			podserver.dodajObavestenje(poruka.getUsername(), obavestenja);
		}
	}
}
