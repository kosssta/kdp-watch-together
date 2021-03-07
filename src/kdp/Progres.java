package kdp;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class Progres extends JDialog implements ActionListener {
	private JProgressBar progres;

	public Progres(JFrame parent, String nazivVidea, String putanja) {
		super(parent, nazivVidea, false);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds((int) (screenSize.width * (0.5 -0.167)), (int) (screenSize.height * (0.5 - 0.167)),
				(int) (screenSize.width * 0.33), (int) (screenSize.height * 0.33));
		
		File file = new File(putanja);
		if (file.isFile())
			dodajKomponente((int) file.length());

		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.setVisible(true);
	}
	
	public Progres(JFrame parent, String nazivVidea, long velicina) {
		super(parent, nazivVidea, false);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds(0, 0,
				(int) (screenSize.width * 0.33), (int) (screenSize.height * 0.33));
		
		dodajKomponente((int) velicina);

		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.setVisible(true);
	}

	private void dodajKomponente(int velicina) {
		this.setLayout(new GridLayout(5, 1));
		progres = new JProgressBar(0, velicina);
		
		this.add(new JLabel(" "));
		this.add(new JLabel(" "));
		this.add(progres);
		this.add(new JLabel(" "));
		this.add(new JLabel(" "));
	}

	public void postaviProgres(int vrednost) {
		SwingUtilities.invokeLater(() -> {
			progres.setValue(vrednost);
		});
	}
	
	public int getProgres() {
		return progres.getValue();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

	}
}
