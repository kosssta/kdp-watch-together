package kdp;

public class VideoPoruka implements Poruka {
	public static final int BUFFER_CAPACITY = 1024*1024*10;
	
	private byte[] buf = new byte[BUFFER_CAPACITY];
	private int size;
	
	@Override
	public Tip getTip() {
		return Tip.VIDEO;
	}

	public byte[] getBuffer() {
		return buf;
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		if (size >= 0 && size <= BUFFER_CAPACITY)
			this.size = size;
		else
			this.size = 0;
	}
}
