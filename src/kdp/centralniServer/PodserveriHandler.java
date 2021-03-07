package kdp.centralniServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import kdp.Konekcija;
import kdp.Poruka;
import kdp.Poruka.Tip;
import kdp.ProveraPoruka;

public class PodserveriHandler extends Thread {
	public static int Y = 5;

	private CentralniServer cs;
	private Podserver p;

	public PodserveriHandler(CentralniServer cs, Podserver p) {
		this.cs = cs;
		this.p = p;
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {

			try {
				Thread.sleep(Y * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}

			if (!proveri() && !proveri() && !proveri()) {
				preraspodeliKorisnike();
				return;
			}
		}
	}

	private boolean proveri() {
		try (Konekcija podserver = new Konekcija(p.getIP(), p.getPort(), 2500)) {
			podserver.setTimeout(Y * 500);
			podserver.posaljiPoruku((new ProveraPoruka()));
			podserver.primiPoruku();
			
			return true;
		} catch (IOException | ClassNotFoundException e) {
		}
		
		return false;
	}

	private synchronized void preraspodeliKorisnike() {
		cs.ukloniPodserver(p);
		cs.dodajLog("Proglasavam podserver " + p.getId() + " nedostupnim");
		if (p.getKorisnici() != null && p.getKorisnici().size() > 0)
			cs.preraspodeliKorisnike(p);
	}
}
