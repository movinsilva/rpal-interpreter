package csem;

import ast.ASTNode;
import ast.ASTNodeType;

public class Eta extends ASTNode {
  private Delta delta;

  public Eta() {
    setType(ASTNodeType.ETA);
  }

  @Override
  public String getValue() {
    return "[eta closure: " + delta.getBoundVars().get(0) + ": " + delta.getIndex() + "]";
  }

  public Eta accept(NodeCopier nodeCopier) {
    return nodeCopier.copy(this);
  }

  public Delta getDelta() {
    return delta;
  }

  public void setDelta(Delta delta) {
    this.delta = delta;
  }

}