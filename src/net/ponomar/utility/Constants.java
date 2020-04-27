package net.ponomar.utility;

public final class Constants {

	private Constants() {

	}

	public static final String LANGUAGES_PATH = "src/languages";
	public static final String SERVICES_PATH = "xml/Services/";

	public static final String IMAGES_RESOURCE_PATH = "/images/";
    public static final String IMAGES_PATH="src" + IMAGES_RESOURCE_PATH;


	public static final String ICONS_RESOURCE_PATH = "/icons/";
	public static final String ICON_PATH = Constants.IMAGES_PATH+"icons/";

	
	public static final String PREVIOUS_BUTTON = Constants.IMAGES_RESOURCE_PATH + "0.gif";
	public static final String NEXT_BUTTON = Constants.IMAGES_RESOURCE_PATH + "2.gif";
	public static final String DEFAULT_ICON = Constants.LANGUAGES_PATH+"/cu"+Constants.ICONS_RESOURCE_PATH+"4447/0.jpg";

	public static final String ICONS_LOCATION = "src/images/icons/";

	public static final String BML_FILE = LANGUAGES_PATH + "/xml/bible.xml";

	public static final String CONFIG_FILE = LANGUAGES_PATH + "/xml/ponomar.config";

	public static final String TRIODION_PATH = "xml/triodion/";
	public static final String PENTECOSTARION_PATH = "xml/pentecostarion/";
	
	public static final String COMMANDS = "xml/Commands/";
	public static final String LANGUAGE_PACKS = COMMANDS + "LanguagePacks.xml";
    public static final String DIVINE_LITURGY = COMMANDS + "DivineLiturgy.xml";



	// Purely functional
	public static final String NEWLINE = "\n";
}
