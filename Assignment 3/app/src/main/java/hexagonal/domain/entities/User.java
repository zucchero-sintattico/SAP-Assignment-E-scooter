package hexagonal.domain.entities;

import java.util.Objects;

public class User {

	private String id;
	private String name;
	private String surname;
	
	public User(String id, String name, String surname) {
		this.id = id;
		this.name = name;
		this.surname = surname;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getSurname() {
		return surname;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return Objects.equals(id, user.id) && Objects.equals(name, user.name) && Objects.equals(surname, user.surname);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, surname);
	}
}