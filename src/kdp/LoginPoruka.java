package kdp;

public class LoginPoruka implements Poruka {
	private Tip tip;
	private String username;
	private String password;

	public LoginPoruka(String username, String password, Tip tip) {
		this.username = username;
		this.password = password;
		this.tip = tip;
	}

	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	@Override
	public Tip getTip() {
		return tip;
	}

}
