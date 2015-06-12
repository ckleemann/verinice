package sernet.gs.reveng;

// Generated Jun 5, 2015 1:28:30 PM by Hibernate Tools 3.4.0.CR1

/**
 * MPersbezTxtId generated by hbm2java
 */
public class MPersbezTxtId implements java.io.Serializable {

	private byte pbzId;
	private short sprId;

	public MPersbezTxtId() {
	}

	public MPersbezTxtId(byte pbzId, short sprId) {
		this.pbzId = pbzId;
		this.sprId = sprId;
	}

	public byte getPbzId() {
		return this.pbzId;
	}

	public void setPbzId(byte pbzId) {
		this.pbzId = pbzId;
	}

	public short getSprId() {
		return this.sprId;
	}

	public void setSprId(short sprId) {
		this.sprId = sprId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof MPersbezTxtId))
			return false;
		MPersbezTxtId castOther = (MPersbezTxtId) other;

		return (this.getPbzId() == castOther.getPbzId())
				&& (this.getSprId() == castOther.getSprId());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getPbzId();
		result = 37 * result + this.getSprId();
		return result;
	}

}