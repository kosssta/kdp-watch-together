package kdp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javafx.util.Duration;

public class Soba implements Serializable {
	private int id;
	private String video;
	private String vlasnikVidea;
	private String admin;
	private List<String> clanovi;
	private long vreme;
	private boolean playing;
	private long timestamp;
	transient private List<Konekcija> gledaoci;

	public Soba(String video, String vlasnikVidea, String admin) {
		this.video = video;
		this.vlasnikVidea = vlasnikVidea;
		this.admin = admin;
		clanovi = new ArrayList<String>();
		vreme = 0;
		timestamp = System.currentTimeMillis();
		gledaoci = new ArrayList<>();
	}

	@Override
	public boolean equals(Object soba) {
		return (soba instanceof Soba) && ((Soba) soba).id == this.id;
	}

	public void dodajClana(String clan) {
		clanovi.add(clan);
	}

	public void dodajClanove(List<String> clanovi) {
		if (clanovi.size() == 0)
			this.clanovi = clanovi;
		else
			for (String c : clanovi)
				this.clanovi.add(c);
	}

	public String getVideo() {
		return video;
	}

	public String getVlasnikVidea() {
		return vlasnikVidea;
	}

	public String getAdmin() {
		return admin;
	}

	public List<String> getClanovi() {
		return clanovi;
	}

	public int getId() {
		return id;
	}

	public boolean isClan(String korisnik) {
		return isAdmin(korisnik) || clanovi.contains(korisnik);
	}

	public boolean isAdmin(String korisnik) {
		return admin.equals(korisnik);
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getVreme() {
		return vreme;
	}

	public void setVreme(long vreme) {
		this.vreme = vreme;
		timestamp = System.currentTimeMillis();
	}

	public boolean isPlaying() {
		return playing;
	}

	public synchronized void setPlaying(boolean playing) {
		this.playing = playing;

		if (gledaoci == null)
			return;

		for (int i = 0; i < gledaoci.size(); i++)
			try {
				gledaoci.get(i).posaljiPoruku(new VideoStatusPoruka(vreme, playing));
			} catch (IOException e) {
				Konekcija kon = gledaoci.get(i);
				try {
					kon.close();
				} catch (IOException e1) {
				}
				gledaoci.remove(i);
				i--;
			}
	}

	public long getTimestamp() {
		return timestamp;
	}

	public synchronized void dodajGledaoca(Konekcija kon) {
		if (gledaoci == null)
			gledaoci = new ArrayList<>();

		gledaoci.add(kon);
		if (playing) {
			long prev = timestamp;
			timestamp = System.currentTimeMillis();
			vreme += timestamp - prev;
		}
		try {
			kon.posaljiPoruku(new VideoStatusPoruka(vreme, playing));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void obrisiGledaoca(Konekcija kon) {
		gledaoci.remove(kon);
	}

	public synchronized void obrisiGledaoce() {
		if (gledaoci != null) {
			for (Konekcija kon : gledaoci) {
				try {
					kon.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		gledaoci = new ArrayList<>();
	}
}
