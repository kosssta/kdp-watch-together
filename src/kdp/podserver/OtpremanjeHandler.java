package kdp.podserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import kdp.Konekcija;
import kdp.Poruka;
import kdp.Status;
import kdp.VideoPoruka;
import kdp.VideoProveraPoruka;
import kdp.klijent.Klijent;

public class OtpremanjeHandler extends Thread {
	private Podserver podserver;
	private Konekcija klijent;
	private VideoProveraPoruka start;

	public OtpremanjeHandler(Podserver podserver, Konekcija konekcija, VideoProveraPoruka poruka) {
		this.podserver = podserver;
		this.klijent = konekcija;
		this.start = poruka;
	}

	@Override
	public void run() {
		Konekcija cs = null;

		String dir = "./videoPodserver" + podserver.getId() + "/" + start.getUsername();
		FileOutputStream fos = null;
		try {
			if (postojiFajl(dir, start.getNazivVidea())) {
				klijent.posaljiPoruku(new Status("Video " + start.getNazivVidea() + " vec postoji na serveru"));
				return;
			}

			if (!start.bezProvereDalje()) {
				cs = new Konekcija(podserver.getCentralniServerIP(), podserver.getCentralniServerPort());

				cs.posaljiPoruku(new VideoProveraPoruka(start.getNazivVidea(), start.getUsername(), false));
				Status status = (Status) cs.primiPoruku();

				if (!"OK".equals(status.getStatus())) {
					klijent.posaljiPoruku(new Status("Video " + start.getNazivVidea() + " vec postoji na serveru"));
					return;
				} else
					klijent.posaljiPoruku(new Status("OK"));
			} else
				klijent.posaljiPoruku(new Status("OK"));

			podserver.dodajVideoUOtpremanju(start.getNazivVidea(), start.getUsername());
			fos = new FileOutputStream(new File(dir + "/" + start.getNazivVidea()));
			Poruka poruka = (Poruka) klijent.primiPoruku();

			while (poruka instanceof VideoPoruka) {
				VideoPoruka video = (VideoPoruka) poruka;
				fos.write(video.getBuffer(), 0, video.getSize());
				if (cs != null)
					cs.posaljiPoruku(video);
				poruka = (Poruka) klijent.primiPoruku();
			}

			fos.close();

			Status status = (Status) poruka;
			if (!"OK".equals(status.getStatus())) {
				System.out.println("Not ok");
				Files.delete(Paths.get(dir + "/" + start.getNazivVidea()));
				if (cs != null)
					cs.posaljiPoruku(new Status("Greska"));
			} else if (cs != null)
				cs.posaljiPoruku(new Status("OK"));

			if (!start.bezProvereDalje())
				klijent.posaljiPoruku(new Status("OK"));

		} catch (ClassNotFoundException | IOException e) {
			if (cs != null)
				try {
					cs.posaljiPoruku(new Status("Greska"));
				} catch (IOException e1) {
				}
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e2) {
				}
			try {
				System.out.println("Not ok");
				Files.delete(Paths.get(dir + "/" + start.getNazivVidea()));
			} catch (IOException e1) {
			}
			try {
				klijent.posaljiPoruku(new Status("Greska"));
			} catch (IOException e1) {
			}

			podserver.dodajLog(
					"Greska pri otrpemanju videa " + start.getNazivVidea() + " od korisnika " + start.getUsername());
		} finally {
			podserver.obrisiVideoUOtpremanju(start.getNazivVidea(), start.getUsername());
			try {
				klijent.close();
			} catch (IOException e) {
			}
			try {
				if (cs != null)
					cs.close();
			} catch (IOException e) {
			}
		}
	}

	private synchronized boolean postojiFajl(String dir, String naziv) throws IOException {
		Files.createDirectories(Paths.get(dir));
		File outFile = new File(dir + "/" + naziv);
		return !outFile.createNewFile();
	}
}
