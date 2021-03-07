package kdp.klijent;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Screen;
import javafx.util.Duration;
import kdp.Konekcija;
import kdp.Poruka;
import kdp.Progres;
import kdp.Soba;
import kdp.Status;
import kdp.VideoGetPoruka;
import kdp.VideoPoruka;
import kdp.VideoStatusPoruka;
import kdp.VremePoruka;

public class VideoPlayer extends JDialog {
	private JPanel videoPanel = new JPanel();
	private MediaPlayer player;
	private File video_source;
	private BufferedOutputStream fout;
	private Konekcija server;
	private MediaView viewer;
	private Controller kontroler;
	private Soba soba;
	private boolean admin;
	private String korisnik;
	private String naziv;
	private Progres progres;
	private Bioskop bioskop;

	public VideoPlayer(Bioskop bioskop, String IP, int port, String naziv, String username, String korisnik) {
		super(bioskop, naziv, false);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds((int) (screenSize.width * 0.05), (int) (screenSize.height * 0.02),
				(int) (screenSize.width * 0.9), (int) (screenSize.height * 0.93));

		this.add(videoPanel, BorderLayout.CENTER);
		this.admin = true;
		this.korisnik = korisnik;
		this.naziv = naziv;
		this.bioskop = bioskop;

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("Window closing...");
				finish();
			}
		});
		this.setVisible(true);
		getVideo(IP, port, naziv, username, bioskop);
	}

	public VideoPlayer(Bioskop bioskop, String IP, int port, String korisnik, Soba soba) {
		super(bioskop, "Soba " + soba.getId(), false);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds(50, 50, (int) (screenSize.width * 0.9), (int) (screenSize.height * 0.9));

		this.add(videoPanel, BorderLayout.CENTER);
		this.soba = soba;
		this.admin = soba.isAdmin(korisnik);
		this.korisnik = korisnik;

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("Window closing...");
				finish();
			}
		});
		this.setVisible(true);
		getVideo(IP, port, soba.getVideo(), soba.getVlasnikVidea(), bioskop);
	}

	private void finish() {
		if (admin && kontroler != null) {
			System.out.println("Kontroler finish");
			kontroler.finish();
			kontroler = null;
		} else 
			player.dispose();

		try {
			if (fout != null)
				fout.close();
			if (server != null)
				server.close();
		} catch (IOException e1) {
		}
	}

	private void getVideo(String IP, int port, String naziv, String username, Bioskop bioskop) {
		Platform.setImplicitExit(false);
		final JFXPanel VFXPanel = new JFXPanel();
		System.out.println("Port: " + port);
		try {
			Files.createDirectories(Paths.get("./tmp/" + korisnik));
			video_source = new File("./tmp/" + korisnik + "/" + naziv);

			server = new Konekcija(IP, port);

			Poruka por = null;
			boolean postojiFajl = video_source.isFile();
			server.posaljiPoruku(new VideoGetPoruka(naziv, username, korisnik, postojiFajl));
			if (!postojiFajl) {
				video_source.createNewFile();
				fout = new BufferedOutputStream(new FileOutputStream(video_source));

				por = (Poruka) server.primiPoruku();
				if (por instanceof VideoGetPoruka) {
					System.out.println(((VideoGetPoruka) por).getVelicina());
					progres = new Progres(bioskop, naziv, ((VideoGetPoruka) por).getVelicina());
				}

			}
			new Thread(() -> {
				try {
					if (!postojiFajl) {
						Poruka poruka = (Poruka) server.primiPoruku();

						while (poruka instanceof VideoPoruka) {
							VideoPoruka p = (VideoPoruka) poruka;
							fout.write(p.getBuffer(), 0, p.getSize());
							if (progres != null)
								progres.postaviProgres(progres.getProgres() + p.getSize());
							server.posaljiPoruku(new VideoGetPoruka(naziv, username, korisnik));
							poruka = (Poruka) server.primiPoruku();
						}

						if (progres != null)
							progres.dispose();
					}

					Media m = new Media(video_source.toURI().toString());
					player = new MediaPlayer(m);

					viewer = new MediaView(player);

					StackPane root = new StackPane();

					Scene scene = new Scene(root);

					// center video position
					javafx.geometry.Rectangle2D screen = Screen.getPrimary().getVisualBounds();
					viewer.setX((screen.getWidth() - videoPanel.getWidth()) / 2);
					viewer.setY((screen.getHeight() - videoPanel.getHeight()) / 2);

					root.getChildren().add(viewer);

					VFXPanel.setScene(scene);

					videoPanel.setLayout(new BorderLayout());
					videoPanel.add(VFXPanel, BorderLayout.CENTER);

					// resize video based on screen size
					DoubleProperty width = viewer.fitWidthProperty();
					DoubleProperty height = viewer.fitHeightProperty();
					width.bind(Bindings.selectDouble(viewer.sceneProperty(), "width"));
					height.bind(Bindings.selectDouble(viewer.sceneProperty(), "height"));
					viewer.setPreserveRatio(true);

					if (soba != null)
						server.posaljiPoruku(new VideoStatusPoruka(soba.getVreme(), soba.isPlaying(), soba.getId(),
								soba.getAdmin(), korisnik));
					else
						server.posaljiPoruku(new VideoStatusPoruka(0, false, 0, "", ""));

					VideoStatusPoruka p = (VideoStatusPoruka) server.primiPoruku();
					
					if (p.isStop()) {
						finish();
						bioskop.dodajObavestenje("Server trenutno nije dostupan.");
						this.dispose();
					}
					
					if (soba != null) {
						soba.setVreme(p.getVreme());
						soba.setPlaying(p.isPlaying());
						System.out.println("Vreme " + soba.getVreme());
						System.out.println("Playing " + soba.isPlaying());
					}
					if (admin) {
						kontroler = new Controller(this, player, videoPanel, soba, server);
						kontroler.start();
					} else {
						if (soba == null)
							player.play();
						else {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
							}
							player.play();
							player.pause();
							player.seek(new Duration(soba.getVreme()));
							videoPanel.validate();
							System.out.println("Seeeeek " + soba.getVreme());
							System.out.println("Cur time: " + player.getCurrentTime());
							if (soba.isPlaying())
								player.play();

							Thread nit = new Thread(() -> {
								try {
									while (true) {
										VideoStatusPoruka po = (VideoStatusPoruka) server.primiPoruku();
										System.out.println("Primio " + po.getVreme() + " - " + po.isPlaying());
										if (po.isStop())
											throw new IOException();
										if (po.isPlaying()) {
											player.play();
											player.seek(new Duration(po.getVreme()));
											System.out.println("Pusti");
										} else {
											System.out.println("Pauza");
											player.pause();
										}
									}
								} catch (IOException | ClassNotFoundException e) {
									finish();
									this.dispose();
								}
							});
							nit.setDaemon(true);
							nit.start();
						}
					}
					Thread nit = new Thread(() -> {
						try (Konekcija kon = new Konekcija(IP, port)) {
							kon.posaljiPoruku(new Status(""));
							kon.primiPoruku();
						} catch (IOException | ClassNotFoundException e) {
							finish();
							this.dispose();
						}
					});
					nit.setDaemon(true);
					nit.start();
				} catch (IOException | ClassNotFoundException e1) {
					finish();
					this.dispose();
				}
			}).start();

		} catch (IOException | ClassNotFoundException e) {
			finish();
			this.dispose();
		}
	}

	public String getNazivVideo() {
		return naziv;
	}

	public String getUsername() {
		return korisnik;
	}

	public void dodajObavestenje(String obavestenje) {
		bioskop.dodajObavestenje(obavestenje);
	}
}

