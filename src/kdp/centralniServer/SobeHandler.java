package kdp.centralniServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import kdp.Konekcija;
import kdp.SobaPoruka;
import kdp.Status;
import kdp.centralniServer.CentralniServer;;

public class SobeHandler extends Thread {
	private CentralniServer server;
	private Konekcija klijent;
	private SobaPoruka poruka;
	private List<Podserver> podserveri;
	private boolean provera = true;

	public SobeHandler(CentralniServer server, Konekcija konekcija, SobaPoruka poruka, List<Podserver> podserveri) {
		this.server = server;
		this.klijent = konekcija;
		this.poruka = poruka;
		this.podserveri = podserveri;
	}

	public SobeHandler(CentralniServer server, Konekcija konekcija, SobaPoruka poruka, List<Podserver> podserveri,
			boolean provera) {
		this.server = server;
		this.klijent = konekcija;
		this.poruka = poruka;
		this.podserveri = podserveri;
		this.provera = provera;
	}

	@Override
	public void run() {
		try {
			if (provera) {
				poruka.getSoba().setId(server.getSobeNextId());
				Konekcija konekcije[] = new Konekcija[podserveri.size()];

				for (int i = 0; i < podserveri.size(); i++) {
					try {
						konekcije[i] = null;
						konekcije[i] = new Konekcija(podserveri.get(i).getIP(), podserveri.get(i).getPort());
						konekcije[i].posaljiPoruku(poruka);
					} catch (IOException e) {
						e.printStackTrace();
						if (konekcije[i] != null) {
							try {
								konekcije[i].close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							konekcije[i] = null;
						}
					}
				}

				boolean greska = false;
				for (int i = 0; i < podserveri.size(); i++) {
					if (konekcije[i] == null) {
						greska = true;
						continue;
					}

					try {
						Status status = (Status) konekcije[i].primiPoruku();
						if (!"OK".equals(status.getStatus()))
							greska = true;
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
						greska = true;
					} finally {
						try {
							if (konekcije[i] != null)
								konekcije[i].close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				if (greska)
					poruka.getSoba().setId(0);
				else
					server.dodajSobu(poruka.getSoba());
			}

			klijent.posaljiPoruku(poruka);
		} catch (IOException e) {
		} finally {
			try {
				klijent.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
