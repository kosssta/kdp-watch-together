package kdp.klijent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import kdp.Konekcija;
import kdp.Progres;
import kdp.Status;
import kdp.VideoPoruka;
import kdp.VideoProveraPoruka;

public class OtpremanjeHandler extends Thread {
	private Bioskop bioskop;
	private String putanja;
	private String naziv;
	private String IP;
	private int port;
	private Progres otpremanje;

	public OtpremanjeHandler(Bioskop bioskop, String putanja, String naziv, String IP, int port, Progres otpremanje) {
		this.bioskop = bioskop;
		this.putanja = putanja;
		this.naziv = naziv;
		this.IP = IP;
		this.port = port;
		this.otpremanje = otpremanje;
	}

	@Override
	public void run() {
		BufferedInputStream in = null;

		try (Konekcija kon = new Konekcija(IP, port)) {
			kon.posaljiPoruku(new VideoProveraPoruka(naziv, bioskop.getUsername()));
			Status status = (Status) kon.primiPoruku();
			if (!"OK".equals(status.getStatus())) {
				bioskop.dodajObavestenje("Video " + naziv + " vec postoji na serveru");
				return;
			}

			File videoFile = new File(putanja);

			in = new BufferedInputStream(new FileInputStream(videoFile));

			while (true) {
				VideoPoruka poruka = new VideoPoruka();
				int num_read = in.read(poruka.getBuffer());
				if (num_read > 0) {
					poruka.setSize(num_read);
					kon.posaljiPoruku(poruka);
					otpremanje.postaviProgres(otpremanje.getProgres() + num_read);
					continue;
				}

				otpremanje.dispose();
				kon.posaljiPoruku(new Status("OK"));
				status = (Status) kon.primiPoruku();

				if (!"OK".equals(status.getStatus()))
					bioskop.dodajObavestenje("Doslo je do greske pri otpremanju videa " + naziv);
				else
					bioskop.dodajObavestenje("Video " + naziv + " je uspesno otpremljen");
				break;
			}

		} catch (IOException | ClassNotFoundException e1) {
			bioskop.dodajObavestenje("Doslo je do greske pri otpremanju videa " + naziv);
		} finally {
			if (otpremanje != null)
				otpremanje.dispose();
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
		}
	}
}
