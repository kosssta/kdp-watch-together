package kdp.podserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.SwingUtilities;

import kdp.LoginPoruka;
import kdp.Poruka.Tip;
import kdp.Status;

public class KorisniciHandler extends Thread {
	private Podserver podserver;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private LoginPoruka poruka;

	public KorisniciHandler(Podserver podserver, Socket socket, ObjectInputStream in, ObjectOutputStream out,
			LoginPoruka poruka) {
		this.podserver = podserver;
		this.socket = socket;
		this.in = in;
		this.out = out;
		this.poruka = poruka;
	}

	@Override
	public void run() {
		if (poruka.getTip() == Tip.REGISTRACIJA)
			registracija();
		else if (poruka.getTip() == Tip.PRIJAVA)
			login();

		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void registracija() {
		try {
			if (podserver.dodajKorisnika(poruka.getUsername(), poruka.getPassword())) {
				this.out.writeObject(new Status("Registracija uspesna"));
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						podserver.dodajLog("Registrovan korisnik: " + poruka.getUsername());
					}
				});
			} else {
				this.out.writeObject(new Status("Greska"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void login() {
		try {
			if (podserver.proveriKorisnika(poruka.getUsername(), poruka.getPassword())) {
				this.out.writeObject(new Status("OK"));
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						podserver.dodajLog("Ulogovan korisnik: " + poruka.getUsername());
					}
				});
			} else {
				this.out.writeObject(new Status("Korisnicko ime ne postoji"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
