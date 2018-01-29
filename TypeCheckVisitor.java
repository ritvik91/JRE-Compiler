package cop5556fa17;


import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;

import java.net.URL;


import cop5556fa17.TypeUtils;

import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
	
	SymbolTable symbolTable = new SymbolTable(); 

		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}		
		

	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {

		if(symbolTable.lookupType(declaration_Variable.name)==null){
			declaration_Variable.Type = TypeUtils.getType(declaration_Variable.firstToken);
			if(declaration_Variable.e!=null){
				declaration_Variable.e.visit(this, null);
				if(declaration_Variable.Type != declaration_Variable.e.type){
					throw new SemanticException(declaration_Variable.firstToken, "error in decl var. Expr type not matching");
				}
			}
			symbolTable.insert(declaration_Variable.name, declaration_Variable);
		}else{
			throw new SemanticException(declaration_Variable.firstToken, "error in decl var");
		}
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {

		expression_Binary.e0.visit(this, null);
		expression_Binary.e1.visit(this, null);

		if(expression_Binary.e0.type==expression_Binary.e1.type){
			Type t = null;
			if(expression_Binary.op==Kind.OP_EQ || expression_Binary.op==Kind.OP_NEQ)
				t = Type.BOOLEAN;
			else if((expression_Binary.op==Kind.OP_GE || expression_Binary.op==Kind.OP_GT
					|| expression_Binary.op==Kind.OP_LE || expression_Binary.op==Kind.OP_LT)
					&& expression_Binary.e0.type == Type.INTEGER){
				t = Type.BOOLEAN;
			}else if((expression_Binary.op==Kind.OP_OR || expression_Binary.op==Kind.OP_AND)
					&&(expression_Binary.e0.type==Type.INTEGER || expression_Binary.e0.type==Type.BOOLEAN)){
				t = expression_Binary.e0.type;
			}else if((expression_Binary.op == Kind.OP_DIV || expression_Binary.op == Kind.OP_MINUS || expression_Binary.op == Kind.OP_MOD
					|| expression_Binary.op == Kind.OP_PLUS || expression_Binary.op == Kind.OP_POWER
					|| expression_Binary.op == Kind.OP_TIMES)&& expression_Binary.e0.type==Type.INTEGER){
				t = Type.INTEGER;
			}
			
			expression_Binary.type = t;
			if(expression_Binary.type==null){
				throw new SemanticException(expression_Binary.firstToken, "expr binary is null");
			}
		}else{
			throw new SemanticException(expression_Binary.firstToken, "error in expr binary");
		}
		
		return this;
		
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		Type t = null;
		expression_Unary.e.visit(this, null);
		Type expType = expression_Unary.e.type;
		if(expression_Unary.op==Kind.OP_EXCL &&(expType==Type.BOOLEAN || expType==Type.INTEGER)){
			t = expType;
		}else if((expression_Unary.op == Kind.OP_PLUS || expression_Unary.op == Kind.OP_MINUS)&&expType==Type.INTEGER){
			t = Type.INTEGER;
		}
		
		expression_Unary.type = t;
		if(expression_Unary.type==null){
			throw new SemanticException(expression_Unary.firstToken, "error in exp unary");
		}
//		throw new UnsupportedOperationException();
		return this;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		
		index.e0.visit(this, null);
		index.e1.visit(this, null);
		if(index.e0.type==Type.INTEGER && index.e1.type==Type.INTEGER){
			index.setCartesian(!(index.e0.firstToken.kind==Kind.KW_r && index.e1.firstToken.kind==Kind.KW_a));
		}else{
			throw new SemanticException(index.firstToken, "error in index");
		}
//		throw new UnsupportedOperationException();
		return this;
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {

		Declaration dec = (Declaration) symbolTable.lookupType(expression_PixelSelector.name);
		expression_PixelSelector.index.visit(this, null);
		Type t = null;
		if(dec.Type==Type.IMAGE){
			t = Type.INTEGER;
		}else if(expression_PixelSelector.index==null){
			t = dec.Type;
		}
		expression_PixelSelector.type = t;
		
		if(expression_PixelSelector.type==null){
			throw new SemanticException(expression_PixelSelector.firstToken, "error in Exp Pixel Selc");
		}
		//		throw new UnsupportedOperationException();
		return this;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {

		expression_Conditional.condition.visit(this, null);
		expression_Conditional.trueExpression.visit(this, null);
		expression_Conditional.falseExpression.visit(this, null);
		if(expression_Conditional.condition.type == Type.BOOLEAN && expression_Conditional.trueExpression.type==expression_Conditional.falseExpression.type){
			expression_Conditional.type = expression_Conditional.trueExpression.type;
		}else{
			throw new SemanticException(expression_Conditional.firstToken, "error in expr conditional");
		}
		
	//	throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {

		if(symbolTable.lookupType(declaration_Image.name)==null){
			symbolTable.insert(declaration_Image.name, declaration_Image);
			declaration_Image.Type = Type.IMAGE;
			
			if(declaration_Image.xSize!=null){
				if(declaration_Image.ySize!=null){
					declaration_Image.xSize.visit(this,null);
					declaration_Image.ySize.visit(this,null);
					if(!(declaration_Image.xSize.type==Type.INTEGER && declaration_Image.ySize.type == Type.INTEGER)){
						throw new SemanticException(declaration_Image.firstToken, "Semantic error in Declaration Image");
				}
				}else{
					throw new SemanticException(declaration_Image.firstToken, "Semantic error in Declaration Image");
				}
			}
			if(declaration_Image.source!=null)
			declaration_Image.source.visit(this, null);
			
		}else{
			throw new SemanticException(declaration_Image.firstToken, "Semantic error in Declaration Image");
		}
		
	//	throw new UnsupportedOperationException();
		return this;
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {

		Boolean isValidURL = true;
		try{
			URL url = new URL(source_StringLiteral.fileOrUrl);
		}catch(Exception e){
			isValidURL = false;
		}
		source_StringLiteral.type = isValidURL?Type.URL:Type.FILE;
	return this;
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {

		source_CommandLineParam.paramNum.visit(this, null);
		source_CommandLineParam.type = null;
		if(source_CommandLineParam.paramNum.type != Type.INTEGER){
			throw new SemanticException(source_CommandLineParam.firstToken, "error in Source_CommandLineParam");
		}
		
		return this;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {

		Declaration dec = (Declaration) symbolTable.lookupType(source_Ident.name);
		if(dec!=null)
			source_Ident.type = dec.Type;
		
		if(!(source_Ident.type==Type.FILE || source_Ident.type== Type.URL)){
			throw new SemanticException(source_Ident.firstToken, "error in source ident");
		}
		
		return this;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {

		if(symbolTable.lookupType(declaration_SourceSink.name)==null){
			symbolTable.insert(declaration_SourceSink.name, declaration_SourceSink);
			declaration_SourceSink.Type =  TypeUtils.getType(declaration_SourceSink.firstToken);
			declaration_SourceSink.source.visit(this, null);
			if(declaration_SourceSink.source.type != declaration_SourceSink.Type && declaration_SourceSink.source.type!=null){
				throw new SemanticException(declaration_SourceSink.firstToken, "error in dec_SourceSink");
			}
		}else{
			throw new SemanticException(declaration_SourceSink.firstToken, "error in dec_SourceSink");
		}
		
	//	throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {

		expression_IntLit.type = Type.INTEGER;
	//	throw new UnsupportedOperationException();
		return this;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {

		expression_FunctionAppWithExprArg.arg.visit(this, null);
		if(expression_FunctionAppWithExprArg.arg.type == Type.INTEGER){
			expression_FunctionAppWithExprArg.type = Type.INTEGER;
		}else{
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, "error in Expression_FunctionAppWithExprArg");
		}
	//	throw new UnsupportedOperationException();
		return this;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {

		expression_FunctionAppWithIndexArg.type = Type.INTEGER;
		expression_FunctionAppWithIndexArg.arg.visit(this, null);
	//	throw new UnsupportedOperationException();
		return this;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {

		expression_PredefinedName.type = Type.INTEGER;
	//	throw new UnsupportedOperationException();
		return this;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {

		Declaration dec = (Declaration)symbolTable.lookupType(statement_Out.name);
		if(dec==null)
			throw new SemanticException(statement_Out.firstToken, "error in Statement out");
		statement_Out.setDec(dec);
		statement_Out.sink.visit(this, null);
		statement_Out.type = dec.Type;//TODO
		
		if(!(((dec.Type==Type.INTEGER || dec.Type ==Type.BOOLEAN)&&statement_Out.sink.type==Type.SCREEN)
				||(dec.Type==Type.IMAGE&&(statement_Out.sink.type==Type.FILE || statement_Out.sink.type==Type.SCREEN)))){
			throw new SemanticException(statement_Out.firstToken, "error in Statement out");
		}
	//	throw new UnsupportedOperationException();
		return this;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {

		Declaration dec = (Declaration)symbolTable.lookupType(statement_In.name);
		statement_In.setDec(dec);
		statement_In.source.visit(this, null);
		if(dec==null)
			throw new SemanticException(statement_In.firstToken, "error in Statement in");
		statement_In.type = dec.Type;
		/*if(!(dec!=null && dec.Type==statement_In.source.type)){
			throw new SemanticException(statement_In.firstToken, "error in statement in");
		}*/
	//	throw new UnsupportedOperationException();
		return this;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		
		statement_Assign.lhs.visit(this, null);
		statement_Assign.e.visit(this, null);

		if(statement_Assign.lhs.type == statement_Assign.e.type || (statement_Assign.lhs.type==Type.IMAGE && statement_Assign.e.type==Type.INTEGER)){
			statement_Assign.setCartesian(statement_Assign.lhs.isCartesian());
		}else{
			throw new SemanticException(statement_Assign.firstToken, "error in Statement Assisgn");
		}
		
	//	throw new UnsupportedOperationException();
		return this;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
	
		if(symbolTable.lookupType(lhs.name)==null){
			throw new SemanticException(lhs.firstToken, "error in lhs");
		}else{
			lhs.declaration = (Declaration) symbolTable.lookupType(lhs.name);
			lhs.type = lhs.declaration.Type;
			if(lhs.index!=null){
				lhs.index.visit(this, null);
				lhs.setCartesian(lhs.index.isCartesian());
			}
		}
		//	throw new UnsupportedOperationException();
		return this;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {

		sink_SCREEN.type = Type.SCREEN;
		//		throw new UnsupportedOperationException();
		return this;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		
		Declaration dec = (Declaration) symbolTable.lookupType(sink_Ident.name);
		sink_Ident.type = dec.Type;
		if(sink_Ident.type!=Type.FILE){
			throw new SemanticException(sink_Ident.firstToken, "error in sink ident");
		}
		//		throw new UnsupportedOperationException();
		return this;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {

		expression_BooleanLit.type = Type.BOOLEAN;
		
	//	throw new UnsupportedOperationException();
		return this;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {

		Declaration dec = (Declaration)symbolTable.lookupType(expression_Ident.name);
		if(dec!=null)
		expression_Ident.type = dec.Type;
	//	throw new UnsupportedOperationException();
		return this;
	}

}
