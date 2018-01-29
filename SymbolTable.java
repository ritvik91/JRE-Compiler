package cop5556fa17;

import java.util.HashMap;

import cop5556fa17.AST.ASTNode;

public class SymbolTable {

	private HashMap<String, ASTNode> symbolTable = new HashMap<>(); //TODO
	
	public ASTNode lookupType(String name){
		return this.symbolTable.get(name);
	}
	
	public void insert(String name, ASTNode node){
		this.symbolTable.put(name, node);
	}
}
