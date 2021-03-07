package kdp.centralniServer;

import java.util.HashMap;

public class Podserver {
	private static int ID = 0;
	
	private int id = ++ID;
	private String IP;
	private int port;
	private HashMap<String, String> korisnici = new HashMap<String, String>();

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
		if (getKorisnici().containsKey(username)) return false;
		getKorisnici().put(username, password);
		return true;
	}
	
	public synchronized boolean postojiKorisnik(String username) {
		return getKorisnici().containsKey(username);
	}
	
	public synchronized boolean proveriKorisnika(String username, String password) {
		return getKorisnici().containsKey(username) && getKorisnici().get(username) == password;
	}
	
	public int getSize() {
		return getKorisnici().size();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof Podserver) && IP.equals(((Podserver)o).getIP()) && port == ((Podserver)o).getPort();
	}

	public HashMap<String, String> getKorisnici() {
		return korisnici;
	}

	public void setKorisnici(HashMap<String, String> korisnici) {
		this.korisnici = korisnici;
	}
}
