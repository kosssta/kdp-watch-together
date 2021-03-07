package kdp.podserver;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import kdp.InitPoruka;
import kdp.Konekcija;
import kdp.KorisniciGet;
import kdp.Korisnik;
import kdp.LoginPoruka;
import kdp.Poruka;
import kdp.ProveraPoruka;
import kdp.Soba;
import kdp.SobaPoruka;
import kdp.Status;
import kdp.VideoGetPoruka;
import kdp.VideoNaziviPoruka;
import kdp.VideoProveraPoruka;
import kdp.VideoStatusPoruka;
import kdp.Poruka.Tip;

public class Podserver extends JFrame implements Runnable {
	public static final int MAX_THREADS = 100;
	private String centralniServerIP;
	private int centralniServerPort;

	private boolean gui;
	private int id;
	private HashMap<String, Korisnik> korisnici = new HashMap<>();
	private List<Soba> sobe = new ArrayList<Soba>();
	private ServerSocket socket;
	private ExecutorService pool;
	private Thread thread;
	private List<String> videoUOtpremanju = new ArrayList<>();

	private JTextArea logovi;

	public Podserver(String centralniServerIP, int centralniServerPort, String gui, int port)
			throws IOException, ClassNotFoundException {
		super("Podserver");
		this.centralniServerIP = centralniServerIP;
		this.centralniServerPort = centralniServerPort;
		this.gui = !"NO-GUI".equals(gui);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds((int) (screenSize.width * 0.375), (int) (screenSize.height * 0.25),
				(int) (screenSize.width * 0.25), (int) (screenSize.height * 0.5));

		dodajKomponente();
		socket = new ServerSocket(port);

		Konekcija centralniServer = new Konekcija(centralniServerIP, centralniServerPort);
		centralniServer.posaljiPoruku(new InitPoruka(InetAddress.getLocalHost().getHostAddress(), port));
		Status status = (Status) centralniServer.primiPoruku();

		if (!"OK".equals(status.getStatus())) {
			centralniServer.close();
			throw new IOException();
		} else {
			status = (Status) centralniServer.primiPoruku();
			this.id = Integer.parseInt(status.getStatus());
			this.setTitle("Podserver " + id);
			dodajLog("Povezan sa centralnim serverom: " + centralniServer.getIP() + " - " + centralniServer.getPort());
		}

		centralniServer.close();
		obrisiFolder(new File("./videoPodserver" + id));

		pool = Executors.newFixedThreadPool(MAX_THREADS);
		thread = new Thread(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		if (this.gui)
			setVisible(true);
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

	public int getId() {
		return id;
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
						pool.execute(new LoginHandler(this, konekcija, (LoginPoruka) poruka));
					else if (poruka.getTip() == Tip.PROVERA) {
						konekcija.posaljiPoruku(new ProveraPoruka());
						client.close();
					} else if (poruka.getTip() == Tip.VIDEO_PROVERA) {
						pool.execute(new OtpremanjeHandler(this, konekcija, (VideoProveraPoruka) poruka));
					} else if (poruka.getTip() == Tip.VIDEO_NAZIVI) {
						pool.execute(
								new VideoNaziviHandler(this, konekcija, ((VideoNaziviPoruka) poruka).getKorisnik()));
					} else if (poruka.getTip() == Tip.VIDEO_GET) {
						pool.execute(new VideoGetHandler(this, konekcija, (VideoGetPoruka) poruka));
					} else if (poruka.getTip() == Tip.KORISNICI_GET) {
						pool.execute(new SviKorisniciHandler(this, konekcija));
					} else if (poruka.getTip() == Tip.SOBA)
						pool.execute(new SobaHandler(this, konekcija, (SobaPoruka) poruka));
					else if (poruka.getTip() == Tip.VIDEO_STATUS) {
						pool.execute(new GledaociHandler(this, konekcija, (VideoStatusPoruka) poruka));
					} else {
						new Thread(() -> {
							try {
								konekcija.primiPoruku();
							} catch (ClassNotFoundException | IOException e) {
							} finally {
								try {
									konekcija.close();
								} catch (IOException e) {
								}
							}
						}).start();
					}
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
				System.out.println("Podserver " + id + ": " + log);
		}
	}

	public String getHost() {
		return socket.getInetAddress().getHostAddress();
	}

	public int getPort() {
		return socket.getLocalPort();
	}

	public synchronized boolean postojiKorisnik(String username) {
		return korisnici.containsKey(username);
	}

	public synchronized boolean proveriKorisnika(String username, String password) {
		return korisnici.containsKey(username) && korisnici.get(username).proveri(password);
	}

	public synchronized boolean dodajKorisnika(String username, String password) {
		if (korisnici.containsKey(username))
			return false;
		korisnici.put(username, new Korisnik(username, password));
		return true;
	}
	
	public synchronized boolean dodajKorisnika(Korisnik korisnik) {
		if (korisnici.containsKey(korisnik.getUsername()))
			return false;
		korisnici.put(korisnik.getUsername(), korisnik);
		return true;
	}

	public synchronized String getObavestenja(String username) {
		return korisnici.containsKey(username) ? korisnici.get(username).getObavestenja() : null;
	}

	public synchronized void dodajObavestenje(String username, String obavestenje) {
		if (korisnici.containsKey(username))
			korisnici.get(username).dodajObavestenje(obavestenje);
	}

	public synchronized void ulogujKorisnika(String username, Konekcija konekcija) {
		if (korisnici.containsKey(username))
			korisnici.get(username).setKonekcija(konekcija);
	}

	public synchronized Konekcija getKonekcija(String username) {
		return korisnici.containsKey(username) ? korisnici.get(username).getKonekcija() : null;
	}

	public synchronized void postaviVreme(String username, String video, String vlasnik, double vreme) {
		korisnici.get(username).dodajStartVreme(video, vlasnik, vreme);
	}

	public synchronized double getVreme(String username, String video, String vlasnik) {
		return korisnici.get(username).getStartVreme(video, vlasnik);
	}

	public synchronized List<String> getKorisnici() {
		List<String> sviKorisnici = new ArrayList<String>();

		for (String k : korisnici.keySet())
			sviKorisnici.add(k);

		return sviKorisnici;
	}

	public void dodajSobu(Soba soba) {
		synchronized (sobe) {
			if (!sobe.contains(soba))
				sobe.add(soba);
			else {
				for (Soba s : sobe)
					if (s.getId() == soba.getId()) {
						s.setVreme(soba.getVreme());
						s.setPlaying(soba.isPlaying());
						break;
					}
			}
		}
	}

	public Soba getSoba(Soba soba) {
		synchronized (sobe) {
			return sobe.get(sobe.indexOf(soba));
		}
	}

	public Soba getSoba(int idSobe) {
		synchronized (sobe) {
			for (Soba s : sobe)
				if (s.getId() == idSobe)
					return s;
		}
		return null;
	}

	public List<Soba> getSobe(String korisnik) {
		List<Soba> sobe = new ArrayList<Soba>();
		synchronized (this.sobe) {
			for (Soba s : this.sobe)
				if (s.isClan(korisnik))
					sobe.add(s);
		}
		return sobe;
	}

	public void dodajVideoUOtpremanju(String naziv, String vlasnik) {
		synchronized (videoUOtpremanju) {
			videoUOtpremanju.add(vlasnik + "/" + naziv);
		}
	}

	public void obrisiVideoUOtpremanju(String naziv, String vlasnik) {
		synchronized (videoUOtpremanju) {
			videoUOtpremanju.remove(vlasnik + "/" + naziv);
		}
	}

	public boolean isVideoUOtpremanju(String naziv, String vlasnik) {
		synchronized (videoUOtpremanju) {
			return videoUOtpremanju.contains(vlasnik + "/" + naziv);
		}
	}

	public String getCentralniServerIP() {
		return centralniServerIP;
	}

	public int getCentralniServerPort() {
		return centralniServerPort;
	}

	public static void main(String[] args) {
		try {
			new Podserver(args[0], Integer.parseInt(args[1]), args.length >= 4 ? args[3] : null,
					Integer.parseInt(args[2])).start();
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Centralni server nije dostupan");
		}
	}
}
