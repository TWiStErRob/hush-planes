package dft.hushplanes.model;

import java.util.*;

import javax.persistence.*;

@Entity
@Table
public class Flight {
	@Id
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

	@OneToMany(fetch = FetchType.EAGER)
	@OrderBy("time ASC")
	public List<Location> path = new ArrayList<>();
}
