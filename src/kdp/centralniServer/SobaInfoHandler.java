package kdp.centralniServer;

import java.io.IOException;
import java.util.List;

import kdp.Konekcija;
import kdp.Poruka;
import kdp.Soba;
import kdp.SobaInfoPoruka;

public class SobaInfoHandler extends Thread {
	private CentralniServer cs;
	private Konekcija konekcija;
	private SobaInfoPoruka poruka;

	public SobaInfoHandler(CentralniServer cs, Konekcija konekcija, SobaInfoPoruka poruka) {
		this.cs = cs;
		this.konekcija = konekcija;
		this.poruka = poruka;
	}

	@Override
	public void run() {
		List<Soba> sobe = cs.getSobe();
		Soba soba = null;

		for (Soba s : sobe)
			if (s.getId() == poruka.getId()) {
				soba = s;
				break;
			}

		if (soba == null) {
			try {
				konekcija.close();
			} catch (IOException e) {
			}
			return;
		}

		try {
			Poruka p = konekcija.primiPoruku();
			while (p instanceof SobaInfoPoruka) {
				poruka = (SobaInfoPoruka) p;
				soba.setVreme(poruka.getVreme());
				soba.setPlaying(poruka.isPlaying());
				p = konekcija.primiPoruku();
			}
		} catch (ClassNotFoundException | IOException e) {
		} finally {
			try {
				konekcija.close();
			} catch (IOException e) {
			}
		}
	}
}
