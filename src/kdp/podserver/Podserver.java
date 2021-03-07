package kdp.podserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import kdp.InitPoruka;
import kdp.LoginPoruka;
import kdp.Poruka;
import kdp.ProveraPoruka;
import kdp.Status;
import kdp.Poruka.Tip;

public class Podserver extends JFrame implements Runnable {
	public static final int MAX_THREADS = 100;
	public static final String CENTRALNI_SERVER_HOST = "192.168.8.105";
	public static final int CENTRALNI_SERVER_PORT = 4567;

	private static int ID = 0;
	private int id;
	private HashMap<String, String> korisnici = new HashMap<String, String>();
	private ServerSocket socket;
	private ExecutorService pool;
	private Thread thread;

	private JTextArea logovi;
	private Semaphore logoviMutex;
	
	public Podserver() throws IOException, ClassNotFoundException {
		super("Podserver " + ++ID);
		this.id = ID;
		this.setBounds(500, 500, 500, 500);
		dodajKomponente();
		socket = new ServerSocket(CENTRALNI_SERVER_PORT + this.id);
		
		Socket centralniServer = new Socket(CENTRALNI_SERVER_HOST, CENTRALNI_SERVER_PORT);
		ObjectOutputStream out = new ObjectOutputStream(centralniServer.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(centralniServer.getInputStream());
		out.writeObject(new InitPoruka(InetAddress.getLocalHost().getHostAddress(), CENTRALNI_SERVER_PORT + this.id));
		Status status = (Status) in.readObject();
		
		in.close();
		out.close();
		centralniServer.close();
		
		if (!"OK".equals(status.getStatus())) throw new IOException();
		else dodajLog("Povezan sa centralnim serverom: " + centralniServer.getInetAddress().getHostAddress() + " - " + centralniServer.getPort());
		
		pool = Executors.newFixedThreadPool(MAX_THREADS);
		thread = new Thread(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
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
						pool.execute(new KorisniciHandler(this, client, in, out, (LoginPoruka)poruka));
					else if(poruka.getTip() == Tip.PROVERA) {
						out.writeObject(new ProveraPoruka());
						
						in.close();
						out.close();
						client.close();
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
	
	public String getHost() {
		return socket.getInetAddress().getHostAddress();
	}
	
	public int getPort() {
		return socket.getLocalPort();
	}

	public synchronized boolean proveriKorisnika(String username, String password) {
		return korisnici.containsKey(username) && korisnici.get(username).equals(password);
	}

	public synchronized boolean dodajKorisnika(String username, String password) {
		if (korisnici.containsKey(username)) return false;
		korisnici.put(username, password);
		return true;
	}

	public static void main(String[] args) {
		try {
			new Podserver().start();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
