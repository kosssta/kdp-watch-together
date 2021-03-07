package kdp;

import java.io.Serializable;

public interface Poruka extends Serializable {
	public enum Tip { INIT, PROVERA, STATUS, REGISTRACIJA, PRIJAVA };
	
	public Tip getTip();
}
