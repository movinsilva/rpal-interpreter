package csem;

import java.util.Stack;
import ast.AST;
import ast.ASTNode;
import ast.ASTNodeType;

public class CSEMachine {

  private Stack<ASTNode> resultStack;
  private Delta rootNodeDelta;

  public CSEMachine(AST ast) {
    // check if the tree is standardize
    if (!ast.isStandardized())
      throw new RuntimeException("AST has NOT been standardized!");
    // Initializes the result stack and the root delta node.
    rootNodeDelta = ast.createDeltas();
    rootNodeDelta.setLinkedEnv(new Environment()); // primitive environment
    resultStack = new Stack<ASTNode>();
  }

  // Method to evaluate the entire program.
  public void evaluateProgram() {
    processControlStack(rootNodeDelta, rootNodeDelta.getLinkedEnv());
  }

  // Method to process the control stack for a given delta node and environment.
  private void processControlStack(Delta currentNodeDelta, Environment env) {

    Stack<ASTNode> controlStack = new Stack<ASTNode>();
    // store the ASTNode elements to be processed during the evaluation.
    controlStack.addAll(currentNodeDelta.getBody());

    // iterates over the control stack and processes each instruction one by one
    // until all instructions are executed
    while (!controlStack.isEmpty())
      processCurrentNode(currentNodeDelta, env, controlStack);
  }

  private void processCurrentNode(Delta currentNodeDelta, Environment env, Stack<ASTNode> currentControlStack) {
    // retrieves the next instruction to be processed
    ASTNode node = currentControlStack.pop();

    // check if a binary operation
    if (applyBinaryOperation(node))
      return;
    // check if a unary operation
    else if (applyUnaryOperation(node))
      return;
    // else handle other types of operations
    else {
      switch (node.getType()) {
        case IDENTIFIER:
          handleIdentifiers(node, env);
          break;
        case NIL:
        case TAU:
          // calls the createTuple method to create a new tuple node
          createTuple(node);
          break;
        case BETA:
          // calls the handleBeta method to handle the beta reduction operation
          handleBeta((Beta) node, currentControlStack);
          break;
        case GAMMA:
          // calls the applyGamma method to handle the gamma reduction operation
          applyGamma(currentNodeDelta, node, env, currentControlStack);
          break;
        case DELTA:
          // Rule 2
          // sets the linked environment of the Delta node to the current environment
          // and pushes the Delta node back to the resultStack
          ((Delta) node).setLinkedEnv(env);
          resultStack.push(node);
          break;
        default:
          resultStack.push(node);
          break;
      }
    }
  }

  // Rule 6
  // applying binary operations on operands based on the type of the opeoperator
  private boolean applyBinaryOperation(ASTNode operator) {
    switch (operator.getType()) {
      // all below operators fall in to binary arithmetic operation type
      // hence call the binaryArithmeticOp method
      case PLUS:
      case MINUS:
      case MULT:
      case DIV:
      case EXP:
      case LS:
      case LE:
      case GR:
      case GE:
        binaryArithmeticOp(operator.getType());
        return true;
      // calls the binaryLogicalEqNeOp method with the corresponding operator type
      // to evaluate the equality or inequality
      case EQ:
      case NE:
        binaryLogicalEqNeOp(operator.getType());
        return true;
      // calls the binaryLogicalOrAndOp method with the corresponding operator type
      // to evaluate the logical OR and AND.
      case OR:
      case AND:
        binaryLogicalOrAndOp(operator.getType());
        return true;
      // calls the augTuples method to perform tuple augmentation
      case AUG:
        augTuples();
        return true;
      default:
        // not a binary operator supported by the CSEMachine
        return false;
    }
  }