class Controller extends Thread {
	private VideoPlayer videoPlayer;
	private MediaPlayer player;
	private JPanel videoPanel;
	private boolean playing = true;
	private JButton pauza;
	private JSlider slider;
	private SliderThread sliderThread;
	private Soba soba;
	private Konekcija server;
	private FileOutputStream fout;

	public Controller(VideoPlayer videoPlayer, MediaPlayer player, JPanel videoPanel, Soba soba, Konekcija server) {
		this.player = player;
		this.videoPanel = videoPanel;
		this.soba = soba;
		this.server = server;
		this.videoPlayer = videoPlayer;
		this.setDaemon(true);
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000);

			double seek = 0;
			if (soba == null)
				seek = ((VremePoruka) server.primiPoruku()).getVreme();

			pauza = new JButton("Pauza");
			pauza.addActionListener(e -> {
				if (playing) {
					player.pause();
					sliderThread.finish();
					pauza.setText("Pusti");
				} else {
					player.play();
					player.setOnPlaying(new Runnable() {
						@Override
						public void run() {
							sliderThread = new SliderThread(Controller.this, soba == null ? server : null);
						}
					});
					pauza.setText("Pauza");
				}
				playing = !playing;

				if (soba != null) {
					soba.setPlaying(playing);
					soba.setVreme((long) player.getCurrentTime().toMillis());
					try {
						server.posaljiPoruku(new VideoStatusPoruka((long) player.getCurrentTime().toMillis(), playing));
					} catch (IOException e1) {
						finish();
					}
				}
			});

