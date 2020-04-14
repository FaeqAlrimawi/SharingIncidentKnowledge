package ie.lero.spare.forensics.assessment.brs.parser;


public class BigraphERTokens {

	// defines . for containment
	public static final String TOKEN_CONTAINMENT = "\\.";
	public static final int CONTAINMENT = 1;

	// defines | for entity juxtaposition
	public static final String TOKEN_ENTITY_JUXTAPOSITION = "\\|";
	public static final int ENTITY_JUXTAPOSITION = 2;

	// defines || for bigraph juxtaposition
	public static final String TOKEN_BIGRAPH_JUXTAPOSITION = "\\|{2}";
	public static final int BIGRAPH_JUXTAPOSITION = 3;

	// defines id for site
	public static final String TOKEN_SITE = "id";
	public static final int SITE = 4;

	// open and close brackets ()
	public static final String TOKEN_OPEN_BRACKET = "\\(";
	public static final int OPEN_BRACKET = 5;
	public static final String TOKEN_CLOSED_BRACKET = "\\)";
	public static final int CLOSED_BRACKET = 6;

	// open and close brackets for connectivity {}
	public static final String TOKEN_OPEN_BRACKET_CONNECTIVITY = "\\{";
	public static final int OPEN_BRACKET_CONNECTIVITY = 7;
	public static final String TOKEN_CLOSED_BRACKET_CONNECTIVITY = "\\}";
	public static final int CLOSED_BRACKET_CONNECTIVITY = 8;

	// connectivity closed /
	public static final String TOKEN_CLOSED_CONNECTIVITY = "\\/";
	public static final int CLOSED_CONNECTIVITY = 9;

	// comma for separating connectivity names in {con1,con2,...}
	public static final String TOKEN_COMMA = ",";
	public static final int COMMA = 10;

	// defines words whether entities or activities
	public static final String TOKEN_WORD = "[a-zA-Z][a-zA-Z0-9_]*";
	public static final int WORD = 11;

	// Represents exactly one whitespace character
	public static final String TOKEN_SMALL_SPACE = "\\s+";//"[\\t\\n\\x0B\f]";// "\\s";
	public static final int SMALL_SPACE = 12;
	
	// defines * for bigraph composition
	public static final String TOKEN_COMPOSITION = "\\*";
	public static final int COMPOSITION = 13;
	
	// defines 1 for grounding a bigraph
	public static final String TOKEN_1 = "1";
	public static final int ONE_1 = 14;
}
