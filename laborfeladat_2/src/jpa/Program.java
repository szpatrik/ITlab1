package jpa;

import java.util.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.NoResultException;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

public class Program {

	private EntityManagerFactory factory;
	private EntityManager em;

	public void initDB() {
		factory = Persistence.createEntityManagerFactory(CommonData.getUnit());
		em = factory.createEntityManager();
	}

	void closeDB() {

		em.close();
	}

	public Program(EntityManager em) {
		this.em = em;
	}

	public Program() {
	}

	public static void main(String[] args) {
		Program app = new Program();
		app.initDB();
		app.startControl();
		app.closeDB();

	}

	public void startControl() {
		// InputStream input = System.in;
		BufferedReader instream = new BufferedReader(new InputStreamReader(
				System.in));
		while (true) {
			try {
				System.out.print(">");
				String inputLine = instream.readLine();
				StringTokenizer tokenizer = new StringTokenizer(inputLine,
						"   ");
				String command = tokenizer.nextToken();

				if ("t".startsWith(command)) {
					ujTipus(readString(tokenizer), readString(tokenizer));
				} else if ("m".startsWith(command)) {
					ujMozdony(readString(tokenizer), readString(tokenizer),
							readString(tokenizer));
				} else if ("s".startsWith(command)) {
					ujVonatszam(readString(tokenizer), readString(tokenizer));
				} else if ("v".startsWith(command)) {
					ujVonat(readString(tokenizer), readString(tokenizer),
							readString(tokenizer), readString(tokenizer));
				} else if ("l".startsWith(command)) {
					String targy = readString(tokenizer);
					if ("t".startsWith(targy)) {
						listazTipus();
					} else if ("m".startsWith(targy)) {
						listazMozdony();
					} else if ("s".startsWith(targy)) {
						listazVonatszam();
					} else if ("v".startsWith(targy)) {
						listazVonat();
					}
				} else if ("x".startsWith(command)) {
					lekerdezes(readString(tokenizer));
				} else if ("e".startsWith(command)) {
					break;
				} else {
					throw new Exception("Hibas parancs! (" + inputLine + ")");
				}
			} catch (Exception e) {
				System.out.println("? " + e.toString());
			}
		}

	}

	static String readString(StringTokenizer tokenizer) throws Exception {
		if (tokenizer.hasMoreElements()) {
			return tokenizer.nextToken();
		} else {
			throw new Exception("Keves parameter!");
		}
	}

	// Uj entitÃ¡sok felvetelehez kapcsolodo szolgaltatások
	protected void ujEntity(Object o) throws Exception {
		em.getTransaction().begin();
		try {
			em.persist(o);
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;
		}
	}

	// Uj tipus felvetele
	public void ujTipus(String azonosito, String fajta) throws Exception {
		// Hozza létre az új "Tipus" entitást és rögzítse adatbázisban az
		// "ujEntity" metódussal.

		// Tipus ellenőrzése
		if (!fajta.equals("goz") && !fajta.equals("diesel")
				&& !fajta.equals("villany")) {
			System.out.println("?Érvénytelen fajta!");
			return;
		}
		// DB lekérés
		Query q = em
				.createQuery("select t from Tipus t where t.azonosito = :azonosito");
		q.setParameter("azonosito", azonosito);
		try {
			q.getSingleResult();
			System.out.println("?Nem sikerült a lekérés");
		} catch (NoResultException e) {
			Tipus tip = new Tipus(azonosito, fajta);
			ujEntity(tip);
		}
	}

	// Uj mozdony felvetele
	public void ujMozdony(String sorszam, String tipusID, String futottkm)
			throws Exception {
		int futott;
		int sorsz;
		try {
			sorsz = Integer.parseInt(sorszam);
			futott = Integer.parseInt(futottkm);
		} catch (NumberFormatException exception) {
			System.out.println("?Ervenytelen szamformatum");
			return;
		}

		Query tipQ = em
				.createQuery("select tip from Tipus tip where tip.azonosito = :azonosito");
		tipQ.setParameter("azonosito", tipusID);

		Tipus t = null;
		try {
			t = (Tipus) tipQ.getSingleResult();
		} catch (NoResultException e) {
			System.out.println("?Nincs talalat");
			return;
		}

		Query mozQ = em
				.createQuery("select moz from Mozdony moz where moz.id = :azonosito");
		mozQ.setParameter("azonosito", sorsz);
		try {
			mozQ.getSingleResult();
			System.out.println("?");
		} catch (NoResultException e) {
			Mozdony moz = new Mozdony(sorsz, futott, t);
			ujEntity(moz);
		}
	}

