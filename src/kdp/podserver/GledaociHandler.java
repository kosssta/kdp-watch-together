package kdp.podserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import kdp.Konekcija;
import kdp.Soba;
import kdp.VideoStatusPoruka;

public class GledaociHandler extends Thread {
	private Podserver podserver;
	private Konekcija klijent;
	private VideoStatusPoruka poruka;

	public GledaociHandler(Podserver podserver, Konekcija konekcija, VideoStatusPoruka poruka) {
		this.podserver = podserver;
		this.klijent = konekcija;
		this.poruka = poruka;
	}

	@Override
	public void run() {
		int idSobe = poruka.getIdSobe();
		Soba soba = podserver.getSoba(idSobe);

		if (soba == null)
			return;

		soba.dodajGledaoca(klijent);
	}
}
