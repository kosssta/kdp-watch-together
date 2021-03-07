package kdp.centralniServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kdp.Korisnik;

public class Podserver {
	private static volatile int ID = 0;

	private int id = ++ID;
	private String IP;
	private int port;
	private HashMap<String, Korisnik> korisnici = new HashMap<>();

	public Podserver(String IP, int port) {
		this.IP = IP;
		this.port = port;
	}

	public String getIP() {
		return IP;
	}

	public int getPort() {
		return port;
	}

	public int getId() {
		return id;
	}

	public synchronized boolean dodajKorisnika(String username, String password) {
		if (korisnici.containsKey(username))
			return false;
		korisnici.put(username, new Korisnik(username, password));
		return true;
	}

	public synchronized boolean postojiKorisnik(String username) {
		return korisnici.containsKey(username);
	}

	public synchronized boolean proveriKorisnika(String username, String password) {
		return korisnici.containsKey(username) && korisnici.get(username).getPassword().equals(password);
	}

	public int getSize() {
		return korisnici.size();
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Podserver) && IP.equals(((Podserver) o).getIP()) && port == ((Podserver) o).getPort();
	}

	public List<Korisnik> getKorisnici() {
		if (korisnici.size() == 0)
			return null;
		List<Korisnik> svi = new ArrayList<>();
		for (String username : korisnici.keySet())
			svi.add(korisnici.get(username));
		return svi;
	}
}
