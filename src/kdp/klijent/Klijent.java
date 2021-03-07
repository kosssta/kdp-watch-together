package kdp.klijent;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import kdp.Konekcija;
import kdp.LoginPoruka;
import kdp.Obavestenje;
import kdp.Poruka;
import kdp.Status;
import kdp.Poruka.Tip;

public class Klijent extends JFrame implements ActionListener {
	private String centralniServerIP;
	private int centralniServerPort;

	private JButton registracija;
	private JButton login;
	private JLabel potvrdaLozinkeLabela;
	private JPasswordField potvrdaLozinke;
	private JTextField username;
	private JPasswordField password;
	private JLabel greska;
	private Thread nit;
	private Bioskop bioskop;

	public Klijent(String centralniServerIP, int centralniServerPort) {
		super("Klijent");
		this.centralniServerIP = centralniServerIP;
		this.centralniServerPort = centralniServerPort;
		this.setResizable(false);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds((int) (screenSize.width * 0.375), (int) (screenSize.height * 0.3),
				(int) (screenSize.width * 0.25), (int) (screenSize.height * 0.3));

		dodajKomponente();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	private void dodajKomponente() {
		this.setLayout(new BorderLayout());

		JLabel registracijaLabel = new JLabel("Registracija");
		registracijaLabel.setHorizontalAlignment(JLabel.CENTER);
		registracijaLabel.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 22));

