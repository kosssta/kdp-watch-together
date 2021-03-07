package kdp.klijent;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import kdp.Konekcija;
import kdp.KorisniciGet;
import kdp.Soba;
import kdp.SobaPoruka;
import kdp.Status;

public class NapraviSobu extends JDialog implements ActionListener {
	private Bioskop bioskop;
	private String IPpodservera;
	private int port;
	private String username;
	private String video;
	private String vlasnikVidea;

	private JTextArea dodatiKorisniciTextArea;
	private List<String> dodatiKorisnici;
	private List<JCheckBox> sviKorisnici;

	public NapraviSobu(Bioskop bioskop, String IP, int port, String korisnik, String video, String vlasnikVidea) {
		super(bioskop, "Napravi sobu", false);
		this.bioskop = bioskop;
		this.IPpodservera = IP;
		this.port = port;
		this.username = korisnik;
		this.video = video;
		this.vlasnikVidea = vlasnikVidea;

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds((int) (screenSize.width * (0.5 - 0.167)), (int) (screenSize.height * (0.5 - 0.167)),
				(int) (screenSize.width * 0.33), (int) (screenSize.height * 0.33));
		this.setMinimumSize(new Dimension((int) (screenSize.width * 0.33), (int) (screenSize.height * 0.33)));

		dodajKomponente();

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}

	private void dodajKomponente() {
		this.setLayout(new BorderLayout());

		JLabel dodatiKorisniciLabel = new JLabel("Dodati korisnici");
		dodatiKorisniciLabel.setHorizontalAlignment(JLabel.CENTER);
		dodatiKorisniciLabel.setFont(new Font("Arial", Font.BOLD, 18));
		dodatiKorisniciTextArea = new JTextArea("", 20, 30);
		dodatiKorisniciTextArea.setEditable(false);
		JPanel east = new JPanel(new BorderLayout());
		east.add(dodatiKorisniciLabel, BorderLayout.NORTH);
		east.add(new JScrollPane(dodatiKorisniciTextArea), BorderLayout.CENTER);
		this.add(east, BorderLayout.EAST);

		JPanel centar = new JPanel(new BorderLayout());
		this.add(centar, BorderLayout.CENTER);

		JLabel labela = new JLabel("Pretraga korisnika:");
		labela.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 18));
		JTextField pretraga = new JTextField("");
		JPanel sviKorisniciPanel = new JPanel(new GridLayout(0, 1));
		dodatiKorisnici = new ArrayList<String>();
		List<String> korisnici = new ArrayList<String>();
		sviKorisnici = new ArrayList<JCheckBox>();

		try (Konekcija podserver = new Konekcija(IPpodservera, port)) {
			podserver.setTimeout(5000);

			podserver.posaljiPoruku(new KorisniciGet(null));
			korisnici = ((KorisniciGet) podserver.primiPoruku()).getKorisnici();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		JPanel pretragaPanel = new JPanel(new BorderLayout());
		pretragaPanel.add(labela, BorderLayout.NORTH);
		pretragaPanel.add(pretraga, BorderLayout.CENTER);
		centar.add(pretragaPanel, BorderLayout.NORTH);
		centar.add(sviKorisniciPanel, BorderLayout.CENTER);

		if (korisnici != null)
			for (String k : korisnici) {
				JCheckBox checkBox = new JCheckBox(k);
				checkBox.addItemListener(e -> {
					if (checkBox.isSelected()) {
						dodatiKorisniciTextArea.append(k + "\n");
						dodatiKorisnici.add(k);
					} else {
						dodatiKorisnici.remove(checkBox.getText());
						dodatiKorisniciTextArea.setText("");
						for (String kor : dodatiKorisnici) {
							dodatiKorisniciTextArea.append(kor + "\n");
						}
					}
				});
				sviKorisnici.add(checkBox);
				if (!username.equals(k))
					sviKorisniciPanel.add(checkBox);
			}

		pretraga.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				try {
					String text = e.getDocument().getText(0, e.getDocument().getLength()).toLowerCase();
					sviKorisniciPanel.removeAll();
					boolean postoji = false;
					for (JCheckBox k : sviKorisnici) {
						String user = k.getText();
						if (!username.equals(user) && (text.length() == 0 || user.toLowerCase().contains(text))) {
							postoji = true;
							sviKorisniciPanel.add(k);
						}
					}
					sviKorisniciPanel.setVisible(postoji);
					validate();
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
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

		JButton napraviSobu = new JButton("Napravi sobu");
		JButton nazad = new JButton("Nazad");
		JPanel jug = new JPanel(new GridLayout(1, 2));

		jug.add(napraviSobu);
		jug.add(nazad);
		centar.add(jug, BorderLayout.SOUTH);

		napraviSobu.addActionListener(this);

		nazad.addActionListener(e -> {
			this.dispose();
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try (Konekcija podserver = new Konekcija(IPpodservera, port)) {
			podserver.setTimeout(5000);

			Soba soba = new Soba(video, vlasnikVidea, username);
			soba.dodajClanove(dodatiKorisnici);
			podserver.posaljiPoruku(new SobaPoruka(soba));

			Status status = (Status) podserver.primiPoruku();

			if (!"OK".equals(status.getStatus())) {
				bioskop.dodajObavestenje("Doslo je do greske pri pravljenju sobe.");
			} else {
				bioskop.dodajObavestenje("Soba uspesno napravljena.");
				this.dispose();
				bioskop.refresh();
			}
		} catch (IOException | ClassNotFoundException e1) {
			bioskop.dodajObavestenje("Doslo je do greske pri pravljenju sobe.");
		}
	}
}
