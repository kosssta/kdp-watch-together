package kdp.centralniServer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import kdp.InitPoruka;
import kdp.LoginPoruka;
import kdp.Poruka;
import kdp.Status;
import kdp.Poruka.Tip;

import java.awt.ScrollPane;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class CentralniServer extends JFrame implements Runnable {
	public static final int MAX_THREADS = 10;
	public static final int PORT = 4567;

	private ServerSocket socket;
	private ExecutorService pool;
	private Thread thread;
	private List<Podserver> podserveri = new ArrayList<>();
	private HashMap<String, String> neraspodeljeniKorisnici;
	
	private JTextArea logovi;
	private Semaphore logoviMutex;

	public CentralniServer() throws IOException {
		super("Centralni server");
		setBounds(1000, 400, 400, 200);
		dodajKomponente();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		socket = new ServerSocket(PORT);
		pool = Executors.newFixedThreadPool(MAX_THREADS);
		thread = new Thread(this);
		setVisible(true);
	}

	private void dodajKomponente() {
		logovi = new JTextArea("");
		logovi.setEditable(false);
		logoviMutex = new Semaphore(1);
		this.add(new JScrollPane(logovi));
	}

	public void start() {
		thread.start();
	}

	public void run() {
		while (!Thread.interrupted()) {
			try {
				Socket client = socket.accept();

				ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(client.getInputStream());

				try {
					Poruka poruka = (Poruka) in.readObject();
					if (poruka.getTip() == Tip.REGISTRACIJA || poruka.getTip() == Tip.PRIJAVA)
						pool.execute(new KorisniciHandler(client, in, out, podserveri, (LoginPoruka) poruka));
					else if (poruka.getTip() == Tip.INIT) {
						try {
							InitPoruka init = (InitPoruka) poruka;
							dodajPodserver(init.getIP(), init.getPort());
							dodajLog("Novi podserver dodat: " + init.getIP() + " - " + init.getPort());
							out.writeObject(new Status("OK"));
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							in.close();
							out.close();
							client.close();
						}
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
		logoviMutex.acquireUninterruptibly();
		logovi.append(log + "\n");
		logoviMutex.release();
	}

	public synchronized void dodajPodserver(String IP, int port) {
		Podserver p = new Podserver(IP, port);
		podserveri.add(p);
		if (podserveri.size() == 1 && neraspodeljeniKorisnici != null) {
			for (String username : neraspodeljeniKorisnici.keySet()) {
				pool.execute(new KorisniciHandler(podserveri, new LoginPoruka(username, neraspodeljeniKorisnici.get(username), Tip.REGISTRACIJA)));
			}
			neraspodeljeniKorisnici = null;
		}
		new PodserveriHandler(this, p).start();
	}

	public synchronized void ukloniPodserver(Podserver p) {
		podserveri.remove(p);
	}

	public static void main(String[] args) {
		try {
			new CentralniServer().run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void preraspodeliKorisnike(Podserver p) {
		if (podserveri.size() == 0) {
			neraspodeljeniKorisnici = p.getKorisnici();
			return;
		}
		HashMap<String, String> korisnici = p.getKorisnici();
		int i = 0;
		for (String username : korisnici.keySet()) {
			Podserver ps = podserveri.get(i);
			pool.execute(new KorisniciHandler(podserveri, new LoginPoruka(username, korisnici.get(username), Tip.REGISTRACIJA)));
			dodajLog("Prebacujem korisnika " + username + " na podserver " + ps.getId());
			i = (i + 1) % podserveri.size();
		}
	}
}
