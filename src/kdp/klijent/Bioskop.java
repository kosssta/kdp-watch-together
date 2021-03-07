package kdp.klijent;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import kdp.VideoNaziviPoruka;
import kdp.Konekcija;
import kdp.Progres;
import kdp.Soba;

public class Bioskop extends JFrame implements ActionListener {
	private Klijent klijent;
	private String IPpodservera;
	private int port;
	private String username;

	private JPanel pocetna;
	private JPanel otpremanje;
	private JPanel gledanje;

	private JButton gledaj;
	private JButton otpremi;
	private String nazivFilma;
	private JButton odjava;

	private JTextArea obavestenja;
	private Thread nit;

	public Bioskop(String IP, int port, Klijent klijent, String username, String obavestenja) {
		super(username);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds((int) (screenSize.width * 0.25), (int) (screenSize.height * 0.25),
				(int) (screenSize.width * 0.5), (int) (screenSize.height * 0.5));
		this.setMinimumSize(new Dimension((int) (screenSize.width * 0.5), (int) (screenSize.height * 0.5)));

		this.IPpodservera = IP;
		this.port = port;
		this.klijent = klijent;
		this.username = username;
		dodajKomponente(obavestenja);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		File folder = new File("./tmp/" + username);
		obrisiFolder(folder);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				File folder = new File("./tmp/" + username);
				obrisiFolder(folder);
				if (nit != null)
					nit.interrupt();
			}
		});
		setVisible(true);
	}

	private void dodajKomponente(String o) {
		this.setLayout(new BorderLayout());
		JPanel centar = new JPanel(new CardLayout());
		gledanje = new JPanel(new GridLayout(2, 1));
		otpremanje = new JPanel(new GridLayout(3, 1));
		pocetna = new JPanel(new BorderLayout());
		gledanje.setVisible(false);
		otpremanje.setVisible(false);

		// obavestenja
		JPanel east = new JPanel(new BorderLayout());
		JLabel obavestenjaLabela = new JLabel("Obavestenja");
		obavestenjaLabela.setHorizontalAlignment(JLabel.CENTER);
		obavestenjaLabela.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 24));
		obavestenja = new JTextArea(20, 30);
		obavestenja.setEditable(false);
		if (o != null && o.length() > 0)
			obavestenja.append(o);
		east.add(obavestenjaLabela, BorderLayout.NORTH);
		east.add(new JScrollPane(obavestenja), BorderLayout.CENTER);
		this.add(east, BorderLayout.EAST);

		// pocetna
		ImageIcon gledajIcon = new ImageIcon("./gledaj-icon.png");
		JLabel gledajLabel = new JLabel("Gledaj");
		gledajLabel.setHorizontalAlignment(JLabel.CENTER);
		gledajLabel.setFont(new Font("Arial", Font.BOLD, 14));
		gledaj = new JButton();
		gledaj.setLayout(new BorderLayout());
		gledaj.add(new JLabel(getScaledImage(gledajIcon, 120, 120)), BorderLayout.CENTER);
		gledaj.add(gledajLabel, BorderLayout.SOUTH);

		ImageIcon otpremiIcon = new ImageIcon("./otpremi-icon.png");
		JLabel otpremiLabel = new JLabel("Otpremi");
		otpremiLabel.setHorizontalAlignment(JLabel.CENTER);
		otpremiLabel.setFont(new Font("Arial", Font.BOLD, 14));
		otpremi = new JButton();
		otpremi.setLayout(new BorderLayout());
		otpremi.add(new JLabel(getScaledImage(otpremiIcon, 120, 120)), BorderLayout.CENTER);
		otpremi.add(otpremiLabel, BorderLayout.SOUTH);
		otpremi.addActionListener(e -> {
			pocetna.setVisible(false);
			otpremanje.setVisible(true);
		});

		ImageIcon odjavaIcon = new ImageIcon("./odjava-icon.png");
		JLabel odjavaLabel = new JLabel("Odjava");
		odjavaLabel.setHorizontalAlignment(JLabel.CENTER);
		odjavaLabel.setFont(new Font("Arial", Font.BOLD, 14));
		odjava = new JButton();
		odjava.setLayout(new BorderLayout());
		odjava.add(new JLabel(getScaledImage(odjavaIcon, 120, 120)));
		odjava.add(odjavaLabel, BorderLayout.SOUTH);

		odjava.addActionListener(e -> {
			this.dispose();
			klijent.odjava();
			new Klijent(klijent.getCentralniServerIP(), klijent.getCentralniServerPort());
		});

		JPanel pocetnaCentar = new JPanel(new GridLayout(3, 1));
		pocetnaCentar.add(gledaj);
		pocetnaCentar.add(otpremi);
		pocetnaCentar.add(odjava);
		pocetna.add(pocetnaCentar, BorderLayout.CENTER);

		centar.add(pocetna);

		// otpremanje
		JLabel nazivLabela = new JLabel(" Izaberite fajl za otpremanje:");
		nazivLabela.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 18));
		otpremanje.add(new JLabel(" "));

		JTextField putanja = new JTextField("");
		JPanel drugiRed = new JPanel(new GridLayout(3, 1));
		drugiRed.add(nazivLabela);
		JPanel drugiRedKomande = new JPanel(new BorderLayout());
		drugiRed.add(drugiRedKomande);
		drugiRed.add(new JLabel(" "));

		JButton browse = new JButton("Browse");
		drugiRedKomande.add(browse, BorderLayout.EAST);
		drugiRedKomande.add(putanja, BorderLayout.CENTER);

		JButton otpr = new JButton("Otpremi");
		JButton nazad = new JButton("Nazad");
		otpr.setEnabled(false);
		nazad.addActionListener(e -> {
			otpremanje.setVisible(false);
			pocetna.setVisible(true);
		});
		browse.addActionListener(e -> {
			JFileChooser jfc = new JFileChooser();
			int ret = jfc.showOpenDialog(this);
			if (ret == JFileChooser.APPROVE_OPTION) {
				putanja.setText(jfc.getSelectedFile().getAbsolutePath());
				nazivFilma = jfc.getSelectedFile().getName();
			}
		});
		otpr.addActionListener(e -> {
			Progres otpremanje = new Progres(this, nazivFilma, putanja.getText());
			new OtpremanjeHandler(this, putanja.getText(), nazivFilma, IPpodservera, port, otpremanje).start();
			otpr.setEnabled(false);
			putanja.setText("");
			nazivFilma = "";
		});
		putanja.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				otpr.setEnabled(putanja.getText().length() > 0);
				int ind = putanja.getText().lastIndexOf("/");
				nazivFilma = ind == -1 ? putanja.getText() : putanja.getText().substring(ind + 1);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				removeUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				removeUpdate(e);
			}
		});

		otpremanje.add(drugiRed);
		JPanel treciRed = new JPanel(new GridLayout(3, 5));
		treciRed.add(new JLabel());
		treciRed.add(otpr);
		treciRed.add(new JLabel());
		treciRed.add(nazad);
		for (int i = 0; i < 11; i++)
			treciRed.add(new JLabel());
		otpremanje.add(treciRed);
		centar.add(otpremanje);

		// gledanje
		gledaj.addActionListener(this);

		centar.add(gledanje);

		this.add(centar, BorderLayout.CENTER);
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

	public void refresh() {
		SwingUtilities.invokeLater(() -> {
			String o = obavestenja.getText();
			this.dispose();
			klijent.setBioskop(new Bioskop(IPpodservera, port, klijent, username, o));
		});
	}

	public void setNit(Thread nit) {
		this.nit = nit;
	}

	public String getUsername() {
		return username;
	}

	public synchronized void dodajObavestenje(String obavestenje) {
		if (obavestenje == null || "null".equals(obavestenje))
			return;

		obavestenja.append(obavestenje + "\n");
		validate();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		pocetna.setVisible(false);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds((int) (screenSize.width * 0.05), (int) (screenSize.height * 0.02),
				(int) (screenSize.width * 0.9), (int) (screenSize.height * 0.93));

		int brVidea = 0;
		VideoNaziviPoruka poruka = new VideoNaziviPoruka(username);

		try (Konekcija kon = new Konekcija(IPpodservera, port)) {
			kon.posaljiPoruku(new VideoNaziviPoruka(username));
			poruka = (VideoNaziviPoruka) kon.primiPoruku();
			brVidea = poruka.getVideosSize();
		} catch (IOException | ClassNotFoundException e1) {
		}

		gledanje.removeAll();
		gledanje.setLayout(new GridLayout(2, 1));

		JPanel prviRed = new JPanel(new BorderLayout());
		JPanel drugiRed = new JPanel(new BorderLayout());

		JPanel prviRedPanel = new JPanel(new BorderLayout());
		JPanel drugiRedPanel = new JPanel(new BorderLayout());

		JPanel gledajPanel = new JPanel(new BorderLayout());
		JLabel gledajLabel = new JLabel("Gledaj");
		gledajLabel.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 24));
		gledajLabel.setHorizontalAlignment(JLabel.CENTER);
		gledajPanel.add(new JSeparator(), BorderLayout.NORTH);
		gledajPanel.add(gledajLabel, BorderLayout.CENTER);
		gledajPanel.add(new JSeparator(), BorderLayout.SOUTH);
		prviRedPanel.add(gledajPanel, BorderLayout.NORTH);
		prviRedPanel.add(prviRed, BorderLayout.CENTER);

		JPanel sobePanel = new JPanel(new BorderLayout());
		JLabel sobeLabel = new JLabel("Sobe");
		sobeLabel.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 24));
		sobeLabel.setHorizontalAlignment(JLabel.CENTER);
		sobePanel.add(new JSeparator(), BorderLayout.NORTH);
		sobePanel.add(sobeLabel, BorderLayout.CENTER);
		sobePanel.add(new JSeparator(), BorderLayout.SOUTH);
		drugiRedPanel.add(sobePanel, BorderLayout.NORTH);
		drugiRedPanel.add(drugiRed, BorderLayout.CENTER);

		if (brVidea == 0) {
			JLabel labela = new JLabel("Nema videa za gledanje");
			labela.setHorizontalAlignment(JLabel.CENTER);
			labela.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 24));
			prviRed.add(labela);
		} else {
			if (brVidea == 1)
				prviRed.setLayout(new GridLayout(1, 1));
			else if (brVidea == 2)
				prviRed.setLayout(new GridLayout(1, 2));
			else if (brVidea <= 4)
				prviRed.setLayout(new GridLayout(2, 2));
			else if (brVidea <= 9)
				prviRed.setLayout(new GridLayout(3, 3));
			else if (brVidea <= 16)
				prviRed.setLayout(new GridLayout(4, 4));

			for (int i = 0; i < brVidea; i++) {
				JPanel video = new JPanel(new BorderLayout());
				JPanel komande = new JPanel(new GridLayout(1, 2));

				JButton gledaj = new JButton("Gledaj video");
				JButton napraviSobu = new JButton("Napravi sobu");
				komande.add(gledaj);
				komande.add(napraviSobu);

				final VideoNaziviPoruka p = poruka;
				final int j = i;

				gledaj.addActionListener(e1 -> {
					new VideoPlayer(this, IPpodservera, port, p.getNaziv(j), p.getUser(j), username);
				});

				napraviSobu.addActionListener(e1 -> {
					new NapraviSobu(this, IPpodservera, port, username, p.getNaziv(j), p.getUser(j));
				});

				JLabel slikaLabel = new JLabel(
						getScaledImage(new ImageIcon("./video-icon.png"), 400 / brVidea, 400 / brVidea));

				video.add(slikaLabel, BorderLayout.CENTER);

				JPanel jug = new JPanel(new GridLayout(2, 1));
				JLabel naziv = new JLabel(poruka.getNaziv(i) + " (" + poruka.getUser(i) + ")");
				naziv.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 14));
				naziv.setHorizontalAlignment(JLabel.CENTER);
				jug.add(naziv);
				jug.add(komande);

				video.add(jug, BorderLayout.SOUTH);

				prviRed.add(video);
			}
		}

		gledanje.add(prviRedPanel);

		JButton nazad = new JButton("Nazad");
		nazad.addActionListener(e1 -> {
			gledanje.setVisible(false);
			pocetna.setVisible(true);
			nazad.setVisible(false);
			this.setBounds((int) (screenSize.width * 0.25), (int) (screenSize.height * 0.25),
					(int) (screenSize.width * 0.5), (int) (screenSize.height * 0.5));
		});

		List<Soba> sobe = poruka.getSobe();
		int brSoba = sobe.size();

		if (brSoba == 0) {
			JLabel labela = new JLabel("Niste clan nijedne sobe");
			labela.setHorizontalAlignment(JLabel.CENTER);
			labela.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 24));
			drugiRed.add(labela);
		} else {
			if (brSoba == 1)
				drugiRed.setLayout(new GridLayout(1, 1));
			else if (brSoba == 2)
				drugiRed.setLayout(new GridLayout(1, 2));
			else if (brSoba <= 4)
				drugiRed.setLayout(new GridLayout(2, 2));
			else if (brSoba <= 9)
				drugiRed.setLayout(new GridLayout(3, 3));
			else if (brSoba <= 16)
				drugiRed.setLayout(new GridLayout(4, 4));

			for (int i = 0; i < brSoba; i++) {
				JPanel soba = new JPanel(new BorderLayout());
				JPanel komande = new JPanel(new GridLayout(1, 2));

				JButton gledaj = new JButton("Gledaj video");
				komande.add(gledaj);

				final int j = i;
				gledaj.addActionListener(e1 -> {
					new VideoPlayer(this, IPpodservera, port, username, sobe.get(j));
				});

				JLabel slikaLabel = new JLabel(
						getScaledImage(new ImageIcon("./video-icon.png"), 400 / brSoba, 400 / brSoba));
				soba.add(slikaLabel, BorderLayout.CENTER);

				JPanel jug = new JPanel(new GridLayout(2, 1));
				JLabel naziv = new JLabel("Soba " + sobe.get(i).getId() + " (admin: " + sobe.get(i).getAdmin() + ")");
				naziv.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 14));
				naziv.setHorizontalAlignment(JLabel.CENTER);
				jug.add(naziv);
				jug.add(komande);

				soba.add(jug, BorderLayout.SOUTH);

				drugiRed.add(soba);
			}
		}
		gledanje.add(drugiRedPanel);

		this.add(nazad, BorderLayout.SOUTH);
		gledanje.setVisible(true);
	}

	public void setIP(String IP) {
		this.IPpodservera = IP;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void finish() {
		SwingUtilities.invokeLater(() -> {
			this.dispose();
		});
	}

	private ImageIcon getScaledImage(ImageIcon img, int w, int h) {
		Image image = img.getImage();
		Image newimg = image.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
		return new ImageIcon(newimg);
	}
}
