package kdp.centralniServer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import kdp.InitPoruka;
import kdp.Konekcija;
import kdp.Soba;
import kdp.SobaPoruka;
import kdp.Status;
import kdp.VideoPoruka;
import kdp.VideoProveraPoruka;

public class InitHandler extends Thread {
	CentralniServer cs;
	Konekcija klijent;
	int port;

	public InitHandler(CentralniServer cs, Konekcija konekcija, InitPoruka poruka) {
		this.cs = cs;
		this.klijent = konekcija;
		this.port = poruka.getPort();
	}

	@Override
	public void run() {
		try {
			int id = cs.dodajPodserver(klijent.getIP(), port);
			klijent.posaljiPoruku(new Status("OK"));
			klijent.posaljiPoruku(new Status(String.valueOf(id)));
			
			Podserver p = cs.getPodserver(id);
			cs.posaljiKorisnike(p);

			Files.createDirectories(Paths.get("./videoCentralniServer"));
			File dir = new File("./videoCentralniServer");

			File[] listUsers = dir.listFiles();
			if (listUsers != null) {
				for (File u : listUsers) {
					if (u.isDirectory()) {
						File[] listFiles = u.listFiles();
						for (File f : listFiles) {
							try (Konekcija konekcija = new Konekcija(klijent.getIP(), port)) {
								konekcija.posaljiPoruku(new VideoProveraPoruka(f.getName(), u.getName(), true));

								if ("OK".equals(((Status) konekcija.primiPoruku()).getStatus())) {
									try (BufferedInputStream fin = new BufferedInputStream(new FileInputStream(f))) {
										VideoPoruka v = new VideoPoruka();

										int num_read = fin.read(v.getBuffer(), 0, v.BUFFER_CAPACITY);
										while (num_read > 0) {
											v.setSize(num_read);
											konekcija.posaljiPoruku(v);
											v = new VideoPoruka();
											num_read = fin.read(v.getBuffer(), 0, v.BUFFER_CAPACITY);
										}
										konekcija.posaljiPoruku(new Status("OK"));
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			
			/*for (Soba s : cs.getSobe()) {
				try (Konekcija konekcija = new Konekcija(klijent.getIP(), port)) {
					konekcija.posaljiPoruku(new SobaPoruka(s, false));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}*/
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
