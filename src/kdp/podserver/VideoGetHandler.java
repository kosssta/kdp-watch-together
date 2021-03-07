package kdp.podserver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import kdp.Konekcija;
import kdp.Poruka;
import kdp.Soba;
import kdp.SobaInfoPoruka;
import kdp.Status;
import kdp.VideoGetPoruka;
import kdp.VideoInfoPoruka;
import kdp.VideoPoruka;
import kdp.VideoStatusPoruka;
import kdp.VremePoruka;

public class VideoGetHandler extends Thread {
	private Podserver podserver;
	private Konekcija klijent;
	private String naziv;
	private String vlasnik;
	private String korisnik;
	private boolean postojiFajl;
	private boolean zatvoriKonekciju;

	public VideoGetHandler(Podserver podserver, Konekcija konekcija, VideoGetPoruka poruka) {
		this.podserver = podserver;
		this.klijent = konekcija;
		this.naziv = poruka.getNaziv();
		this.vlasnik = poruka.getVlasnik();
		this.korisnik = poruka.getKorisnik();
		this.postojiFajl = poruka.getPostojiFajl();
	}

	@Override
	public void run() {
		File video = new File("./videoPodserver" + podserver.getId() + "/" + vlasnik + "/" + naziv);
		BufferedInputStream fin = null;
		zatvoriKonekciju = true;

		try {
			if (!postojiFajl) {
				fin = new BufferedInputStream(new FileInputStream(video));
				VideoGetPoruka poruka = new VideoGetPoruka(naziv, vlasnik, korisnik);
				poruka.setVelicina(video.length());
				klijent.posaljiPoruku(poruka);
				while (!Thread.interrupted()) {
					VideoPoruka p = new VideoPoruka();
					int num_read = fin.read(p.getBuffer(), 0, p.BUFFER_CAPACITY);

					if (num_read <= 0)
						break;

					p.setSize(num_read);
					klijent.posaljiPoruku(p);

					Poruka por = (Poruka) klijent.primiPoruku();
				}
				klijent.posaljiPoruku(new Status("OK"));
			}

			VideoStatusPoruka p = (VideoStatusPoruka) klijent.primiPoruku();

			int idSobe = p.getIdSobe();

			if (idSobe == 0) {
				klijent.posaljiPoruku(p);
				klijent.posaljiPoruku(new VremePoruka(podserver.getVreme(korisnik, naziv, vlasnik)));
				try {
					while (true) {
						VremePoruka v = (VremePoruka) klijent.primiPoruku();
						Konekcija cs = new Konekcija(podserver.getCentralniServerIP(), podserver.getCentralniServerPort());
						cs.posaljiPoruku(new VideoInfoPoruka(korisnik, vlasnik, this.naziv, v.getVreme()));
						cs.close();
						podserver.postaviVreme(korisnik, naziv, vlasnik, v.getVreme());
					}
				} catch (IOException | ClassNotFoundException e) {
				}
				return;
			}

			if (fin != null) {
				fin.close();
				fin = null;
			}

			Soba soba = podserver.getSoba(idSobe);
			if (podserver.postojiKorisnik(soba.getAdmin())) {
				if (soba.isPlaying())
					soba.setVreme(soba.getVreme() + System.currentTimeMillis() - soba.getTimestamp());

				if (soba.isAdmin(korisnik)) {
					klijent.posaljiPoruku(new VideoStatusPoruka(soba.getVreme(), soba.isPlaying()));
					soba.setPlaying(true);
				} else {
					soba.dodajGledaoca(klijent);
					zatvoriKonekciju = false;
					return;
				}
			} else {
				try (Konekcija cs = new Konekcija(podserver.getCentralniServerIP(),
						podserver.getCentralniServerPort())) {
					if (soba.isPlaying())
						soba.setVreme(soba.getVreme() + System.currentTimeMillis() - soba.getTimestamp());

					cs.posaljiPoruku(new VideoStatusPoruka(soba.getVreme(), soba.isPlaying(), soba.getId(),
							soba.getAdmin(), korisnik));
					Status status = (Status) cs.primiPoruku();

					if (!"OK".equals(status.getStatus()))
						return;

					while (true) {
						p = (VideoStatusPoruka) cs.primiPoruku();
						klijent.posaljiPoruku(p);
						soba.setVreme(p.getVreme());
						soba.setPlaying(p.isPlaying());
					}
				} catch (IOException | ClassNotFoundException e) {
				}
			}

			Konekcija cs = null;
			try {
				cs = new Konekcija(podserver.getCentralniServerIP(), podserver.getCentralniServerPort());
				cs.posaljiPoruku(new SobaInfoPoruka(soba.getVreme(), soba.isPlaying(), soba.getId()));
				while (true) {
					p = (VideoStatusPoruka) klijent.primiPoruku();
					cs.posaljiPoruku(new SobaInfoPoruka(p.getVreme(), p.isPlaying(), p.getIdSobe()));
					soba.setVreme(p.getVreme());
					soba.setPlaying(p.isPlaying());
				}
			} catch (IOException e) {
				soba.setVreme(
						soba.getVreme() + (soba.isPlaying() ? System.currentTimeMillis() - soba.getTimestamp() : 0));
				soba.setPlaying(false);
				if (cs != null)
					cs.posaljiPoruku(new SobaInfoPoruka(soba.getVreme(), false, soba.getId()));
				else
					klijent.posaljiPoruku(new Status("Server trenutno nije dostupan"));
			} finally {
				if (cs != null)
					cs.close();
			}
		} catch (IOException | ClassNotFoundException e) {
		} finally {
			try {
				if (fin != null) {
					fin.close();
					fin = null;
				}
				if (zatvoriKonekciju) {
					klijent.close();
				}
			} catch (IOException e) {
			}
		}

	}
}
