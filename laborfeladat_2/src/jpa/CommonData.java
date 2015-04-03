package jpa;

public class CommonData {

	private static final String PERSISTENCE_UNIT_NAME = "vonatdb";
	private static final String DBASE_DIRECTORY = ".\\vonatdb\\";

	public static String getDir() {
		return DBASE_DIRECTORY;
	}
	public static String getUnit() {
		return PERSISTENCE_UNIT_NAME;
	}

}
