package kdp.centralniServer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Proxy;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import kdp.Konekcija;
import kdp.Poruka;
import kdp.Status;
import kdp.VideoPoruka;
import kdp.VideoProveraPoruka;

public class VideoHandler extends Thread {
	private Konekcija klijent;
	private VideoProveraPoruka start;
	private List<Podserver> podserveri;

	public VideoHandler(Konekcija konekcija, VideoProveraPoruka start, List<Podserver> podserveri) {
		this.klijent = konekcija;
		this.start = start;
		this.podserveri = podserveri;
	}

	@Override
	public void run() {
		Konekcija[] konekcije = null;
		BufferedOutputStream fout = null;

		String dir = "./videoCentralniServer/" + start.getUsername();
		try {
			int i = 0;
			konekcije = new Konekcija[podserveri.size()];

			for (Podserver p : podserveri) {
				konekcije[i] = new Konekcija(p.getIP(), p.getPort());
				konekcije[i].posaljiPoruku(new VideoProveraPoruka(start.getNazivVidea(), start.getUsername(), true));
				i++;
			}

			int cnt = 0;
			for (i = 0; i < podserveri.size(); i++) {
				Status status = (Status) konekcije[i].primiPoruku();
				if (!"OK".equals(status.getStatus())) {
					cnt++;
					konekcije[i].close();
					konekcije[i] = null;
				}
			}

			System.out.println("Cnt = " + cnt);
			if (cnt > 1) {
				klijent.posaljiPoruku(new Status("Video vec postoji"));
				for (i = 0; i < podserveri.size(); i++)
					if (konekcije[i] != null)
						konekcije[i].posaljiPoruku(new Status("Video vec postoji"));
				return;
			}

			klijent.posaljiPoruku(new Status("OK"));

			Files.createDirectories(Paths.get(dir));
			File file = new File(dir + "/" + start.getNazivVidea());
			file.createNewFile();
			fout = new BufferedOutputStream(new FileOutputStream(file));

			Poruka poruka = (Poruka) klijent.primiPoruku();

			while (poruka instanceof VideoPoruka) {
				VideoPoruka video = (VideoPoruka) poruka;
				fout.write(video.getBuffer(), 0, video.getSize());
				for (i = 0; i < podserveri.size(); i++)
					if (konekcije[i] != null)
						konekcije[i].posaljiPoruku(video);
				poruka = (Poruka) klijent.primiPoruku();
			}

			fout.close();

			if (!"OK".equals(((Status) poruka).getStatus()))
				throw new IOException();
			
			for (i = 0; i < podserveri.size(); i++)
				if (konekcije[i] != null)
					konekcije[i].posaljiPoruku(new Status("OK"));

		} catch (IOException | ClassNotFoundException e) {
			for (int i = 0; i < podserveri.size(); i++)
				if (konekcije[i] != null)
					try {
						konekcije[i].posaljiPoruku(new Status("Greska"));
					} catch (IOException e1) {
					}
			try {
				klijent.posaljiPoruku(new Status("Greska"));
			} catch (IOException e2) {
			}
			try {
				fout.close();
			} catch (IOException e2) {
			}
			try {
				Files.delete(Paths.get(dir + "/" + start.getNazivVidea()));
			} catch (IOException e1) {
			}
		} finally {
			try {
				if (fout != null)
					fout.close();
				klijent.close();

				if (konekcije != null)
					for (int i = 0; i < podserveri.size(); i++)
						if (konekcije[i] != null)
							konekcije[i].close();
			} catch (IOException e) {
			}
		}
	}
}
