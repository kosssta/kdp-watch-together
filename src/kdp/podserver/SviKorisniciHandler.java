package kdp.podserver;

import java.io.IOException;

import kdp.Konekcija;
import kdp.KorisniciGet;

public class SviKorisniciHandler extends Thread {
	private Podserver podserver;
	private Konekcija klijent;

	public SviKorisniciHandler(Podserver podserver, Konekcija konekcija) {
		this.podserver = podserver;
		this.klijent = konekcija;
	}

	@Override
	public void run() {
		try (Konekcija cs = new Konekcija(podserver.getCentralniServerIP(), podserver.getCentralniServerPort())) {
			cs.posaljiPoruku(new KorisniciGet(null));
			KorisniciGet korisnici = (KorisniciGet) cs.primiPoruku();
			klijent.posaljiPoruku(korisnici);
		} catch (IOException | ClassNotFoundException e) {
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