		JLabel prijavaLabel = new JLabel("Prijava");
		prijavaLabel.setHorizontalAlignment(JLabel.CENTER);
		prijavaLabel.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 22));
		prijavaLabel.setVisible(false);

		JRadioButton registracijaMeni = new JRadioButton("Registracija");
		JRadioButton prijavaMeni = new JRadioButton("Prijava");
		registracijaMeni.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 14));
		prijavaMeni.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 14));
		registracijaMeni.setHorizontalAlignment(JRadioButton.CENTER);
		prijavaMeni.setHorizontalAlignment(JRadioButton.CENTER);
		registracijaMeni.setSelected(true);
		registracijaMeni.addActionListener(e -> {
			greska.setText("");
			username.setText("");
			password.setText("");
			potvrdaLozinke.setText("");
			potvrdaLozinkeLabela.setVisible(true);
			potvrdaLozinke.setVisible(true);
			login.setVisible(false);
			registracija.setVisible(true);
			prijavaMeni.setSelected(false);
			registracijaMeni.setSelected(true);
			prijavaLabel.setVisible(false);
			registracijaLabel.setVisible(true);
		});
		prijavaMeni.addActionListener(e -> {
			greska.setText("");
			username.setText("");
			password.setText("");
			potvrdaLozinke.setText("");
			potvrdaLozinkeLabela.setVisible(false);
			potvrdaLozinke.setVisible(false);
			registracija.setVisible(false);
			login.setVisible(true);
			prijavaMeni.setSelected(true);
			registracijaMeni.setSelected(false);
			registracijaLabel.setVisible(false);
			prijavaLabel.setVisible(true);
		});
		JPanel sever = new JPanel(new CardLayout());
		sever.add(registracijaLabel);
		sever.add(prijavaLabel);
		this.add(sever, BorderLayout.NORTH);

		username = new JTextField();
		password = new JPasswordField();
		potvrdaLozinke = new JPasswordField();
		JLabel usernameLabela = new JLabel("Korisnicko ime:");
		JLabel passwordLabela = new JLabel("Lozinka:");
		potvrdaLozinkeLabela = new JLabel("Potvrda lozinke:");
		usernameLabela.setHorizontalAlignment(JLabel.CENTER);
		passwordLabela.setHorizontalAlignment(JLabel.CENTER);
		potvrdaLozinkeLabela.setHorizontalAlignment(JLabel.CENTER);
		usernameLabela.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 14));
		passwordLabela.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 14));
		potvrdaLozinkeLabela.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 14));

		JPanel centar = new JPanel(new GridLayout(8, 2));
		for (int i = 0; i < 2; i++)
			centar.add(new JLabel(" "));
		centar.add(usernameLabela);
		centar.add(username);
		for (int i = 0; i < 2; i++)
			centar.add(new JLabel(" "));
		centar.add(passwordLabela);
		centar.add(password);
		for (int i = 0; i < 2; i++)
			centar.add(new JLabel(" "));
		centar.add(potvrdaLozinkeLabela);
		centar.add(potvrdaLozinke);
		for (int i = 0; i < 2; i++)
			centar.add(new JLabel(" "));
		centar.add(registracijaMeni);
		centar.add(prijavaMeni);
		this.add(centar, BorderLayout.CENTER);

		registracija = new JButton("Registruj se");
		login = new JButton("Prijavi se");
		login.setVisible(false);
		registracija.addActionListener(this);
		login.addActionListener(this);

		JPanel jug = new JPanel(new GridLayout(2, 1));
		greska = new JLabel("");
		greska.setFont(new Font("Arial", Font.BOLD, 14));
		greska.setForeground(Color.RED);
		greska.setHorizontalAlignment(JLabel.CENTER);
		jug.add(greska);
		JPanel buttons = new JPanel(new CardLayout());
		buttons.add(registracija);
		buttons.add(login);
		jug.add(buttons);
		this.add(jug, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		greska.setText("");
		if (e.getSource() == registracija)
			registracija();
		else if (e.getSource() == login)
			prijava(false);
	}

	private void registracija() {
		String usernameString = username.getText();
		String passwordString = new String(password.getPassword());
		String potvrdaString = new String(potvrdaLozinke.getPassword());

		if ("".equals(usernameString) || "".equals(passwordString) || "".equals(potvrdaString)) {
			greska.setText("Sva polja moraju biti popunjena");
			return;
		}

		if (!passwordString.equals(potvrdaString)) {
			greska.setText("Potvrda lozinke se razlikuje od lozinke");
			return;
		}

		try (Konekcija konekcija = new Konekcija(centralniServerIP, centralniServerPort)) {
			konekcija.posaljiPoruku(new LoginPoruka(usernameString, passwordString, Poruka.Tip.REGISTRACIJA));
			Status status = (Status) konekcija.primiPoruku();
			greska.setText(status.getStatus());
			if ("Registracija uspesna".equals(status.getStatus())) {
				username.setText("");
				password.setText("");
				potvrdaLozinke.setText("");
			}
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Server trenutno nije dostupan");
		}
	}

	private boolean prijava(boolean repeat) {
		String usernameString = username.getText();
		String passwordString = new String(password.getPassword());

		if ("".equals(usernameString) || "".equals(passwordString)) {
			greska.setText("Sva polja moraju biti popunjena");
			return false;
		}

		String[] odgovor = null;
		try (Konekcija kon = new Konekcija(centralniServerIP, centralniServerPort)) {
			kon.posaljiPoruku(new LoginPoruka(usernameString, passwordString, Poruka.Tip.PRIJAVA));
			Status status = (Status) kon.primiPoruku();
			if ("Greska".equals(status.getStatus())) {
				greska.setText(status.getStatus());
				return false;
			} else {
				odgovor = status.getStatus().split("#");
				if (odgovor.length < 2) {
					greska.setText(status.getStatus());
					return false;
				} else if (repeat) {
					if (bioskop != null) {
						bioskop.setIP(odgovor[0]);
						bioskop.setPort(Integer.parseInt(odgovor[1]));
						System.out.println("Set ip: " + odgovor[0] + " " + odgovor[1]);
					}
				} else {
					this.dispose();
					bioskop = new Bioskop(odgovor[0], Integer.parseInt(odgovor[1]), this, usernameString, null);
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			return false;
		}

		final String odg[] = odgovor;
		if (bioskop != null && odgovor != null && odgovor.length >= 2) {
			nit = new Thread(() -> {
				try (Konekcija kon = new Konekcija(odg[0], Integer.parseInt(odg[1]))) {
					kon.posaljiPoruku(new LoginPoruka(usernameString, passwordString, Tip.PRIJAVA, true));

					kon.setTimeout(1000);
					String obavestenje = null;
					while (!Thread.interrupted()) {
						try {
							if (obavestenje != null) {
								kon.posaljiPoruku(new Status("OK"));
								obavestenje = null;
							}
							obavestenje = ((Obavestenje) kon.primiPoruku()).getObavestenja();
							bioskop.dodajObavestenje(obavestenje);
						} catch (SocketTimeoutException e) {
						}
					}

				} catch (NumberFormatException | IOException | ClassNotFoundException e) {
					if (bioskop != null) {
						bioskop.dodajObavestenje("Izgubljena je veza sa serverom.");
						bioskop.dodajObavestenje("Pokusavam da se rekonektujem za 13 sekundi...");
					}
					System.out.println("Rekonekcija");
					try {
						Thread.sleep(13000);
					} catch (InterruptedException e1) {
					}
					if (prijava(true)) {
						bioskop.dodajObavestenje("Rekonekcija uspesna.");
						System.out.println("Rekonekcija uspesna");
					}
					else
						new ServerNedostupan(this);
				}
			});
			nit.setDaemon(true);
			bioskop.setNit(nit);
			nit.start();
		}

		return true;
	}

	public void odjava() {
		if (nit != null)
			nit.interrupt();
	}

	public void finish() {
		if (bioskop != null)
			bioskop.finish();
		this.dispose();
	}

	public String getCentralniServerIP() {
		return centralniServerIP;
	}

	public int getCentralniServerPort() {
		return centralniServerPort;
	}
	

	public void setBioskop(Bioskop bioskop) {
		this.bioskop = bioskop;
	}

	public static void main(String[] args) {
		new Klijent(args[0], Integer.parseInt(args[1]));
	}

}

class ServerNedostupan extends JDialog {
	private Klijent klijent;

	public ServerNedostupan(Klijent klijent) {
		super(klijent, "Server nedostupan", true);
		this.klijent = klijent;
		this.setAlwaysOnTop(true);
		this.setResizable(false);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds((int) (screenSize.width * 0.25), (int) (screenSize.height * 0.375),
				(int) (screenSize.width * 0.5), (int) (screenSize.height * 0.25));
		dodajKomponente();
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				klijent.finish();
			}
		});
		this.setVisible(true);
	}

	private void dodajKomponente() {
		this.setLayout(new GridLayout(2, 1));
		this.add(new JLabel("Server trenutno nije dostupan."));
		JButton dugme = new JButton("OK");
		JPanel panel = new JPanel(new GridLayout(3, 3));
		for (int i = 0; i < 4; i++)
			panel.add(new JLabel(" "));
		panel.add(dugme);
		for (int i = 0; i < 4; i++)
			panel.add(new JLabel(" "));
		this.add(panel);

		dugme.addActionListener(e -> {
			klijent.finish();
			System.exit(0);
		});
	}
}
