package kdp.podserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import kdp.Konekcija;
import kdp.Soba;
import kdp.VideoNaziviPoruka;

public class VideoNaziviHandler extends Thread {
	private Podserver podserver;
	private Konekcija klijent;
	private String korisnik;

	public VideoNaziviHandler(Podserver podserver, Konekcija konekcija, String korisnik) {
		this.podserver = podserver;
		this.klijent = konekcija;
		this.korisnik = korisnik;
	}

	@Override
	public void run() {
		VideoNaziviPoruka poruka = new VideoNaziviPoruka(null);
		File dir = new File("./videoPodserver" + podserver.getId());

		File[] listUsers = dir.listFiles();
		if (listUsers != null) {
			for (File u : listUsers) {
				if (u.isDirectory()) {
					File[] listFiles = u.listFiles();
					for (File f : listFiles) {
						if (f.isFile() && !podserver.isVideoUOtpremanju(f.getName(), u.getName()))
							poruka.dodajNaziv(f.getName(), u.getName());
					}
				}
			}
		}
		
		List<Soba> sobe = podserver.getSobe(korisnik);
		for (Soba s : sobe)
			poruka.dodajSobu(s);

		try {
			klijent.posaljiPoruku(poruka);
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
