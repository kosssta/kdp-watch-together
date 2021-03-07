package kdp;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Konekcija implements Closeable {
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public Konekcija(String IP, int port) throws UnknownHostException, IOException {
		socket = new Socket();
		socket.connect(new InetSocketAddress(IP, port), 2500);
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
	}
	
	public Konekcija(String IP, int port, int timeout) throws UnknownHostException, IOException {
		socket = new Socket();
		socket.connect(new InetSocketAddress(IP, port), timeout);
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
	}

	public Konekcija(Socket socket) throws IOException {
		this.socket = socket;
		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.in = new ObjectInputStream(socket.getInputStream());
	}

	public Konekcija(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
		this.socket = socket;
		this.in = in;
		this.out = out;
	}

	public void posaljiObjekat(Object objekat) throws IOException {
		out.writeObject(objekat);
	}

	public Object primiObjekat() throws ClassNotFoundException, IOException {
		return in.readObject();
	}

	public void posaljiPoruku(Poruka poruka) throws IOException {
		out.writeObject(poruka);
	}

	public Poruka primiPoruku() throws ClassNotFoundException, IOException {
		return (Poruka) in.readObject();
	}

	public String getIP() {
		return socket != null ? socket.getInetAddress().getHostAddress() : null;
	}
	
	public int getPort() {
		return socket != null ? socket.getPort() : null;
	}
	
	public void setTimeout(int timeout) throws SocketException {
		socket.setSoTimeout(timeout);
	}

	@Override
	public void close() throws IOException {
		in.close();
		out.close();
		socket.close();
	}

}
