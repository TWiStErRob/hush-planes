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
	@Column
	public Integer engines;
	@Column(nullable = false)
	public EngineType engineType;
	@Column(nullable = false)
	public EnginePlacement enginePlacement;

	@OneToMany(fetch = FetchType.EAGER)
	@OrderBy("time ASC")
	public List<Location> path = new ArrayList<>();

	@Transient
	public Location current;

	@Column
	public Runway runway;
	@Column
    public String route;
	@Column
    public Double deviation;

	public enum EngineType {
		None,
		Piston,
		Turbo,
		Jet,
		Electric
	}

	public enum EnginePlacement {
		Unknown,
		AftMounted,
		WingBuried,
		FuselageBuried,
		NoseMounted,
		WingMounted
	}
}
