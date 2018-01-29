package cop5556fa17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.TypeUtils.Type;
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
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);		
		// if GRADE, generates code to add string to log
	//	CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
	//	CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		cw.visitField(ACC_STATIC, "x", "I", null, null);
		cw.visitField(ACC_STATIC, "y", "I", null, null);
		cw.visitField(ACC_STATIC, "r", "I", null, null);
		cw.visitField(ACC_STATIC, "a", "I", null, null);
		cw.visitField(ACC_STATIC, "X", "I", null, null);
		cw.visitField(ACC_STATIC, "Y", "I", null, null);
		cw.visitField(ACC_STATIC, "R", "I", null, null);
		cw.visitField(ACC_STATIC, "A", "I", null, null);//TODO
		cw.visitField(ACC_STATIC, "DEF_X", "I", null, new Integer(256));
		cw.visitField(ACC_STATIC, "DEF_Y", "I", null, new Integer(256));
		cw.visitField(ACC_STATIC, "Z", "I", null, new Integer(16777215));
		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);
		
		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		
		Type decType = declaration_Variable.Type;
		
		if(decType==Type.INTEGER){
			cw.visitField(ACC_STATIC, declaration_Variable.name, getVisitType(declaration_Variable.Type), null, null);
		}else if(decType==Type.BOOLEAN){
			cw.visitField(ACC_STATIC, declaration_Variable.name, getVisitType(declaration_Variable.Type), null, null);
		}
		if(declaration_Variable.e!=null){
			declaration_Variable.e.visit(this, null);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, getVisitType(declaration_Variable.Type));
		}
		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {

		expression_Binary.e0.visit(this, null);
		expression_Binary.e1.visit(this, null);
		
		Kind op = expression_Binary.op;
		switch(op){
		case OP_PLUS: mv.visitInsn(IADD); break;
		case OP_OR: mv.visitInsn(IOR); break;
		case OP_AND: mv.visitInsn(IAND); break;
		case OP_EQ: {
			if(expression_Binary.type==Type.BOOLEAN || expression_Binary.type == Type.INTEGER){
				Label l1 = new Label();
				mv.visitJumpInsn(IF_ICMPNE, l1);
				mv.visitInsn(ICONST_1);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
				break;	
		}else
			mv.visitInsn(IF_ACMPEQ); break;
		}
		case OP_NEQ: {
			if(expression_Binary.type==Type.BOOLEAN || expression_Binary.type == Type.INTEGER){
				Label l1 = new Label();
				mv.visitJumpInsn(IF_ICMPEQ, l1);
				mv.visitInsn(ICONST_1);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
				break;	
		}else
			mv.visitInsn(IF_ACMPNE); break;
		}
		case OP_LT: {
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPGE, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			break;	
		}
		case OP_GT : {
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPLE, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			break;	
		}
		case OP_LE: {
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPGT, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			break;	
		}
		case OP_GE: {
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPLT, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
			break;	
		}
		case OP_MINUS: mv.visitInsn(ISUB); break;
		case OP_TIMES: mv.visitInsn(IMUL); break;
		case OP_DIV: mv.visitInsn(IDIV); break;
		case OP_MOD: mv.visitInsn(IREM); break;
		}
		
	//	CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.type);
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {

		expression_Unary.e.visit(this, null);
		Kind op = expression_Unary.op;
		if(op==Kind.OP_MINUS){
			mv.visitInsn(INEG);
		}else if(op==Kind.OP_EXCL){
			if(expression_Unary.e.type==Type.BOOLEAN){
				mv.visitLdcInsn(true);
				mv.visitInsn(IXOR);
			}else{
				mv.visitLdcInsn(new Integer(Integer.MAX_VALUE));//TODO
				mv.visitInsn(IXOR);
			}
		}
		
	//	CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.type);
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {

		index.e0.visit(this, null);
		index.e1.visit(this, null);
		
		if(!index.isCartesian()) {
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		}
	
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		//TODO
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, ImageSupport.ImageDesc);
		expression_PixelSelector.index.visit(this, null);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", ImageSupport.getPixelSig, false);
		return null;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		Label startLabel = new Label();
		Label endLabel = new Label();
		expression_Conditional.condition.visit(this,null);
		mv.visitLdcInsn(true);
		mv.visitJumpInsn(IF_ICMPEQ, startLabel);
		expression_Conditional.falseExpression.visit(this, null);
		mv.visitJumpInsn(GOTO,endLabel);
		mv.visitLabel(startLabel);
		expression_Conditional.trueExpression.visit(this, null);
		mv.visitLabel(endLabel);
		
	//	CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.type);
		return null;
	}

	//TODO change in typecheck as per Doc 6
	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		
		cw.visitField(ACC_STATIC, declaration_Image.name, getVisitType(declaration_Image.Type), null, null);
		
		if(declaration_Image.source!=null) {
			declaration_Image.source.visit(this, null);
			if(declaration_Image.xSize!=null) {
				declaration_Image.xSize.visit(this, null);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
				declaration_Image.ySize.visit(this, null);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
			}else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig,false);
		}else {
			if(declaration_Image.xSize!=null) {
				declaration_Image.xSize.visit(this, null);
				declaration_Image.ySize.visit(this, null);
			}else {
				mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");
				mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig,false);
		}
		
		mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, ImageSupport.ImageDesc);
		
		return null;
	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {

		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
		return null;
	}

	

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		
		mv.visitVarInsn(ALOAD , 0);
		source_CommandLineParam.paramNum.visit(this, null);
		mv.visitInsn(AALOAD);
		return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {

		mv.visitLdcInsn(source_Ident.name);
		return null;
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {

		cw.visitField(ACC_STATIC, declaration_SourceSink.name, getVisitType(declaration_SourceSink.Type), null, null);
		if(declaration_SourceSink.source!=null) {
			declaration_SourceSink.source.visit(this, null);
			mv.visitFieldInsn(PUTSTATIC,className, declaration_SourceSink.name, ImageSupport.StringDesc);
		}
		
		return null;
	}
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {

		mv.visitLdcInsn(new Integer(expression_IntLit.value));
	//	CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		expression_FunctionAppWithExprArg.arg.visit(this, null);
		
		if(expression_FunctionAppWithExprArg.function==Kind.KW_abs) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig,false);
		}else if(expression_FunctionAppWithExprArg.function==Kind.KW_log) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig,false);
		}
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {

		expression_FunctionAppWithIndexArg.arg.e0.visit(this, null);
		expression_FunctionAppWithIndexArg.arg.e1.visit(this, null);
		switch(expression_FunctionAppWithIndexArg.function) {
		case KW_cart_x: mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig,false);break;
		case KW_cart_y: mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig,false);break;
		case KW_polar_a: mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig,false);break;
		case KW_polar_r: mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig,false);break;
		}
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {

		switch(expression_PredefinedName.kind) {
		case KW_x: mv.visitFieldInsn(GETSTATIC, className, "x", "I");break;
		case KW_y: mv.visitFieldInsn(GETSTATIC, className, "y", "I");break;
		case KW_a: {
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig,false);
			break;
		}
		case KW_r: {
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig,false);
			break;
		}
		case KW_X: mv.visitFieldInsn(GETSTATIC, className, "X", "I");break;
		case KW_Y: mv.visitFieldInsn(GETSTATIC, className, "Y", "I");break;
		case KW_R: mv.visitFieldInsn(GETSTATIC, className, "R", "I");break;
		case KW_A: mv.visitFieldInsn(GETSTATIC, className, "A", "I");break;
		case KW_Z: mv.visitFieldInsn(GETSTATIC, className, "Z", "I");break;
		case KW_DEF_X: mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");break;
		case KW_DEF_Y: mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");break;
		}

		return null;
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		
		if(statement_Out.getDec().Type ==Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, getVisitType(statement_Out.getDec().Type));
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeLog", "globalLogAddImage", "(Ljava/awt/image/BufferedImage;)V", false);
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, getVisitType(statement_Out.getDec().Type));
			statement_Out.sink.visit(this, null); 
		}else {
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, getVisitType(statement_Out.getDec().Type));
			
			CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().Type);
			
			if(statement_Out.getDec().Type == Type.INTEGER)
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
			else if(statement_Out.getDec().Type == Type.BOOLEAN)
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
		}
		
		
		return null;
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
	 *  to convert String to actual type. 
	 *  
	 *  TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		
		statement_In.source.visit(this, null);
		if(statement_In.getDec().Type == Type.INTEGER){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I",false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, getVisitType(statement_In.type));
		}else if(statement_In.getDec().Type == Type.BOOLEAN){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z",false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, getVisitType(statement_In.type));
		}else if(statement_In.getDec().Type == Type.IMAGE){
			Declaration_Image declaration_Image = (Declaration_Image)statement_In.getDec();
			if(declaration_Image.xSize!=null) {
				declaration_Image.xSize.visit(this, null);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
				declaration_Image.ySize.visit(this, null);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
			}else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig,false);
		
			mv.visitFieldInsn(PUTSTATIC,className, statement_In.name, ImageSupport.ImageDesc);
		}
		return null;
	}

	
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		
		if(statement_Assign.lhs.type!=Type.IMAGE) {
			statement_Assign.e.visit(this, null);
			statement_Assign.lhs.visit(this, null);
		}else {
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig,false);
			mv.visitFieldInsn(PUTSTATIC, className, "Y", "I");
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig,false);
			mv.visitFieldInsn(PUTSTATIC, className, "X", "I");
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTSTATIC, className, "x", "I");
			Label l3 = new Label();
			mv.visitJumpInsn(GOTO, l3);
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTSTATIC, className, "y", "I");
			Label l5 = new Label();
			mv.visitJumpInsn(GOTO, l5);
			Label l6 = new Label();
			mv.visitLabel(l6);
			statement_Assign.e.visit(this, null);
			statement_Assign.lhs.visit(this, null);
			Label l7 = new Label();
			mv.visitLabel(l7);
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitFieldInsn(PUTSTATIC, className, "y", "I");
			mv.visitLabel(l5);
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			mv.visitJumpInsn(IF_ICMPLT, l6);
			Label l8 = new Label();
			mv.visitLabel(l8);
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitFieldInsn(PUTSTATIC, className, "x", "I");
			mv.visitLabel(l3);
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "X", "I");
			mv.visitJumpInsn(IF_ICMPLT, l4);
			Label l9 = new Label();
			mv.visitLabel(l9);
		}
		
		return null;
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {

		String fieldName = lhs.name;
		String fieldType = getVisitType(lhs.type);
		if(lhs.type==Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, fieldName,fieldType);
			mv.visitFieldInsn(GETSTATIC, className, "x","I");
			mv.visitFieldInsn(GETSTATIC, className, "y","I");
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", ImageSupport.setPixelSig,false);
		}else {
			mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		}
		return null;
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {

		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeFrame", ImageSupport.makeFrameSig,false);
		mv.visitInsn(POP);
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {

		mv.visitLdcInsn(sink_Ident.name);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", ImageSupport.writeSig,false);
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {

		mv.visitLdcInsn(new Boolean(expression_BooleanLit.value));
	//	CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name,getVisitType(expression_Ident.type));
	//	CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.type);
		return null;
	}
	
	public static String getVisitType(Type type){
		
		switch(type){
		case INTEGER: return "I";
		case BOOLEAN: return "Z";
		case IMAGE: return ImageSupport.ImageDesc;
		default: return ImageSupport.StringDesc;
		}
	}
	
	public static int getSlot(String s) {
		switch(s) {
		case "x": return 1;
		case "y": return 2;
		case "X": return 3;
		case "Y": return 4;
		case "r": return 5;
		case "a": return 6;
		case "R": return 7;
		case "A": return 8;
		default: return -1;
		}
	}

}
