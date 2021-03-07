package kdp.centralniServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import kdp.Konekcija;
import kdp.Soba;
import kdp.Status;
import kdp.VideoStatusPoruka;

public class GledanjeHandler extends Thread {
	private CentralniServer cs;
	private Konekcija klijent;
	private VideoStatusPoruka poruka;
	private List<Podserver> podserveri;
	private int idSobe;

	public GledanjeHandler(CentralniServer cs, Konekcija konekcija, VideoStatusPoruka poruka, List<Podserver> podserveri) {
		this.cs = cs;
		this.klijent = konekcija;
		this.poruka = poruka;
		this.idSobe = poruka.getIdSobe();
		this.podserveri = podserveri;
	}

	@Override
	public void run() {
		Konekcija podserver = null;

		long timestamp = 0;
		try {
			Podserver p = null;
			for (Podserver ps : podserveri) {
				if (ps.postojiKorisnik(poruka.getAdmin())) {
					p = ps;
					break;
				}
			}

			if (p == null) {
				klijent.posaljiPoruku(new Status("Greska"));
				return;
			}

			klijent.posaljiPoruku(new Status("OK"));

			podserver = new Konekcija(p.getIP(), p.getPort());

			podserver.posaljiPoruku(poruka);

			while (!Thread.interrupted()) {
				poruka = (VideoStatusPoruka) podserver.primiPoruku();
				timestamp = System.currentTimeMillis();
				klijent.posaljiPoruku(poruka);
			}

		} catch (IOException | ClassNotFoundException e) {
			try {
				Soba soba = null;
				if (idSobe > 0) {
					List<Soba> sobe = cs.getSobe();
					for (Soba s : sobe)
						if (s.getId() == idSobe) {
							soba = s;
							break;
						}
				}
				if (soba != null) {
					soba.setVreme(timestamp > 0 && poruka.isPlaying() ? poruka.getVreme() + System.currentTimeMillis() - timestamp
							: poruka.getVreme());
					soba.setPlaying(false);
				}
				klijent.posaljiPoruku(new VideoStatusPoruka(soba.getVreme(), false, true));
			} catch (IOException e1) {
			}
		} finally {
			try {
				if (podserver != null)
					podserver.close();
				klijent.close();
			} catch (IOException e) {
				// e.printStackTrace();
			}

		}
	}
}