  // perform binary arithmetic operations
  private void binaryArithmeticOp(ASTNodeType type) {
    //
    ASTNode operand1 = resultStack.pop();
    ASTNode operand2 = resultStack.pop();
    if (operand1.getType() != ASTNodeType.INTEGER || operand2.getType() != ASTNodeType.INTEGER)
      EvaluationError.printError(operand1.getSourceLineNumber(),
          "Expected two integers; was given \"" + operand1.getValue() + "\", \"" + operand2.getValue() + "\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);
    // enters to a switch statement based on the type of the arithmetic operation
    switch (type) {
      case PLUS:
        result
            .setValue(Integer.toString(Integer.parseInt(operand1.getValue()) + Integer.parseInt(operand2.getValue())));
        break;
      case MINUS:
        result
            .setValue(Integer.toString(Integer.parseInt(operand1.getValue()) - Integer.parseInt(operand2.getValue())));
        break;
      case MULT:
        result
            .setValue(Integer.toString(Integer.parseInt(operand1.getValue()) * Integer.parseInt(operand2.getValue())));
        break;
      case DIV:
        result
            .setValue(Integer.toString(Integer.parseInt(operand1.getValue()) / Integer.parseInt(operand2.getValue())));
        break;
      case EXP:
        result.setValue(
            Integer.toString(
                (int) Math.pow(Integer.parseInt(operand1.getValue()), Integer.parseInt(operand2.getValue()))));
        break;
      case LS:
        if (Integer.parseInt(operand1.getValue()) < Integer.parseInt(operand2.getValue()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case LE:
        if (Integer.parseInt(operand1.getValue()) <= Integer.parseInt(operand2.getValue()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case GR:
        if (Integer.parseInt(operand1.getValue()) > Integer.parseInt(operand2.getValue()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case GE:
        if (Integer.parseInt(operand1.getValue()) >= Integer.parseInt(operand2.getValue()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      // If the type does not match any of the known arithmetic operations,
      // the method exits the switch statement.
      default:
        break;
    }
    // result node is pushed onto the resultStack
    resultStack.push(result);
  }

  // performing binary logical equality and inequality
  private void binaryLogicalEqNeOp(ASTNodeType type) {
    ASTNode operand1 = resultStack.pop();
    ASTNode operand2 = resultStack.pop();

    // checks the types of operand1 and operand2 before performing binary logical
    // equality
    if (operand1.getType() == ASTNodeType.TRUE || operand1.getType() == ASTNodeType.FALSE) {
      if (operand2.getType() != ASTNodeType.TRUE && operand2.getType() != ASTNodeType.FALSE)
        EvaluationError.printError(operand1.getSourceLineNumber(),
            "Type error in the program");
      compareTruthValues(operand1, operand2, type);
      return;
    }

    if (operand1.getType() != operand2.getType())
      EvaluationError.printError(operand1.getSourceLineNumber(),
          "type error in the program");

    // checks the specific type of operand1 to determine which
    // comparison operation to perform
    if (operand1.getType() == ASTNodeType.STRING)
      compareStrings(operand1, operand2, type);
    else if (operand1.getType() == ASTNodeType.INTEGER)
      compareIntegers(operand1, operand2, type);
    else
      EvaluationError.printError(operand1.getSourceLineNumber(),
          " comparison operation is not supported for the given types");

  }

  private void compareTruthValues(ASTNode operand1, ASTNode operand2, ASTNodeType type) {
    if (operand1.getType() == operand2.getType())
      if (type == ASTNodeType.EQ)
        pushTrueNode();
      else
        pushFalseNode();
    else if (type == ASTNodeType.EQ)
      pushFalseNode();
    else
      pushTrueNode();
  }

  private void compareStrings(ASTNode operand1, ASTNode operand2, ASTNodeType type) {
    if (operand1.getValue().equals(operand2.getValue()))
      if (type == ASTNodeType.EQ)
        pushTrueNode();
      else
        pushFalseNode();
    else if (type == ASTNodeType.EQ)
      pushFalseNode();
    else
      pushTrueNode();
  }

  private void compareIntegers(ASTNode operand1, ASTNode operand2, ASTNodeType type) {
    if (Integer.parseInt(operand1.getValue()) == Integer.parseInt(operand2.getValue()))
      if (type == ASTNodeType.EQ)
        pushTrueNode();
      else
        pushFalseNode();
    else if (type == ASTNodeType.EQ)
      pushFalseNode();
    else
      pushTrueNode();
  }

  private void binaryLogicalOrAndOp(ASTNodeType type) {
    ASTNode operand1 = resultStack.pop();
    ASTNode operand2 = resultStack.pop();

    if ((operand1.getType() == ASTNodeType.TRUE || operand1.getType() == ASTNodeType.FALSE) &&
        (operand2.getType() == ASTNodeType.TRUE || operand2.getType() == ASTNodeType.FALSE)) {
      orAndTruthValues(operand1, operand2, type);
      return;
    }

    EvaluationError.printError(operand1.getSourceLineNumber(),
        "Don't know how to " + type + " \"" + operand1.getValue() + "\", \"" + operand2.getValue() + "\"");
  }

  private void orAndTruthValues(ASTNode operand1, ASTNode operand2, ASTNodeType type) {
    if (type == ASTNodeType.OR) {
      if (operand1.getType() == ASTNodeType.TRUE || operand2.getType() == ASTNodeType.TRUE)
        pushTrueNode();
      else
        pushFalseNode();
    } else {
      if (operand1.getType() == ASTNodeType.TRUE && operand2.getType() == ASTNodeType.TRUE)
        pushTrueNode();
      else
        pushFalseNode();
    }
  }

  private void augTuples() {
    ASTNode operand1 = resultStack.pop();
    ASTNode operand2 = resultStack.pop();

    if (operand1.getType() != ASTNodeType.TUPLE)
      EvaluationError.printError(operand1.getSourceLineNumber(),
          "Cannot augment a non-tuple \"" + operand1.getValue() + "\"");

    ASTNode childNode = operand1.getChild();
    if (childNode == null)
      operand1.setChild(operand2);
    else {
      while (childNode.getSibling() != null)
        childNode = childNode.getSibling();
      childNode.setSibling(operand2);
    }
    operand2.setSibling(null);

    resultStack.push(operand1);
  }

  // RULE 7
  // handling unary operations (NOT and NEG) on the result stack
  private boolean applyUnaryOperation(ASTNode operator) {
    switch (operator.getType()) {
      case NOT:
        not();
        return true;
      case NEG:
        neg();
        return true;
      default:
        return false;
    }
  }

  private void not() {
    ASTNode rand = resultStack.pop();
    if (rand.getType() != ASTNodeType.TRUE && rand.getType() != ASTNodeType.FALSE)
      EvaluationError.printError(rand.getSourceLineNumber(),
          "Not given a truth value");

    // Negate the truth value and push the result back to the resultStack.
    if (rand.getType() == ASTNodeType.TRUE)
      pushFalseNode();
    else
      pushTrueNode();
  }

  // handles the negation of integers
  private void neg() {
    ASTNode rand = resultStack.pop();
    if (rand.getType() != ASTNodeType.INTEGER)
      EvaluationError.printError(rand.getSourceLineNumber(),
          "An integer was expected");

    // Negate the integer value and push the result back to the resultStack.
    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);
    result.setValue(Integer.toString(-1 * Integer.parseInt(rand.getValue())));
    resultStack.push(result);
  }

  // RULE 3
  // handles the reduction of beta and delta nodes
  private void applyGamma(Delta currentDelta, ASTNode node, Environment env,
      Stack<ASTNode> currentControlStack) {
    ASTNode operator = resultStack.pop();
    ASTNode rand = resultStack.pop();

    if (operator.getType() == ASTNodeType.DELTA) {
      Delta nextDelta = (Delta) operator;

      Environment newEnv = new Environment();
      newEnv.setParent(nextDelta.getLinkedEnv());

      // RULE 4
      if (nextDelta.getBoundVars().size() == 1) {
        newEnv.addMapping(nextDelta.getBoundVars().get(0), rand);
      }
      // RULE 11
      else {
        if (rand.getType() != ASTNodeType.TUPLE)
          EvaluationError.printError(rand.getSourceLineNumber(),
              "Expected a tuple; was given \"" + rand.getValue() + "\"");

        for (int i = 0; i < nextDelta.getBoundVars().size(); i++) {
          newEnv.addMapping(nextDelta.getBoundVars().get(i), getNthTupleChild((Tuple) rand, i + 1)); // + 1 coz tuple
                                                                                                     // indexing starts
                                                                                                     // at 1
        }
      }

      processControlStack(nextDelta, newEnv);
      return;
    } else if (operator.getType() == ASTNodeType.YSTAR) {
      // RULE 12
      if (rand.getType() != ASTNodeType.DELTA)
        EvaluationError.printError(rand.getSourceLineNumber(),
            "Expected a Delta; was given \"" + rand.getValue() + "\"");

      Eta etaNode = new Eta();
      etaNode.setDelta((Delta) rand);
      resultStack.push(etaNode);
      return;
    } else if (operator.getType() == ASTNodeType.ETA) {
      // RULE 13
      // push back the rand, the eta and then the delta it contains
      resultStack.push(rand);
      resultStack.push(operator);
      resultStack.push(((Eta) operator).getDelta());
      // push back two gammas (one for the eta and one for the delta)
      currentControlStack.push(node);
      currentControlStack.push(node);
      return;
    } else if (operator.getType() == ASTNodeType.TUPLE) {
      tupleSelection((Tuple) operator, rand);
      return;
    } else if (evaluateReservedIdentifiers(operator, rand, currentControlStack))
      return;
    else
      EvaluationError.printError(operator.getSourceLineNumber(),
          "Don't know how to evaluate \"" + operator.getValue() + "\"");
  }

  private boolean evaluateReservedIdentifiers(ASTNode operator, ASTNode rand, Stack<ASTNode> currentControlStack) {
    switch (operator.getValue()) {
      case "Isinteger":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.INTEGER);
        return true;
      case "Isstring":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.STRING);
        return true;
      case "Isdummy":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.DUMMY);
        return true;
      case "Isfunction":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.DELTA);
        return true;
      case "Istuple":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.TUPLE);
        return true;
      case "Istruthvalue":
        if (rand.getType() == ASTNodeType.TRUE || rand.getType() == ASTNodeType.FALSE)
          pushTrueNode();
        else
          pushFalseNode();
        return true;
      case "Stem":
        stem(rand);
        return true;
      case "Stern":
        stern(rand);
        return true;
      case "Conc":
      case "conc": // typos
        conc(rand, currentControlStack);
        return true;
      case "Print":
      case "print": // typos
        printNodeValue(rand);
        pushDummyNode();
        return true;
      case "ItoS":
        itos(rand);
        return true;
      case "Order":
        order(rand);
        return true;
      case "Null":
        isNullTuple(rand);
        return true;
      default:
        return false;
    }
  }

  private void checkTypeAndPushTrueOrFalse(ASTNode rand, ASTNodeType type) {
    if (rand.getType() == type)
      pushTrueNode();
    else
      pushFalseNode();
  }

  private void pushTrueNode() {
    ASTNode trueNode = new ASTNode();
    trueNode.setType(ASTNodeType.TRUE);
    trueNode.setValue("true");
    resultStack.push(trueNode);
  }

  private void pushFalseNode() {
    ASTNode falseNode = new ASTNode();
    falseNode.setType(ASTNodeType.FALSE);
    falseNode.setValue("false");
    resultStack.push(falseNode);
  }

  private void pushDummyNode() {
    ASTNode falseNode = new ASTNode();
    falseNode.setType(ASTNodeType.DUMMY);
    resultStack.push(falseNode);
  }

  private void stem(ASTNode rand) {
    if (rand.getType() != ASTNodeType.STRING)
      EvaluationError.printError(rand.getSourceLineNumber(),
          "Expected a string; was given \"" + rand.getValue() + "\"");

    if (rand.getValue().isEmpty())
      rand.setValue("");
    else
      rand.setValue(rand.getValue().substring(0, 1));

    resultStack.push(rand);
  }

  private void stern(ASTNode rand) {
    if (rand.getType() != ASTNodeType.STRING)
      EvaluationError.printError(rand.getSourceLineNumber(),
          "Expected a string; was given \"" + rand.getValue() + "\"");

    if (rand.getValue().isEmpty() || rand.getValue().length() == 1)
      rand.setValue("");
    else
      rand.setValue(rand.getValue().substring(1));

    resultStack.push(rand);
  }

  private void conc(ASTNode operand1, Stack<ASTNode> currentControlStack) {
    currentControlStack.pop();
    ASTNode operand2 = resultStack.pop();
    if (operand1.getType() != ASTNodeType.STRING || operand2.getType() != ASTNodeType.STRING)
      EvaluationError.printError(operand1.getSourceLineNumber(),
          "Expected two strings; was given \"" + operand1.getValue() + "\", \"" + operand2.getValue() + "\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.STRING);
    result.setValue(operand1.getValue() + operand2.getValue());

    resultStack.push(result);
  }

  private void itos(ASTNode rand) {
    if (rand.getType() != ASTNodeType.INTEGER)
      EvaluationError.printError(rand.getSourceLineNumber(),
          "Expected an integer; was given \"" + rand.getValue() + "\"");

    rand.setType(ASTNodeType.STRING); // all values are stored internally as strings, so nothing else to do
    resultStack.push(rand);
  }

  private void order(ASTNode rand) {
    if (rand.getType() != ASTNodeType.TUPLE)
      EvaluationError.printError(rand.getSourceLineNumber(), "Expected a tuple; was given \"" + rand.getValue() + "\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);
    result.setValue(Integer.toString(getNumChildren(rand)));

    resultStack.push(result);
  }

  private void isNullTuple(ASTNode rand) {
    if (rand.getType() != ASTNodeType.TUPLE)
      EvaluationError.printError(rand.getSourceLineNumber(), "Expected a tuple; was given \"" + rand.getValue() + "\"");

    if (getNumChildren(rand) == 0)
      pushTrueNode();
    else
      pushFalseNode();
  }

  // RULE 10
  private void tupleSelection(Tuple operator, ASTNode rand) {
    if (rand.getType() != ASTNodeType.INTEGER)
      EvaluationError.printError(rand.getSourceLineNumber(),
          "Non-integer tuple selection with \"" + rand.getValue() + "\"");

    ASTNode result = getNthTupleChild(operator, Integer.parseInt(rand.getValue()));
    if (result == null)
      EvaluationError.printError(rand.getSourceLineNumber(),
          "Tuple selection index " + rand.getValue() + " out of bounds");

    resultStack.push(result);
  }

  /**
   * Get the nth element of the tuple. Note that n starts from 1 and NOT 0.
   * 
   * @param tupleNode
   * @param n         n starts from 1 and NOT 0.
   * @return
   */
  private ASTNode getNthTupleChild(Tuple tupleNode, int n) {
    ASTNode childNode = tupleNode.getChild();
    for (int i = 1; i < n; ++i) { // tuple selection index starts at 1
      if (childNode == null)
        break;
      childNode = childNode.getSibling();
    }
    return childNode;
  }

  private void handleIdentifiers(ASTNode node, Environment env) {
    if (env.lookup(node.getValue()) != null) // RULE 1
      resultStack.push(env.lookup(node.getValue()));
    else if (isReservedIdentifier(node.getValue()))
      resultStack.push(node);
    else
      EvaluationError.printError(node.getSourceLineNumber(), "Undeclared identifier \"" + node.getValue() + "\"");
  }

  // RULE 9
  private void createTuple(ASTNode node) {
    int childCount = getNumChildren(node);
    Tuple tupleNode = new Tuple();
    if (childCount == 0) {
      resultStack.push(tupleNode);
      return;
    }

    ASTNode childNode = null, tempNode = null;
    for (int i = 0; i < childCount; ++i) {
      if (childNode == null)
        childNode = resultStack.pop();
      else if (tempNode == null) {
        tempNode = resultStack.pop();
        childNode.setSibling(tempNode);
      } else {
        tempNode.setSibling(resultStack.pop());
        tempNode = tempNode.getSibling();
      }
    }
    tempNode.setSibling(null);
    tupleNode.setChild(childNode);
    resultStack.push(tupleNode);
  }

  // RULE 8
  private void handleBeta(Beta node, Stack<ASTNode> currentControlStack) {
    ASTNode conditionResultNode = resultStack.pop();

    if (conditionResultNode.getType() != ASTNodeType.TRUE && conditionResultNode.getType() != ASTNodeType.FALSE)
      EvaluationError.printError(conditionResultNode.getSourceLineNumber(),
          "Expecting a truthvalue; found \"" + conditionResultNode.getValue() + "\"");

    if (conditionResultNode.getType() == ASTNodeType.TRUE)
      currentControlStack.addAll(node.getThenBody());
    else
      currentControlStack.addAll(node.getElseBody());
  }

  private int getNumChildren(ASTNode node) {
    int childCount = 0;
    ASTNode childNode = node.getChild();
    while (childNode != null) {
      childCount++;
      childNode = childNode.getSibling();
    }
    return childCount;
  }

  private void printNodeValue(ASTNode rand) {
    String evaluationResult = rand.getValue();
    evaluationResult = evaluationResult.replace("\\t", "\t");
    evaluationResult = evaluationResult.replace("\\n", "\n");
    System.out.print(evaluationResult);
  }

  // Note how this list is different from the one defined in Scanner.java
  private boolean isReservedIdentifier(String value) {
    switch (value) {
      case "Isinteger":
      case "Isstring":
      case "Istuple":
      case "Isdummy":
      case "Istruthvalue":
      case "Isfunction":
      case "ItoS":
      case "Order":
      case "Conc":
      case "conc": // typos
      case "Stern":
      case "Stem":
      case "Null":
      case "Print":
      case "print": // typos
      case "neg":
        return true;
    }
    return false;
  }

}