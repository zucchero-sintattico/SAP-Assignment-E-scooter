package user_service.entities;

import java.util.Objects;

public class User {

	private final String name;
	private final String email;
	private final String password;
	private final boolean isMaintainer;
	
	public User(String name, String email, String password, boolean isMaintainer) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.isMaintainer = isMaintainer;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public boolean isMaintainer() {
		return isMaintainer;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return isMaintainer == user.isMaintainer && Objects.equals(name, user.name) && Objects.equals(email, user.email) && Objects.equals(password, user.password);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, email, password, isMaintainer);
	}
}