package cop5556fa17;



import java.util.ArrayList;
import java.util.Arrays;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.Parser.SyntaxException;
import cop5556fa17.AST.*;

import static cop5556fa17.Scanner.Kind.*;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	public void match(Kind k) throws SyntaxException {

		if (t.kind == k) {
			t = scanner.nextToken();
		} else {
			System.out.println("flag");
			throw new SyntaxException(t, "unmatched error");
		}
	}

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException {

		Token firstToken = t;
		ArrayList<ASTNode> decsAndStatements = new ArrayList<>();
		match(Kind.IDENTIFIER);
		while (t.kind == Kind.KW_int || t.kind == Kind.KW_boolean || t.kind == Kind.KW_image || t.kind == Kind.KW_url
				|| t.kind == Kind.KW_file || t.kind == Kind.IDENTIFIER) {
			if (t.kind == Kind.KW_int || t.kind == Kind.KW_boolean || t.kind == Kind.KW_image || t.kind == Kind.KW_url
					|| t.kind == Kind.KW_file) {
				Declaration dec = Declaration();
				decsAndStatements.add(dec);
				match(Kind.SEMI);
			}

			else if (t.kind == Kind.IDENTIFIER) {
				Statement st = Statement();
				decsAndStatements.add(st);
				match(Kind.SEMI);
			}
		}
		Program p = new Program(firstToken, firstToken, decsAndStatements);
		return p;
	}
	
	Declaration Declaration() throws SyntaxException {

		Declaration dec = null;
		if (t.kind == Kind.KW_int) {
			dec = VariableDeclartion();
		}
		else if (t.kind == Kind.KW_boolean) {
			dec = VariableDeclartion();
		}

		else if (t.kind == Kind.KW_image) {
			dec = ImageDeclaration();
		}

		else if (t.kind == Kind.KW_url) {
			dec = SourceSinkDeclaration();

		} else if (t.kind == Kind.KW_file) {
			dec = SourceSinkDeclaration();
		} else
			throw new SyntaxException(t, "Declartion error");
		
		return dec;
	}


	Declaration_Variable VariableDeclartion() throws SyntaxException {
		Token firstToken = t;
		Token type = varType();
		Expression e = null;
		Token name = t;
		match(Kind.IDENTIFIER);
		if (t.kind == Kind.OP_ASSIGN) {
			match(Kind.OP_ASSIGN);
			e = expression();
		}
		Declaration_Variable decVar = new Declaration_Variable(firstToken, type, name, e);
		return decVar;
	}

	Token varType() throws SyntaxException{
		Token type = null;
		if(t.kind == Kind.KW_int){
			type = t;
			match(Kind.KW_int);
		}
		else if(t.kind == Kind.KW_boolean){
			type = t;
			match(Kind.KW_boolean);
		}
		else
			throw new SyntaxException(t, "varType error");
		return type;
	}

	Declaration_SourceSink SourceSinkDeclaration () throws SyntaxException{
		Token firstToken = t;
		Token type = SourceSinkType();
		Token name = t;
		match(Kind.IDENTIFIER);
		match(Kind.OP_ASSIGN);
		Source sc = Source();
		
		Declaration_SourceSink ds = new Declaration_SourceSink(firstToken, type, name, sc);
		return ds;
	}

	Source Source() throws SyntaxException {

		Source sc = null;
		Token firstToken = t;
		
		if (t.kind == Kind.STRING_LITERAL) {
			sc = new Source_StringLiteral(firstToken, t.getText());
			match(Kind.STRING_LITERAL);
		} else if (t.kind == Kind.OP_AT) {
			match(Kind.OP_AT);
			Expression e0 = expression();
			sc = new Source_CommandLineParam(firstToken, e0);
		}

		else if (t.kind == Kind.IDENTIFIER) {
			sc = new Source_Ident(firstToken, t);
			match(Kind.IDENTIFIER);
		}
		else
			throw new SyntaxException(t, " error at source");
		
		return sc;

	}


	Token SourceSinkType() throws SyntaxException {
		Token type = null;
		if (t.kind == Kind.KW_url) {
			type = t;
			match(Kind.KW_url);
		} else if (t.kind == Kind.KW_file) {
			type = t;
			match(Kind.KW_file);
		}

		else
			throw new SyntaxException(t, " error at source");
		
		return type;
	}
	
	Declaration_Image ImageDeclaration() throws SyntaxException{
		Token first = t;
		Declaration_Image di = null;
		Source sc = null;
		Expression e0 = null;
		Expression e1 = null;
		
		match(Kind.KW_image);
		
		if(t.kind == Kind.LSQUARE){
			match(Kind.LSQUARE);
			e0 = expression();
			match(Kind.COMMA);
			e1 = expression();
			match(Kind.RSQUARE);
		}
		Token name = t;
		match(Kind.IDENTIFIER);
		if(t.kind==Kind.OP_LARROW){
			match(Kind.OP_LARROW);
			sc = Source();
		}
		di = new Declaration_Image(first, e0, e1, name, sc);
		return di;
	}
	
	Statement Statement() throws SyntaxException{
		Token firstToken = t;
		Statement st = null;
		if(t.kind==Kind.IDENTIFIER){
			try{
				st = AssignmentStatement();
			}catch(SyntaxException e){
				try{
				st = ImageOutStatement(firstToken);
				}catch(SyntaxException e2){
				st = ImageInStatement(firstToken);
				}
			}
			
		}else{
			throw new SyntaxException(t, "Error in Statement");
		}
		
		return st;
	}

	Statement_Out ImageOutStatement(Token firstToken) throws SyntaxException{
		Statement_Out s = null;
		//match(Kind.IDENTIFIER);
		match(Kind.OP_RARROW);
		Sink sink = Sink();
		s = new Statement_Out(firstToken, firstToken, sink);
		return s;
	}
	
	Sink Sink() throws SyntaxException {
		Sink s = null;
		Token firstToken = t;
		if (t.kind == Kind.IDENTIFIER){
			s = new Sink_Ident(firstToken, firstToken);
			match(Kind.IDENTIFIER);
		}
		else if (t.kind == Kind.KW_SCREEN){
			s = new Sink_SCREEN(firstToken);
			match(Kind.KW_SCREEN);
		}
		else
			throw new SyntaxException(t, "Sink exception");
		
		return s;
	}

	Statement_In ImageInStatement(Token firstToken) throws SyntaxException{
		Statement_In st = null;
		//match(Kind.IDENTIFIER);
		match(Kind.OP_LARROW);
		Source src = Source();
		st = new Statement_In(firstToken, firstToken, src);
		return st;
		
	}
	Statement_Assign AssignmentStatement() throws SyntaxException{
		Token firstToken = t;
		Statement_Assign st = null;
		LHS lhs = Lhs();
		if(t.kind==Kind.OP_ASSIGN)
			match(Kind.OP_ASSIGN);
		else
			throw new SyntaxException(t, "error in AssignmentStatement ");
		Expression e = expression();
		
		st = new Statement_Assign(firstToken, lhs, e);
		return st;
	}

	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	Expression expression() throws SyntaxException {
		Token firstToken = t;
		Expression e = null;
		
		e = OrExpression();
		if (t.kind == Kind.OP_Q) {
			match(Kind.OP_Q);
			Expression e1 = expression();
			match(Kind.OP_COLON);
			Expression e2 = expression();
			e = new Expression_Conditional(firstToken, e, e1, e2);
		}
		
		return e;
	}
	
	
	
	public Expression OrExpression() throws SyntaxException {

		Expression e1 = null;
		Token firstToken = t;
		Token op = null;
		
		Expression e0 = AndExpression();
		while (t.kind == Kind.OP_OR) {
			if (t.kind == Kind.OP_OR) {
				op = t;
				match(Kind.OP_OR);
			}else{
				throw new SyntaxException(t, "Error in OrExpr");
			}
			e1 = AndExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}

	
	public Expression AndExpression() throws SyntaxException {

		Expression e1 = null;
		Token firstToken = t;
		Token op = null;
		
		Expression e0 = EqExpression();
		while (t.kind == Kind.OP_AND) {
			if (t.kind == Kind.OP_AND) {
				op = t;
				match(Kind.OP_AND);
			}else{
				throw new SyntaxException(t, "Error in And Expression");
			}
			e1 = EqExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		
		return e0;
	}
	
	public Expression EqExpression() throws SyntaxException {

		Expression e1 = null;
		Token firstToken = t;
		Token op = null;
		
		Expression e0 = RelExpression();
		while (t.kind == Kind.OP_EQ || t.kind == Kind.OP_NEQ) {
			if (t.kind == Kind.OP_EQ) {
				op = t;
				match(Kind.OP_EQ);
			} else if (t.kind == Kind.OP_NEQ) {
				op = t;
				match(Kind.OP_NEQ);
			}else{
				throw new SyntaxException(t, "Error in EqExpression");
			}
			e1 = RelExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		
		return e0;
	}
	
	public Expression RelExpression() throws SyntaxException {

		Expression e1 = null;
		Token firstToken = t;
		Token op = null;
		Expression e0 = AddExpression();
		while (t.kind == Kind.OP_LT || t.kind == Kind.OP_GT || t.kind == Kind.OP_LE || t.kind == Kind.OP_GE) {

			if (t.kind == Kind.OP_LT) {
				op = t;
				match(Kind.OP_LT);
			} else if (t.kind == Kind.OP_GT) {
				op = t;
				match(Kind.OP_GT);
			} else if (t.kind == Kind.OP_LE) {
				op = t;
				match(Kind.OP_LE);
			} else if (t.kind == Kind.OP_GE) {
				op = t;
				match(Kind.OP_GE);
			}else{
				throw new SyntaxException(t, "Error in RelExpr");
			}
			e1 = AddExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	public Expression AddExpression() throws SyntaxException {

		Token firstToken = t;
		Token op = null;
		Expression e0 = MultExpression();
		Expression e1 = null;
		
		while (t.kind == Kind.OP_PLUS || t.kind == Kind.OP_MINUS) {
			if (t.kind == Kind.OP_PLUS) {
				op = t;
				match(Kind.OP_PLUS);
			} else if (t.kind == Kind.OP_MINUS) {
				op = t;
				match(Kind.OP_MINUS);
			}else{
				throw new SyntaxException(t, "Error in AddExpr");
			}
			e1 = MultExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}

		return e0;
	}
	
	public Expression MultExpression() throws SyntaxException {
		
		Token firstToken = t;
		Token op = null;

		Expression e0 = UnaryExpression();
		Expression e1 = null;
		while (t.kind == Kind.OP_TIMES || t.kind == Kind.OP_DIV || t.kind == Kind.OP_MOD) {
			if (t.kind == Kind.OP_TIMES) {
				op = t;
				match(Kind.OP_TIMES);
			} else if (t.kind == Kind.OP_DIV) {
				op = t;
				match(Kind.OP_DIV);
			} else if (t.kind == Kind.OP_MOD) {
				op = t;
				match(Kind.OP_MOD);
			}else{
				throw new SyntaxException(t, "Error in MultExpr");
			}
			e1 = UnaryExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		
		return e0;
	}
	
	Expression UnaryExpression() throws SyntaxException {
		Expression ex = null;
		Token firstToken = t;
		if (t.kind == Kind.OP_PLUS) {
			Token op = t;
			match(Kind.OP_PLUS);
			Expression e = UnaryExpression();
			ex = new Expression_Unary(firstToken, op, e);
		}
		else if (t.kind == Kind.OP_MINUS) {
			Token op = t;
			match(Kind.OP_MINUS);
			Expression e = UnaryExpression();
			ex = new Expression_Unary(firstToken, op, e);
		}
		else
		{
			try{
			ex = UnaryExpressionNotPlusMinus();
			}catch(SyntaxException e){
				throw new SyntaxException(t, "ERROR In Unary Expression");
			}
		}
		return ex;
	}
	
	public Expression UnaryExpressionNotPlusMinus() throws SyntaxException {

		Expression e = null;
		Token firstToken = t;
		if (t.kind == Kind.KW_X) {
			e = new Expression_PredefinedName(firstToken, t.kind);
			match(Kind.KW_X);
		} else if (t.kind == Kind.KW_x) {
			e = new Expression_PredefinedName(firstToken, t.kind);
			match(Kind.KW_x);
		} else if (t.kind == Kind.KW_y) {
			e = new Expression_PredefinedName(firstToken, t.kind);
			match(Kind.KW_y);
		} else if (t.kind == Kind.KW_Y) {
			e = new Expression_PredefinedName(firstToken, t.kind);
			match(Kind.KW_Y);
		} else if (t.kind == Kind.KW_Z) {
			e = new Expression_PredefinedName(firstToken, t.kind);
			match(Kind.KW_Z);
		} else if (t.kind == Kind.KW_R) {
			e = new Expression_PredefinedName(firstToken, t.kind);
			match(Kind.KW_R);
		} else if (t.kind == Kind.KW_r) {
			e = new Expression_PredefinedName(firstToken, t.kind);
			match(Kind.KW_r);
		} else if (t.kind == Kind.KW_a) {
			e = new Expression_PredefinedName(firstToken, t.kind);
			match(Kind.KW_a);
		}
		else if (t.kind == Kind.KW_A) {
			e = new Expression_PredefinedName(firstToken, t.kind);
			match(Kind.KW_A);
		} else if (t.kind == Kind.KW_DEF_X) {
			e = new Expression_PredefinedName(firstToken, t.kind);
			match(Kind.KW_DEF_X);
		} else if (t.kind == Kind.KW_DEF_Y) {
			e = new Expression_PredefinedName(firstToken, t.kind);
			match(Kind.KW_DEF_Y);
		}
		else if (t.kind == Kind.IDENTIFIER) {
			e = IdentOrPixelSelectorExpression();
		}
		else if (t.kind == Kind.INTEGER_LITERAL || t.kind == Kind.LPAREN || t.kind == Kind.KW_sin
				|| t.kind == Kind.KW_cos || t.kind == Kind.KW_atan || t.kind == Kind.KW_abs || t.kind == Kind.KW_cart_x
				|| t.kind == Kind.KW_cart_y || t.kind == Kind.KW_polar_a || t.kind == Kind.KW_polar_r
				|| t.kind == Kind.BOOLEAN_LITERAL) {
			e = Primary();
		} else if (t.kind == Kind.OP_EXCL) {
			Token op = t;
			match(Kind.OP_EXCL);
			Expression eu = UnaryExpression();
			e = new Expression_Unary(firstToken, op,eu);
		} else
			throw new SyntaxException(t, " error in UnaryExpressionNotPlusMinus");
		
		return e;
	}

	Expression Primary() throws SyntaxException {
		Token firstToken = t;
		Expression e = null;
		if (t.kind == Kind.INTEGER_LITERAL) {
			e = new Expression_IntLit(firstToken, t.intVal());
			match(Kind.INTEGER_LITERAL);
		}
		else if (t.kind == Kind.LPAREN) {
			match(Kind.LPAREN);
			e = expression();
			//e.firstToken = firstToken;
			match(Kind.RPAREN);
		} else if (t.kind == Kind.BOOLEAN_LITERAL) {
			e = new Expression_BooleanLit(firstToken, Boolean.valueOf(t.getText()));
			match(Kind.BOOLEAN_LITERAL);
		}
		else {
			try{
				e = FunctionApplication();
			}catch(SyntaxException err){
				throw new SyntaxException(t, "Primary Error");
			}
		}
		return e;
	}

	Expression IdentOrPixelSelectorExpression() throws SyntaxException{
		Expression e = null;
		Token firstToken = t;
		Token name = t;
		Index i = null;
		match(Kind.IDENTIFIER);
		if(t.kind == Kind.LSQUARE){
			match(Kind.LSQUARE);
			i = selector();
			match(Kind.RSQUARE);
			e = new Expression_PixelSelector(firstToken, name, i);
		}else{
			e = new Expression_Ident(firstToken, firstToken);
		}
		return e;
	}

	LHS Lhs() throws SyntaxException {

		Token firstToken = t;
		Token name = t;
		Index i = null;
		match(Kind.IDENTIFIER);
		if (t.kind == Kind.LSQUARE) {

			match(Kind.LSQUARE);
			i = LhsSelector();
			match(Kind.RSQUARE);
		}
		LHS lhs = new LHS(firstToken, name, i);
		return lhs;
	}

	Expression_FunctionApp FunctionApplication() throws SyntaxException {
		
		Expression_FunctionApp Ex = null; 
		Token firstToken = t;
		Kind k = FunctionName();
		Index i=null;

		if (t.kind == Kind.LPAREN) {
			match(Kind.LPAREN);
			Expression e = expression();
			match(Kind.RPAREN);
			Ex = new Expression_FunctionAppWithExprArg(firstToken, k, e);

		} else if (t.kind == Kind.LSQUARE) {

			match(Kind.LSQUARE);
			i = selector();
			match(Kind.RSQUARE);
			Ex = new Expression_FunctionAppWithIndexArg(firstToken, k, i);
		}
		else {
			throw new SyntaxException(t, "error in Function Application");
		}

		return Ex;
	}

	Kind FunctionName() throws SyntaxException {
		Kind k = t.kind;
		if (t.kind == Kind.KW_sin)
			match(Kind.KW_sin);
		else if (t.kind == Kind.KW_cos)
			match(Kind.KW_cos);
		else if (t.kind == Kind.KW_atan)
			match(Kind.KW_atan);
		else if (t.kind == Kind.KW_abs)
			match(Kind.KW_abs);
		else if (t.kind == Kind.KW_cart_x)
			match(Kind.KW_cart_x);
		else if (t.kind == Kind.KW_cart_y)
			match(Kind.KW_cart_y);
		else if (t.kind == Kind.KW_polar_a)
			match(Kind.KW_polar_a);
		else if (t.kind == Kind.KW_polar_r)
			match(Kind.KW_polar_r);
		else
			throw new SyntaxException(t, "error in function name");
		
		return k;
	}

	Index LhsSelector() throws SyntaxException{
		match(Kind.LSQUARE);
		Index i = null;
		if (t.kind == Kind.KW_x) {
			i = xySelector();

		} else if (t.kind == Kind.KW_r) {
			i = RaSelector();

		} else
			throw new SyntaxException(t, "Lhs selector error");
		match(Kind.RSQUARE);
		
		return i;
	}


	Index RaSelector() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = new Expression_PredefinedName(t, t.kind);
		match(Kind.KW_r);
		match(Kind.COMMA);
		Expression e1 = new Expression_PredefinedName(t, t.kind);
		match(Kind.KW_a);
		
		Index i = new Index(firstToken, e0, e1);
		return i;
	}

	Index xySelector() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = new Expression_PredefinedName(t, t.kind);
		match(Kind.KW_x);
		match(Kind.COMMA);
		Expression e1 = new Expression_PredefinedName(t, t.kind);
		match(Kind.KW_y);
		
		Index i = new Index(firstToken, e0, e1);
		return i;

	}

	Index selector() throws SyntaxException {
		Token first = t;
		Expression e0 = expression();
		match(Kind.COMMA);
		Expression e1 = expression();
		
		Index i = new Index(first, e0, e1);
		return i;
		
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}
