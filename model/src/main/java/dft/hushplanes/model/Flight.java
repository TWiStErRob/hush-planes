package dft.hushplanes.model;

import javax.persistence.*;

@Entity
@Table
public class Flight {
	@Id
	@Column
	public long id;

	@Column
	public String name;
	
	@Column
	public String origin;
	@Column
	public String destination;
	
	@Column
	public String country;
	@Column
	public String operator;
	@Column
	public String model;
}
