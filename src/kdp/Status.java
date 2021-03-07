package kdp;

public class Status implements Poruka {
	private String status;
	
	public Status(String status) {
		this.status = status;
	}
	
	@Override
	public Tip getTip() {
		return Tip.STATUS;
	}
	
	public String getStatus() {
		return status;
	}
}