	// Uj vonatszam felvetele
	public void ujVonatszam(String sorszam, String uthossz) throws Exception {
		// Alakítsa át a megfelelõ típusokra a kapott String paramétereket.
		// Ellenõrizze, hogy van-e már ilyen vonatszám
		// Hozza létre az új "Vonatszám" entitást és rögzítse adatbázisban az
		// "ujEntity" metódussal.
		int szam = 0;
		long hossz = 0;
		// tipusellenorzes
		try {
			szam = Integer.parseInt(sorszam);
			hossz = Long.parseLong(uthossz);
		} catch (NumberFormatException e) {
			System.out.println("?Hiba a típuskonvertálás során!");
			return;
		}
		// db lekérés
		Query qvsz = null;
		try {
			qvsz = em
					.createQuery("select vsz from Vonatszam vsz where vsz.szam = :szam");
			qvsz.setParameter("szam", szam);
		} catch (IllegalArgumentException e1) {
			System.out.println("?" + e1.toString());
		}
		// hibakezeles, es vonatszam hozzaadasa
		try {
			qvsz.getSingleResult();
			System.out.println("?Nem sikerült a lekérés!!!");
		} catch (NoResultException e) {
			Vonatszam vszam = new Vonatszam(szam, hossz);
			ujEntity(vszam);
		}
	}

	// Uj vonat felvetele
	public void ujVonat(String vonatszamAzonosito, String datum,
			String mozdonySorszam, String keses) throws Exception {
		int vszID, mID, kesesInt;
		Date ParsedDate;
		
		try {
		    vszID = Integer.parseInt(vonatszamAzonosito);
		    mID = Integer.parseInt(mozdonySorszam);
		    kesesInt = Integer.parseInt(keses);
		    SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd");
		    ParsedDate = SDF.parse(datum);
		} catch (NumberFormatException e) {
		    System.out.println("?Hiba a típuskonvertálás során!");
		    return;
		} catch (ParseException e) {
		    System.out.println("?Sikertelen Parse");
		    return;
		}
		
		Vonatszam vsz;
		try {
		    Query vszQ = em.createQuery("select vsz from Vonatszam vsz where vsz.szam = :szam");
		    vszQ.setParameter("szam", vszID);
		    vsz = (Vonatszam) vszQ.getSingleResult();
		} catch (NoResultException e) {
		    System.out.println("?Nincs talalat!");
		    return;
		}
		
		Mozdony moz;
		try {
		    Query mozQ = em.createQuery("select m from Mozdony m where m.id = :id");
		    mozQ.setParameter("id", mID);
		    moz = (Mozdony) mozQ.getSingleResult();
		} catch (NoResultException e) {
		    System.out.println("?Nincs talalat");
		    return;
		}
		
		try {
		    Query vQ = em.createQuery("select v from Vonat v where v.datum = :datum and v.vonatSzam = :vsz");
		    vQ.setParameter("datum", ParsedDate);
		    vQ.setParameter("vsz", vsz);
		    vQ.getSingleResult();
		    System.out.println("?");
		    return;
		} catch (NoResultException e) {
		    //jók vagyunk, mehetünk tovább
		    ujEntity(new Vonat(ParsedDate, kesesInt, moz, vsz));
		    em.getTransaction().begin();
		    moz.setFutottkm(moz.getFutottkm() + vsz.getUthossz().intValue());
		    em.persist(moz);
		    em.getTransaction().commit();
		}
	}

	// Listazasi szolgaltatasok
	public void listazEntity(List list) {
		for (Object o : list) {
			System.out.println(o);
		}
	}

	// Tipusok listazasa
	public void listazTipus() throws Exception {
		listazEntity(em.createQuery("SELECT t FROM Tipus t").getResultList());
	}

	// Mozdonyok listazasa
	public void listazMozdony() throws Exception {
		// Készítsen lekérdezést, amely visszaadja az összes mozdonyt, majd
		// irassa ki a listazEntity metódussal az eredményt.
		listazEntity(em.createQuery("select moz from Mozdony moz")
				.getResultList());
	}

	// Vonatszamok listazasa
	public void listazVonatszam() throws Exception {
		// Készítsen lekérdezést, amely visszaadja az összes vonatszámot, majd
		// irassa ki a listazEntity metódussal az eredményt.

		listazEntity(em
				.createQuery("select vonatszam from Vonatszam vonatszam")
				.getResultList());
	}

	// Vonatok listazasa
	public void listazVonat() throws Exception {
		// TODO
		// Készítsen lekérdezést, amely visszaadja az összes vonatot, majd
		// irassa ki a listazEntity metódussal az eredményt.
		listazEntity(em.createQuery("select v from Vonat v").getResultList());
	}

	// Egyedi lekerdezes
	public void lekerdezes(String datum) throws Exception {
		Date date;
		try {
		    SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd");
		    date = SDF.parse(datum);
		} catch (ParseException e) {
		    System.out.println("?Sikertelen Parse");
		    return;
		}
		
		Query q = em.createQuery("select t.fajta, SUM(vsz.uthossz) "
			+ "from Tipus t, Mozdony m, Vonat v, Vonatszam vsz "
			+ "where v.datum = :input and v.vonatSzam = vsz and v.mozdony = m and m.tipus = t "
			+ "group by t.fajta");
		q.setParameter("input", date);
		List<Object[]> result = q.getResultList();
		for (Object[] item : result) {
		    System.out.println(item[0] + " " + item[1]);
		}
	    }
	}

