package com.FST.GestionDesVentes.Entities;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
public class User implements UserDetails {

	
	 @Id
	  @GeneratedValue
	  private Integer id;
	  private String firstname;
	  private String lastname;
	  private String email;
	  private String password;

	  @Enumerated(EnumType.STRING)
	  private Role role;

	  @OneToMany(mappedBy = "user")
	  private List<Token> tokens;

	  
	  public Integer getId() {
		return id;
	}

	  public void setId(Integer id) {
		this.id = id;
	 }

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public void setTokens(List<Token> tokens) {
		this.tokens = tokens;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	  public Collection<? extends GrantedAuthority> getAuthorities() {
	    return role.getAuthorities();
	  }

	  @Override
	  public String getPassword() {
	    return password;
	  }

	  @Override
	  public String getUsername() {
	    return email;
	  }

	  @Override
	  public boolean isAccountNonExpired() {
	    return true;
	  }

	  @Override
	  public boolean isAccountNonLocked() {
	    return true;
	  }

	  @Override
	  public boolean isCredentialsNonExpired() {
	    return true;
	  }

	  @Override
	  public boolean isEnabled() {
	    return true;
	  }

	  public User(String firstname, String lastname, String email, String password, Role role) {
	        this.firstname = firstname;
	        this.lastname = lastname;
	        this.email = email;
	        this.password = password;
	        this.role = role;
	    }

	public User() {
		super();
	}
	  
	  
}
