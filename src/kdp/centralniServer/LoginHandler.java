package kdp.centralniServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import kdp.Konekcija;
import kdp.LoginPoruka;
import kdp.Obavestenje;
import kdp.Soba;
import kdp.Status;

public class LoginHandler extends Thread {
	private CentralniServer cs;
	private Konekcija klijent;
	private List<Podserver> podserveri;
	private LoginPoruka poruka;
	private Podserver p;

	public LoginHandler(Konekcija konekcija, List<Podserver> podserveri, LoginPoruka poruka) {
		this.klijent = konekcija;
		this.podserveri = podserveri;
		this.poruka = poruka;
	}

	public LoginHandler(CentralniServer cs, List<Podserver> podserveri, LoginPoruka poruka) {
		this.cs = cs;
		this.podserveri = podserveri;
		this.poruka = poruka;
	}

	public LoginHandler(CentralniServer cs, List<Podserver> podserveri, LoginPoruka poruka, Podserver p) {
		this.cs = cs;
		this.podserveri = podserveri;
		this.poruka = poruka;
		this.p = p;
	}

	public void run() {
		switch (poruka.getTip()) {
		case REGISTRACIJA:
			registracija();
			break;
		case PRIJAVA:
			login();
			break;
		}

		try {
			if (klijent != null)
				klijent.close();
		} catch (IOException e) {
		}
	}

	private void registracija() {
		Konekcija podserver = null;

		try {
			if (podserveri.size() == 0) {
				if (klijent != null)
					klijent.posaljiPoruku(new Status("Greska"));
				return;
			}

			Podserver dodavanje = null;
			if (this.p == null) {
				int min = Integer.MAX_VALUE;
				for (Podserver p : podserveri) {
					if (p.postojiKorisnik(poruka.getUsername())) {
						if (klijent != null)
							klijent.posaljiPoruku(new Status("Korisnicko ime vec postoji"));
						return;
					}
					int br = p.getSize();
					if (br < min) {
						min = br;
						dodavanje = p;
					}
				}
			} else
				dodavanje = p;
			
			podserver = new Konekcija(dodavanje.getIP(), dodavanje.getPort());

			podserver.posaljiPoruku(poruka);
			dodavanje.dodajKorisnika(poruka.getUsername(), poruka.getPassword());
			Status status = (Status) podserver.primiPoruku();

			if (klijent != null)
				klijent.posaljiPoruku(status);

			if (cs != null && "Registracija uspesna".equals(status.getStatus())) {
				List<Soba> sobe = cs.getSobe(poruka.getUsername());
				for (Soba s : sobe)
					podserver.posaljiObjekat(s);
			}
			podserver.posaljiObjekat(new Status("OK"));
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (podserver != null)
					podserver.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void login() {
		if (podserveri.size() == 0) {
			try {
				klijent.posaljiPoruku(new Status("Greska"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		Konekcija podserver = null;
		for (Podserver p : podserveri) {
			if (p.postojiKorisnik(poruka.getUsername())) {
				try {
					podserver = new Konekcija(p.getIP(), p.getPort());

					podserver.posaljiPoruku(poruka);
					Status status = (Status) podserver.primiPoruku();
					if (klijent != null && !"OK".equals(status.getStatus()))
						klijent.posaljiPoruku(status);
					else {
						klijent.posaljiPoruku(new Status("" + p.getIP() + "#" + p.getPort()));
					}
				} catch (IOException | ClassNotFoundException e) {
					try {
						if (klijent != null)
							klijent.posaljiPoruku(new Status("Greska"));
					} catch (IOException e1) {
					}
				} finally {
					try {
						if (podserver != null)
							podserver.close();
					} catch (IOException e) {
					}
				}

				return;
			}
		}

		try {
			if (klijent != null)
				klijent.posaljiPoruku(new Status("Korisnicko ime ne postoji"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
