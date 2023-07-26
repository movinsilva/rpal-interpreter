package csem;

import java.util.Stack;

import ast.ASTNode;
import ast.ASTNodeType;

/**
 * Used to evaluate conditionals.
 * 'cond -> then | else' in source becomes 'Beta cond' on the control stack
 * where
 * Beta.thenBranch = standardized version of then
 * Beta.elseBranch = standardized version of else
 * 
 * This inversion is key to implementing a program order evaluation
 * (critical for recursion where putting the then and else nodes above the
 * Conditional
 * node on the control stack will cause infinite recursion if the then and else
 * nodes call the recursive function themselves). Putting the cond node before
 * Beta (and, since
 * Beta contains the then and else nodes, effectively before the then and else
 * nodes), allows
 * evaluating the cond first and then (in the base case) choosing the
 * non-recursive option. This
 * allows breaking out of infinite recursion.
 * 
 * @author Raj
 */
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
