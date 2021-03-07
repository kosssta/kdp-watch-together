package kdp.centralniServer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import kdp.InitPoruka;
import kdp.Konekcija;
import kdp.Korisnik;
import kdp.LoginPoruka;
import kdp.Poruka;
import kdp.VideoProveraPoruka;
import kdp.VideoStatusPoruka;
import kdp.Poruka.Tip;
import kdp.Soba;
import kdp.SobaInfoPoruka;
import kdp.SobaPoruka;
import kdp.VideoInfoPoruka;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class CentralniServer extends JFrame implements Runnable {
	public static final int MAX_THREADS = 10;
	public int port;

	private boolean gui;
	private ServerSocket socket;
	private ExecutorService pool;
	private Thread thread;
	private List<Podserver> podserveri = new ArrayList<>();
	private List<Korisnik> neraspodeljeniKorisnici;
	private List<Soba> sobe = new ArrayList<>();

	private JTextArea logovi;

	private volatile int sobeNextId = 1;

	public CentralniServer(int port, boolean gui) throws IOException {
		super("Centralni server");
		this.port = port;
		this.gui = gui;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds((int) (screenSize.width * 0.375), (int) (screenSize.height * 0.25),
				(int) (screenSize.width * 0.25), (int) (screenSize.height * 0.5));

		obrisiFolder(new File("./videoCentralniServer"));
		dodajKomponente();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		socket = new ServerSocket(port);
		pool = Executors.newFixedThreadPool(MAX_THREADS);
		thread = new Thread(this);
		if (gui)
			setVisible(true);
		dodajLog("Centralni server pokrenut na portu " + port);
	}

	private void dodajKomponente() {
		logovi = new JTextArea("");
		logovi.setEditable(false);
		this.add(new JScrollPane(logovi));
	}

	private void obrisiFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory())
					obrisiFolder(f);
				else
					f.delete();
			}
		}
		folder.delete();
	}

	public void start() {
		thread.start();
	}

	public void run() {
		while (!Thread.interrupted()) {
			try {
				Socket client = socket.accept();
				Konekcija konekcija = new Konekcija(client);

				try {
					Poruka poruka = (Poruka) konekcija.primiPoruku();
					if (poruka.getTip() == Tip.REGISTRACIJA || poruka.getTip() == Tip.PRIJAVA)
						pool.execute(new LoginHandler(konekcija, podserveri, (LoginPoruka) poruka));
					else if (poruka.getTip() == Tip.INIT) {
						pool.execute(new InitHandler(this, konekcija, (InitPoruka) poruka));
					} else if (poruka.getTip() == Tip.VIDEO_PROVERA)
						pool.execute(new VideoHandler(konekcija, (VideoProveraPoruka) poruka, podserveri));
					else if (poruka.getTip() == Tip.SOBA)
						pool.execute(new SobeHandler(this, konekcija, (SobaPoruka) poruka, podserveri));
					else if (poruka.getTip() == Tip.VIDEO_STATUS)
						pool.execute(new GledanjeHandler(this, konekcija, (VideoStatusPoruka) poruka, podserveri));
					else if (poruka.getTip() == Tip.KORISNICI_GET)
						pool.execute(new KorisniciGetHandler(konekcija, podserveri));
					else if (poruka.getTip() == Tip.SOBA_INFO)
						pool.execute(new SobaInfoHandler(this, konekcija, (SobaInfoPoruka) poruka));
					else if (poruka.getTip() == Tip.VIDEO_INFO)
						pool.execute(new VideoInfoHandler(this, konekcija, (VideoInfoPoruka) poruka));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void dodajLog(String log) {
		synchronized (logovi) {
			if (gui)
				logovi.append(log + "\n");
			else
				System.out.println("CS: " + log);
		}
	}

	public synchronized int dodajPodserver(String IP, int port) {
		Podserver p = null;
		boolean postoji = false;
		for (Podserver ps : podserveri)
			if (ps.getIP().equals(IP) && ps.getPort() == port) {
				p = ps;
				postoji = true;
				break;
			}

		if (p == null) {
			p = new Podserver(IP, port);
			podserveri.add(p);
			dodajLog("Novi podserver dodat: " + p.getIP() + " - " + p.getPort());
		} else
			dodajLog("Ponovo uspostavljena konekcija sa podserverom: " + p.getIP() + " - " + p.getPort());

		if (podserveri.size() == 1 && neraspodeljeniKorisnici != null) {
			for (Korisnik korisnik : neraspodeljeniKorisnici) {
				pool.execute(new LoginHandler(this, podserveri, new LoginPoruka(korisnik, Tip.REGISTRACIJA)));
			}
			neraspodeljeniKorisnici = null;
		}
		if (!postoji)
			new PodserveriHandler(this, p).start();
		return p.getId();
	}

	public synchronized void ukloniPodserver(Podserver p) {
		podserveri.remove(p);
	}
	
	public synchronized Podserver getPodserver(int id) {
		for (Podserver p : podserveri)
			if (p.getId() == id)
				return p;
		return null;
	}
	
	public synchronized void posaljiKorisnike(Podserver p) {
		if (p != null && p.getKorisnici() != null) {
			for (Korisnik k : p.getKorisnici())
				pool.execute(new LoginHandler(this, podserveri, new LoginPoruka(k, Tip.REGISTRACIJA), p));
		}
	}

	public synchronized void preraspodeliKorisnike(Podserver p) {
		if (podserveri.size() == 0) {
			neraspodeljeniKorisnici = p.getKorisnici();
			return;
		}

		List<Korisnik> korisnici = p.getKorisnici();
		int i = 0;
		for (Korisnik korisnik : korisnici) {
			Podserver ps = podserveri.get(i);
			pool.execute(new LoginHandler(this, podserveri, new LoginPoruka(korisnik, Tip.REGISTRACIJA)));
			dodajLog("Prebacujem korisnika " + korisnik.getUsername() + " na podserver " + ps.getId());

			i = (i + 1) % podserveri.size();
		}
	}

	public synchronized Korisnik getKorisnik(String username) {
		for (Podserver p : podserveri)
			for (Korisnik kor : p.getKorisnici())
				if (username.equals(kor.getUsername()))
					return kor;
		return null;
	}

	public void dodajSobu(Soba soba) {
		synchronized (sobe) {
			if (!sobe.contains(soba))
				sobe.add(soba);
			else {
				for (Soba s : sobe)
					if (s.getId() == soba.getId()) {
						s.obrisiGledaoce();
						s.setVreme(soba.getVreme());
						s.setPlaying(soba.isPlaying());
						break;
					}
			}
		}
	}

	public List<Soba> getSobe() {
		List<Soba> sve = new ArrayList<>();
		synchronized (sobe) {
			for (Soba s : sobe)
				sve.add(s);
		}
		return sve;
	}

	public List<Soba> getSobe(String username) {
		List<Soba> sve = new ArrayList<>();
		synchronized (sobe) {
			for (Soba s : sobe)
				if (s.isAdmin(username) || s.isClan(username))
					sve.add(s);
		}
		return sve;
	}

	public int getSobeNextId() {
		return sobeNextId++;
	}

	public static void main(String[] args) {
		try {
			new CentralniServer(Integer.parseInt(args[0]), args.length >= 1 || !"NO-GUI".equals(args[1])).run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
