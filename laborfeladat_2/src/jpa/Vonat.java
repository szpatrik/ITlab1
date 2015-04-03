package jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
public class Vonat {

	@Temporal(TemporalType.DATE)
	private Date datum;
	private int keses;
	@ManyToOne
	private Mozdony mozdony;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne
	private Vonatszam vonatSzam;

	public Vonat() {
	}

	public Vonat(Date date, int keses, Mozdony moz, Vonatszam vsz) {
		this.datum = date;
		this.keses = keses;
		this.mozdony = moz;
		this.vonatSzam = vsz;
	}

	public int getId() {
		return id;
	}

	public Date getDatum() {
		return datum;
	}

	public void setDatum(Date datum) {
		this.datum = datum;
	}

	public int getKeses() {
		return keses;
	}

	public void setKeses(int keses) {
		this.keses = keses;
	}

	public Mozdony getMozdony() {
		return mozdony;
	}

	public void setMozdony(Mozdony moz) {
		this.mozdony = moz;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		return vonatSzam.getSzam() + " " + sdf.format(datum) + " "
				+ mozdony.getId() + " " + mozdony.getFutottkm() + " " + keses;
	}

	public Vonatszam getVonatSzam() {
		return vonatSzam;
	}

	public void setVonatSzam(Vonatszam vonatSzam) {
		this.vonatSzam = vonatSzam;
	}

}
