/* *
 * Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2017.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2017 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2017
  */

package cop5556fa17;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Scanner {
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		
		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}
		
		public int getPos() { return pos; }

	}

	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, 
		KW_x/* x */, KW_X/* X */, KW_y/* y */, KW_Y/* Y */, KW_r/* r */, KW_R/* R */, KW_a/* a */, 
		KW_A/* A */, KW_Z/* Z */, KW_DEF_X/* DEF_X */, KW_DEF_Y/* DEF_Y */, KW_SCREEN/* SCREEN */, 
		KW_cart_x/* cart_x */, KW_cart_y/* cart_y */, KW_polar_a/* polar_a */, KW_polar_r/* polar_r */, 
		KW_abs/* abs */, KW_sin/* sin */, KW_cos/* cos */, KW_atan/* atan */, KW_log/* log */, 
		KW_image/* image */,  KW_int/* int */, 
		KW_boolean/* boolean */, KW_url/* url */, KW_file/* file */, OP_ASSIGN/* = */, OP_GT/* > */, OP_LT/* < */, 
		OP_EXCL/* ! */, OP_Q/* ? */, OP_COLON/* : */, OP_EQ/* == */, OP_NEQ/* != */, OP_GE/* >= */, OP_LE/* <= */, 
		OP_AND/* & */, OP_OR/* | */, OP_PLUS/* + */, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, 
		OP_POWER/* ** */, OP_AT/* @ */, OP_RARROW/* -> */, OP_LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, SEMI/* ; */, COMMA/* , */, EOF;
	}
	
	public static enum State{
		START, IN_DIGIT,IN_IDENT, AFTER_EQ, AFTER_EXCL, AFTER_MUL, AFTER_GT, AFTER_LT, AFTER_MINUS, AFTER_FSLASH, IN_STRING;
	}

	/** Class to represent Tokens. 
	 * 
	 * This is defined as a (non-static) inner class
	 * which means that each Token instance is associated with a specific 
	 * Scanner instance.  We use this when some token methods access the
	 * chars array in the associated Scanner.
	 * 
	 * 
	 * @author Beverly Sanders
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos;
		public final int length;
		public final int line;
		public final int pos_in_line;

		public Token(Kind kind, int pos, int length, int line, int pos_in_line) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.line = line;
			this.pos_in_line = pos_in_line;
		}

		public String getText() {
			if (kind == Kind.STRING_LITERAL) {
				return chars2String(chars, pos, length);
			}
			else return String.copyValueOf(chars, pos, length);
		}

		/**
		 * To get the text of a StringLiteral, we need to remove the
		 * enclosing " characters and convert escaped characters to
		 * the represented character.  For example the two characters \ t
		 * in the char array should be converted to a single tab character in
		 * the returned String
		 * 
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		private String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) {// omit initial and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); //for completeness, line termination chars not allowed in String literals
						break;
					case 'n':
						sb.append('\n'); //for completeness, line termination chars not allowed in String literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}

		
		/**
		 * precondition:  This Token is an INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		public String toString() {
			return "[" + kind + "," + String.copyValueOf(chars, pos, length)  + "," + pos + "," + length + "," + line + ","
					+ pos_in_line + "]";
		}

		/** 
		 * Since we overrode equals, we need to override hashCode.
		 * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
		 * 
		 * Both the equals and hashCode method were generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + line;
			result = prime * result + pos;
			result = prime * result + pos_in_line;
			return result;
		}

		/**
		 * Override equals method to return true if other object
		 * is the same class and all fields are equal.
		 * 
		 * Overriding this creates an obligation to override hashCode.
		 * 
		 * Both hashCode and equals were generated by eclipse.
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (line != other.line)
				return false;
			if (pos != other.pos)
				return false;
			if (pos_in_line != other.pos_in_line)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is 
		 * associated with.
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}

	/** 
	 * Extra character added to the end of the input characters to simplify the
	 * Scanner.  
	 */
	static final char EOFchar = 0;
	
	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;
	
	/**
	 * An array of characters representing the input.  These are the characters
	 * from the input string plus and additional EOFchar at the end.
	 */
	final char[] chars;  


	/**
	 * Hash Map for keywords
	 */
	final static HashMap<String,Kind> keyWordMap = new HashMap<String,Kind>();

	public void fillMap(){
		keyWordMap.put("x", Kind.KW_x);
		keyWordMap.put("X", Kind.KW_X);
		keyWordMap.put("y", Kind.KW_y);
		keyWordMap.put("Y", Kind.KW_Y);
		keyWordMap.put("r", Kind.KW_r);
		keyWordMap.put("R", Kind.KW_R);
		keyWordMap.put("a", Kind.KW_a);
		keyWordMap.put("A", Kind.KW_A);
		keyWordMap.put("DEF_X", Kind.KW_DEF_X);
		keyWordMap.put("DEF_Y", Kind.KW_DEF_Y);
		keyWordMap.put("Z", Kind.KW_Z);
		keyWordMap.put("SCREEN", Kind.KW_SCREEN);
		keyWordMap.put("cart_x", Kind.KW_cart_x);
		keyWordMap.put("cart_y", Kind.KW_cart_y);
		keyWordMap.put("polar_a", Kind.KW_polar_a);
		keyWordMap.put("polar_r", Kind.KW_polar_r);
		keyWordMap.put("abs", Kind.KW_abs);
		keyWordMap.put("sin", Kind.KW_sin);
		keyWordMap.put("atan", Kind.KW_atan);
		keyWordMap.put("cos", Kind.KW_cos);
		keyWordMap.put("log", Kind.KW_log);
		keyWordMap.put("image", Kind.KW_image);
		keyWordMap.put("int", Kind.KW_int);
		keyWordMap.put("true", Kind.BOOLEAN_LITERAL);
		keyWordMap.put("false", Kind.BOOLEAN_LITERAL);
		keyWordMap.put("boolean", Kind.KW_boolean);
		keyWordMap.put("url", Kind.KW_url);
		keyWordMap.put("file", Kind.KW_file);
	}
	
	/**
	 * Set of escape sequences
	 */
	public final HashSet<Character> escSeq = new HashSet<Character>();
	
	public void fillSet(){
		escSeq.add('b');
		escSeq.add('t');
		escSeq.add('r');
		escSeq.add('n');
		escSeq.add('f');
		escSeq.add('"');
		escSeq.add('\'');
		escSeq.add('\\');
	}
	
	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFchar;
		tokens = new ArrayList<Token>();
		fillMap();
		fillSet();
	}


	/**
	 * Method to scan the input and create a list of Tokens.
	 * 
	 * If an error is encountered during scanning, throw a LexicalException.
	 * 
	 * @return
	 * @throws LexicalException
	 */
	public Scanner scan() throws LexicalException {
		int pos = 0;
		int line = 1;
		int posInLine = 1;
		boolean comment = false;
		StringBuilder br = new StringBuilder();
		int escPos = -1;

		State state = State.START;
		int startPos = 0;
		while (pos < chars.length) {
			char ch = chars[pos];
			switch (state) {
			case START: {
				// pos = skipWhiteSpace(pos);
				ch = chars[pos];//pos < length ? chars.charAt(pos) : -1;
				startPos = pos;
				switch (ch) {
				case '+': {tokens.add(new Token(Kind.OP_PLUS, startPos, 1,line,posInLine));pos++;posInLine++;} break;
				case '*': {state = State.AFTER_MUL;pos++;} break;
				case '>': {state = State.AFTER_GT;pos++;} break;
				case '<': {state = State.AFTER_LT;pos++;} break;
				case '!': {state = State.AFTER_EXCL;pos++;} break;
				case '/': {state = State.AFTER_FSLASH;pos++;} break;
				case '"': {state = State.IN_STRING;pos++;} break;
				case '?': {tokens.add(new Token(Kind.OP_Q, startPos, 1,line,posInLine));pos++;posInLine++;} break;
				case ':': {tokens.add(new Token(Kind.OP_COLON, startPos, 1,line,posInLine));pos++;posInLine++;} break;
				case '&': {tokens.add(new Token(Kind.OP_AND, startPos, 1,line,posInLine));pos++;posInLine++;} break;
				case '|': {tokens.add(new Token(Kind.OP_OR, startPos, 1,line,posInLine));pos++;posInLine++;} break;
				case '-': {state = State.AFTER_MINUS;pos++;} break;
				case '%': {tokens.add(new Token(Kind.OP_MOD, startPos, 1,line,posInLine));pos++;posInLine++;} break;
				case '@': {tokens.add(new Token(Kind.OP_AT, startPos, 1,line,posInLine));pos++;posInLine++;} break;
				case '=': {state = State.AFTER_EQ;pos++;}break;
				case '0': {tokens.add(new Token(Kind.INTEGER_LITERAL,startPos, 1,line,posInLine));pos++;posInLine++;}break;
				case ';': {tokens.add(new Token(Kind.SEMI,startPos, 1,line,posInLine));pos++;posInLine++;}break;
				case ',': {tokens.add(new Token(Kind.COMMA,startPos, 1,line,posInLine));pos++;posInLine++;}break;
				case '(': {tokens.add(new Token(Kind.LPAREN,startPos, 1,line,posInLine));pos++;posInLine++;}break;
				case ')': {tokens.add(new Token(Kind.RPAREN,startPos, 1,line,posInLine));pos++;posInLine++;}break;
				case '[': {tokens.add(new Token(Kind.LSQUARE,startPos, 1,line,posInLine));pos++;posInLine++;}break;
				case ']': {tokens.add(new Token(Kind.RSQUARE,startPos, 1,line,posInLine));pos++;posInLine++;}break;
				case '\n': {if(!(pos>0 && chars[pos-1]=='\r')){posInLine = 1; line++;}pos++;}break;
				case '\r': {posInLine = 1; line++;pos++;}break;
				case EOFchar : { pos++;}break; // next iteration should terminate loop}
				default: {
					if (Character.isDigit(ch)) {state = State.IN_DIGIT;} 
					else if (Character.isJavaIdentifierStart(ch)) {
						state = State.IN_IDENT;//pos++;
					} 
					else if (Character.isWhitespace(ch)){
						pos++;
						posInLine++;
					}
					else { error(pos);  }
				}
				} // switch (ch)
			}  break;
			case IN_DIGIT: {
				if(Character.isDigit(ch)){
					pos++;
					br.append(ch);
				}else{
					try{
					String check = br.toString();
					Integer.parseInt(check);
					}catch(NumberFormatException e){
						throw new LexicalException("Invalid Number", startPos);
					}
					tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, pos - startPos,line,posInLine));
					posInLine += pos-startPos;
					state = State.START;
					br = new StringBuilder();
				}
			}  break;
			case IN_IDENT: {
				if (Character.isJavaIdentifierPart(ch) && ch!=EOFchar) {
					pos++;
				} else {
					String ident = new String(chars,startPos,pos-startPos);
					if(keyWordMap.containsKey(ident)){
						tokens.add(new Token(keyWordMap.get(ident), startPos, pos - startPos,line,posInLine));
					}else{
						tokens.add(new Token(Kind.IDENTIFIER, startPos, pos - startPos,line,posInLine));
					}
					posInLine += pos-startPos;
					state = State.START;
				}
			}  break;
			case AFTER_EQ: {
				if(ch == '='){
					tokens.add(new Token(Kind.OP_EQ, startPos, 2,line,posInLine));
					pos++;
					posInLine+= pos - startPos;
				}else{
					tokens.add(new Token(Kind.OP_ASSIGN, startPos, 1,line,posInLine));
					posInLine++;
				}
				state = State.START;
			}  break;
			case AFTER_MUL: {
				if(ch == '*'){
					tokens.add(new Token(Kind.OP_POWER, startPos, 2,line,posInLine));
					pos++;
					posInLine+= pos - startPos;
				}else{
					tokens.add(new Token(Kind.OP_TIMES, startPos, 1,line,posInLine));
					posInLine++;
				}
				state = State.START;
			}  break;
			case AFTER_GT: {
				if(ch == '='){
					tokens.add(new Token(Kind.OP_GE, startPos, 2,line,posInLine));
					pos++;
					posInLine+= pos - startPos;
				}else{
					tokens.add(new Token(Kind.OP_GT, startPos, 1,line,posInLine));
					posInLine++;
				}
				state = State.START;
			}  break;
			case AFTER_LT: {
				if(ch == '='){
					tokens.add(new Token(Kind.OP_LE, startPos, 2,line,posInLine));
					pos++;
					posInLine+= pos - startPos;
				}else if(ch == '-'){
					tokens.add(new Token(Kind.OP_LARROW, startPos, 2,line,posInLine));
					pos++;
					posInLine+= pos - startPos;
				}else{
					tokens.add(new Token(Kind.OP_LT, startPos, 1,line,posInLine));
					posInLine++;
				}
				state = State.START;
			}  break;
			case AFTER_MINUS: {
				if(ch == '>'){
					tokens.add(new Token(Kind.OP_RARROW, startPos, 2,line,posInLine));
					pos++;
					posInLine+= pos - startPos;
				}else{
					tokens.add(new Token(Kind.OP_MINUS, startPos, 1,line,posInLine));
					posInLine++;
				}
				state = State.START;
			}  break;
			case AFTER_EXCL: {
				if(ch == '='){
					tokens.add(new Token(Kind.OP_NEQ, startPos, 2,line,posInLine));
					pos++;
					posInLine+= pos - startPos;
				}else{
					tokens.add(new Token(Kind.OP_EXCL, startPos, 1,line,posInLine));
					posInLine++;
				}
				state = State.START;
			}  break;
			case AFTER_FSLASH: {
				if(ch == '/'){
					comment=true;
					pos++;
					posInLine++;
				}else if(comment && (ch=='\n' || ch == '\r' || ch==EOFchar)){
					comment = false;
					posInLine++;
					state = State.START;
				}else{
					if(comment){
						pos++;
					}else{
						tokens.add(new Token(Kind.OP_DIV, startPos, 1,line,posInLine));
						state = State.START;
					}
					posInLine++;
				}
			}  break;
			case IN_STRING: {
				if(ch==EOFchar || ch=='\n' || ch=='\r'){
					error(pos);
					break;
				}
				if(ch=='"' && ((pos>0&&chars[pos-1]!='\\') || (pos-1)==escPos)){
					tokens.add(new Token(Kind.STRING_LITERAL, startPos, pos-startPos+1,line,posInLine));
					pos++;
					posInLine+=pos-startPos;
					state = State.START;
				}else{
					if(pos>0 && chars[pos-1]=='\\' && (pos-1)!=escPos){
						if(!escSeq.contains(ch))
							error(pos);
						else{
							escPos = pos;
						}
					}
					pos++;
				}
			}  break;
			default:  {
				error(pos);
			};
			}// switch(state)
		} // while
		pos--;
		tokens.add(new Token(Kind.EOF, pos, 0,line,posInLine));
		return this;

	}

	/**
	 * General method to throw error
	 * @param pos
	 * @throws LexicalException
	 */
	private void error(int pos) throws LexicalException {
		throw new LexicalException("Invalid Lexicon", pos);		
	}


	/**
	 * Returns true if the internal interator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	
	/**
	 * Returns the next Token, but does not update the internal iterator.
	 * This means that the next call to nextToken or peek will return the
	 * same Token as returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}
	
	
	/**
	 * Resets the internal iterator so that the next call to peek or nextToken
	 * will return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		return sb.toString();
	}

}
