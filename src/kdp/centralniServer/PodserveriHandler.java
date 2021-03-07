package kdp.centralniServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

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
		Socket socket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;

		try {
			socket = new Socket(p.getIP(), p.getPort());
			socket.setSoTimeout(Y * 500);

			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());

			out.writeObject(new ProveraPoruka());

			in.readObject();

			return true;
		} catch (SocketException e) {

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			try {
				Thread.sleep(Y * 500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	private synchronized void preraspodeliKorisnike() {
		cs.ukloniPodserver(p);
		cs.dodajLog("Proglasavam podserver " + p.getId() + " nedostupnim");
		if (p.getKorisnici().size() > 0)
			cs.preraspodeliKorisnike(p);
	}
}