			int i = 0;
			for (; player.getMedia().getDuration().isUnknown() && i < 5; i++) {
				Thread.sleep(1000);
				System.out.println("Controller duration: " + (int) player.getMedia().getDuration().toSeconds());
			}

			if (i == 5) {
				Files.delete(Paths.get("./tmp/" + videoPlayer.getUsername() + "/" + videoPlayer.getNazivVideo()));
				return;
			}

			slider = new JSlider(0, (int) player.getMedia().getDuration().toSeconds(), 0);
			slider.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					sliderThread.finish();
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					player.pause();
					player.seek(new Duration(1000 * slider.getValue()));
					if (soba != null) {
						soba.setPlaying(true);
						soba.setVreme((long) new Duration(1000 * slider.getValue()).toMillis());
						try {
							server.posaljiPoruku(new VideoStatusPoruka(1000 * slider.getValue(), true));
						} catch (IOException e1) {
							finish();
						}
					}
					player.play();
					player.setOnPlaying(new Runnable() {
						@Override
						public void run() {
							sliderThread = new SliderThread(Controller.this, soba == null ? server : null);
						}
					});
					pauza.setText("Pauza");
				}
			});

			JPanel jug = new JPanel(new GridLayout(2, 1));
			jug.add(slider);
			jug.add(pauza);
			videoPanel.add(jug, BorderLayout.SOUTH);
			videoPanel.validate();

			player.setOnPlaying(new Runnable() {
				@Override
				public void run() {
					sliderThread = new SliderThread(Controller.this, soba == null ? server : null);
				}
			});

			if (soba == null) {
				player.play();
				player.seek(new Duration(seek));
			} else {
				player.play();
				player.seek(new Duration(soba.getVreme()));
				if (soba != null) {
					try {
						server.posaljiPoruku(new VideoStatusPoruka(soba.getVreme(), true));
					} catch (IOException e1) {
						finish();
					}
				}
				System.out.println("Start " + soba.getVreme());
			}
		} catch (InterruptedException | ClassNotFoundException | IOException e) {
		}
	}

	public void finish() {
		if (sliderThread != null)
			sliderThread.finish();
		try {
			if (fout != null)
				fout.close();
		} catch (IOException e) {
		}
		player.dispose();
	}

	public synchronized void stopSliderThread() {
		if (sliderThread != null) {
			sliderThread.interrupt();
			sliderThread = null;
		}
	}

	public synchronized void startSliderThread() {
		if (sliderThread != null) {
			sliderThread.interrupt();
		}
		sliderThread = new SliderThread(this, server);
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public void setPauzaLabel(String label) {
		pauza.setText(label);
	}

	public JSlider getSlider() {
		return slider;
	}

	public MediaPlayer getMediaPlayer() {
		return player;
	}

	public VideoPlayer getVideoPlayer() {
		return videoPlayer;
	}
}

class SliderThread extends Thread {
	private MediaPlayer player;
	private JSlider slider;
	private Controller kontroler;
	private boolean stop;
	private Konekcija server;

	public SliderThread(Controller kontroler, Konekcija server) {
		this.kontroler = kontroler;
		this.player = kontroler.getMediaPlayer();
		this.slider = kontroler.getSlider();
		this.server = server;
		this.setDaemon(true);
		start();
	}

	@Override
	public void run() {
		try {
			do {
				if (player.getStatus() == MediaPlayer.Status.DISPOSED)
					return;
				else if (slider.getValue() == slider.getMaximum()) {
					player.seek(new Duration(0));
					player.pause();
					slider.setValue(0);
					kontroler.setPauzaLabel("Pusti");
					kontroler.setPlaying(false);
					return;
				}

				slider.setValue((int) player.getCurrentTime().toSeconds());
				if (server != null)
					try {
						server.posaljiPoruku(new VremePoruka(player.getCurrentTime().toMillis()));
					} catch (IOException e) {
						kontroler.getVideoPlayer().dodajObavestenje("Izgubljena konekcija sa serverom");
						kontroler.finish();
					}
				Thread.sleep(1000);
			} while (!stop);
		} catch (InterruptedException e) {
		}
	}

	public void finish() {
		stop = true;
	}
}
