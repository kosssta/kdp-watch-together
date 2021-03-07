package kdp.centralniServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import kdp.LoginPoruka;
import kdp.Status;

public class KorisniciHandler extends Thread {
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private List<Podserver> podserveri;
	private LoginPoruka poruka;

	public KorisniciHandler(Socket socket, ObjectInputStream in, ObjectOutputStream out, List<Podserver> podserveri,
			LoginPoruka poruka) {
		this.socket = socket;
		this.in = in;
		this.out = out;
		this.podserveri = podserveri;
		this.poruka = poruka;
	}

	public KorisniciHandler(List<Podserver> podserveri, LoginPoruka poruka) {
		this.podserveri = podserveri;
		this.poruka = poruka;
	}

	public void run() {
		switch (poruka.getTip()) {
		case REGISTRACIJA:
			registracija();
			break;
		case PRIJAVA:
			login();
			break;
		}

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

	private void registracija() {
		Socket podserver = null;
		ObjectOutputStream pout = null;
		ObjectInputStream pin = null;

		try {
			if (podserveri.size() == 0) {
				if (this.out != null)
					this.out.writeObject(new Status("Greska"));
				return;
			}

			Podserver dodavanje = null;
			int min = Integer.MAX_VALUE;
			for (Podserver p : podserveri) {
				if (p.postojiKorisnik(poruka.getUsername())) {
					if (this.out != null)
						this.out.writeObject(new Status("Korisnicko ime vec postoji"));
					return;
				}
				int br = p.getSize();
				if (br < min) {
					min = br;
					dodavanje = p;
				}
			}

			podserver = new Socket(dodavanje.getIP(), dodavanje.getPort());
			pout = new ObjectOutputStream(podserver.getOutputStream());
			pin = new ObjectInputStream(podserver.getInputStream());

			pout.writeObject(poruka);
			dodavanje.dodajKorisnika(poruka.getUsername(), poruka.getPassword());
			Status status = (Status) pin.readObject();
			if (this.out != null)
				this.out.writeObject(status);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pin != null)
					pin.close();
				if (pout != null)
					pout.close();
				if (podserver != null)
					podserver.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void login() {
		Socket podserver = null;
		ObjectOutputStream pout = null;
		ObjectInputStream pin = null;

		for (Podserver p : podserveri) {
			if (p.postojiKorisnik(poruka.getUsername())) {
				try {
					podserver = new Socket(p.getIP(), p.getPort());
					pout = new ObjectOutputStream(podserver.getOutputStream());
					pin = new ObjectInputStream(podserver.getInputStream());

					pout.writeObject(poruka);
					Status status = (Status) pin.readObject();
					if (this.out != null && !"OK".equals(status.getStatus()))
						this.out.writeObject(status);
					else
						this.out.writeObject(
								new Status("" + p.getIP() + "#" + p.getPort()));
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
					try {
						if (this.out != null)
							this.out.writeObject(new Status("Greska"));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} finally {
					try {
						if (pin != null)
							pin.close();
						if (pout != null)
							pout.close();
						if (podserver != null)
							podserver.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				return;
			}
		}

		try {
			if (this.out != null)
				this.out.writeObject(new Status("Korisnicko ime ne postoji"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
