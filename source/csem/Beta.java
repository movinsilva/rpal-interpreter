package csem;

import java.util.Stack;

import ast.ASTNode;
import ast.ASTNodeType;

public class Beta extends ASTNode {
  private Stack<ASTNode> thenBranch; // Changed the variable name to thenBranch
  private Stack<ASTNode> elseBranch;

  public Beta() {
    setType(ASTNodeType.BETA);
    thenBranch = new Stack<ASTNode>(); // Updated variable initialization
    elseBranch = new Stack<ASTNode>();
  }

  public Beta accept(NodeCopier nodeCopier) {
    return nodeCopier.copy(this);
  }

  public Stack<ASTNode> getThenBody() { // Keep the method name unchanged
    return thenBranch;
  }

  public Stack<ASTNode> getElseBody() { // Keep the method name unchanged
    return elseBranch;
  }

  public void setThenBody(Stack<ASTNode> thenBody) { // Keep the method name unchanged
    this.thenBranch = thenBody;
  }

  public void setElseBody(Stack<ASTNode> elseBody) { // Keep the method name unchanged
    this.elseBranch = elseBody;
  }

}