package kdp;

import java.io.Serializable;

public interface Poruka extends Serializable {
	public enum Tip { INIT, PROVERA, STATUS, REGISTRACIJA, PRIJAVA, VIDEO, VIDEO_PROVERA, VIDEO_NAZIVI, VIDEO_GET, KORISNICI_GET,
		SOBA, VIDEO_STATUS, SOBA_INFO, OBAVESTENJE, VREME, VIDEO_INFO };

	public Tip getTip();
}
