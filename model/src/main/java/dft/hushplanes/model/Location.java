package dft.hushplanes.model;

import java.io.Serializable;

import javax.persistence.*;

import dft.hushplanes.model.Location.CompositeId;

@Entity
@Table
@IdClass(CompositeId.class)
public class Location {
	@Id
	@ManyToOne
	public Flight flight;

	@Id
	public long time;

	@Column
	public Double latitude;
	@Column
	public Double longitude;
	@Column
	public Float speed;
	@Column
	public Float speed_vertical;
	@Column
	public Float bearing;
	@Column
	public String file;

	static class CompositeId implements Serializable {
		long flight;
		long time;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof CompositeId)) {
				return false;
			}
			CompositeId id = (CompositeId)o;
			return flight == id.flight && time == id.time;
		}

		@Override
		public int hashCode() {
			int result = (int)(flight ^ (flight >>> 32));
			result = 31 * result + (int)(time ^ (time >>> 32));
			return result;
		}
	}
}
