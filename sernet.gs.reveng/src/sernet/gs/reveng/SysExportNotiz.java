package sernet.gs.reveng;

// Generated Jun 5, 2015 1:28:30 PM by Hibernate Tools 3.4.0.CR1

import java.util.Date;

/**
 * SysExportNotiz generated by hbm2java
 */
public class SysExportNotiz implements java.io.Serializable {

	private SysExportNotizId id;
	private SysExport sysExport;
	private Date erstelltAm;

	public SysExportNotiz() {
	}

	public SysExportNotiz(SysExportNotizId id, SysExport sysExport,
			Date erstelltAm) {
		this.id = id;
		this.sysExport = sysExport;
		this.erstelltAm = erstelltAm;
	}

	public SysExportNotizId getId() {
		return this.id;
	}

	public void setId(SysExportNotizId id) {
		this.id = id;
	}

	public SysExport getSysExport() {
		return this.sysExport;
	}

	public void setSysExport(SysExport sysExport) {
		this.sysExport = sysExport;
	}

	public Date getErstelltAm() {
		return this.erstelltAm;
	}

	public void setErstelltAm(Date erstelltAm) {
		this.erstelltAm = erstelltAm;
	}

}