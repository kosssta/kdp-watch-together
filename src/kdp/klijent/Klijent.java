package kdp.klijent;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import kdp.LoginPoruka;
import kdp.Poruka;
import kdp.Status;
import kdp.Poruka.Tip;

public class Klijent extends JFrame implements ActionListener {
	private static String HOST = "192.168.8.105";
	private static int PORT = 4567;

	private JButton registracija;
	private JButton login;
	private JLabel potvrdaLozinkeLabela;
	private JTextField potvrdaLozinke;
	private JTextField username;
	private JTextField password;
	private JLabel greska;

	public Klijent() {
		super("Klijent");
		this.setBounds(1000, 300, 400, 300);
		dodajKomponente();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	private void dodajKomponente() {
		JButton registracijaMeni = new JButton("Registracija");
		JButton prijavaMeni = new JButton("Prijava");
		registracijaMeni.addActionListener(e -> {
			potvrdaLozinkeLabela.setVisible(true);
			potvrdaLozinke.setVisible(true);
			login.setVisible(false);
			registracija.setVisible(true);
		});
		prijavaMeni.addActionListener(e -> {
			potvrdaLozinkeLabela.setVisible(false);
			potvrdaLozinke.setVisible(false);
			registracija.setVisible(false);
			login.setVisible(true);
		});
		JPanel sever = new JPanel(new GridLayout(1, 2));
		sever.add(registracijaMeni);
		sever.add(prijavaMeni);
		this.add(sever, BorderLayout.NORTH);

		username = new JTextField();
		password = new JTextField();
		potvrdaLozinke = new JTextField();
		JLabel usernameLabela = new JLabel("Korisnicko ime:");
		JLabel passwordLabela = new JLabel("Lozinka:");
		potvrdaLozinkeLabela = new JLabel("Potvrda lozinke:");
		potvrdaLozinke.setVisible(false);
		potvrdaLozinkeLabela.setVisible(false);

		JPanel centar = new JPanel(new GridLayout(3, 2));
		centar.add(usernameLabela);
		centar.add(username);
		centar.add(passwordLabela);
		centar.add(password);
		centar.add(potvrdaLozinkeLabela);
		centar.add(potvrdaLozinke);
		this.add(centar, BorderLayout.CENTER);

		registracija = new JButton("Registracija");
		login = new JButton("Prijava");
		registracija.addActionListener(this);
		login.addActionListener(this);

		JPanel jug = new JPanel(new GridLayout(2, 1));
		greska = new JLabel("");
		greska.setForeground(Color.RED);
		greska.setAlignmentX(CENTER_ALIGNMENT);
		jug.add(greska);
		JPanel buttons = new JPanel(new CardLayout());
		buttons.add(login);
		buttons.add(registracija);
		jug.add(buttons);
		this.add(jug, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		greska.setText("");
		if (e.getSource() == registracija)
			registracija();
		else if (e.getSource() == login)
			prijava();
	}

	private void registracija() {
		String usernameString = username.getText();
		String passwordString = password.getText();
		String potvrdaString = potvrdaLozinke.getText();

		if ("".equals(usernameString) || "".equals(passwordString) || "".equals(potvrdaString)) {
			greska.setText("Sva polja moraju biti popunjena");
			return;
		}

		if (!passwordString.equals(potvrdaString)) {
			greska.setText("Potvrda lozinke se razlikuje od lozinke");
			return;
		}

		Socket socket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;

		try {
			socket = new Socket(HOST, PORT);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());

			out.writeObject(new LoginPoruka(usernameString, passwordString, Poruka.Tip.REGISTRACIJA));
			Status status = (Status) in.readObject();
			greska.setText(status.getStatus());
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void prijava() {
		String usernameString = username.getText();
		String passwordString = password.getText();

		if ("".equals(usernameString) || "".equals(passwordString)) {
			greska.setText("Sva polja moraju biti popunjena");
			return;
		}

		Socket socket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;

		try {
			socket = new Socket(HOST, PORT);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());

			out.writeObject(new LoginPoruka(usernameString, passwordString, Poruka.Tip.PRIJAVA));
			Status status = (Status) in.readObject();
			if ("Greska".equals(status.getStatus()))
				greska.setText(status.getStatus());
			else {
				String[] odgovor = status.getStatus().split("#");
				if (odgovor.length < 2)
					greska.setText("Greska " + status.getStatus());
				else {
					this.setVisible(false);
					new Bioskop(odgovor[0], Integer.parseInt(odgovor[1]), usernameString);
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new Klijent();
	}

}
