package kdp.klijent;

import java.awt.BorderLayout;
import java.util.concurrent.Semaphore;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Bioskop extends JFrame {
	private String IPpodservera;
	private int port;
	
	private JButton gledaj;
	
	private JTextArea obavestenja;
	private Semaphore obavestenjaMutex;
	
	public Bioskop(String IP, int port, String username) {
		super(username);
		this.setBounds(1000, 1000, 500, 500);
		this.IPpodservera = IP;
		this.port = port;
		dodajKomponente();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private void dodajKomponente() {
		gledaj = new JButton("GLEDAJ");
		this.add(gledaj, BorderLayout.WEST);
		
		obavestenja = new JTextArea("");
		obavestenjaMutex = new Semaphore(1);
		this.add(new JScrollPane(obavestenja), BorderLayout.EAST);
	}
	
	public void dodajObavestenje(String obavestenje) {
		obavestenjaMutex.acquireUninterruptibly();
		obavestenja.append(obavestenje + "\n");
		obavestenjaMutex.release();
	}
}
