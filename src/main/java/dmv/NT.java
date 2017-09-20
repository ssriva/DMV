package dmv;

public class NT {

	public NT(){}
	
	static String[] dirset = {"right", "left", "sealed"};
	public static String[] tagset={"ROOT", "#",  "$",  "''",  "(",  ")",  ",",  ".",  ":",  "CC",  "CD",  "DT",  "EX",  "FW",  "IN",  "JJ",  "LS",  "MD",  "NN",  "PD",  "PO",  "PR",  "RB",  "RP",  "SY",  "TO",  "UH",  "VB",  "WD",  "WP",  "WR",  "``"};
	public static String[] finer ={"ROOT", "#",  "$",  "''",  "(",  ")",  ",",  ".",  ":",  "CC",  "CD",  "DT",  "EX",  "FW",  "IN",  "JJ",  "JJR",  "JJS",  "LS",  "MD",  "NN",  "NNP",  "NNPS",  "NNS",  "PDT",  "POS",  "PRP",  "PRP$",  "RB",  "RBR",  "RBS",  "RP",  "SYM",  "TO",  "UH",  "VB",  "VBD",  "VBG",  "VBN",  "VBP",  "VBZ",  "WDT",  "WP",  "WRB",  "``"};
	
	public NT(String pos, String sealing){
		this.pos = pos;
		this.sealing = sealing;
	}
	
	private String pos;
	private String sealing;
	
	public String getPos() {
		return pos;
	}
	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getSealing() {
		return sealing;
	}

	public void setSealing(String sealing) {
		this.sealing = sealing;
	}
}
