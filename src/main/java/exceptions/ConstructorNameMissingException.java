package exceptions;

import model.nodes.AbstractNode;
import plugin.MontiCoreException;

public class ConstructorNameMissingException implements MontiCoreException {
  private AbstractNode currentNode;
  private ExceptionType type = ExceptionType.CONSTRUCTOR_NAME_MISSING;

  public ConstructorNameMissingException(AbstractNode node){
      this.currentNode = node;
  }

  @Override
  public String getContentMessage() {
      return "Constructor name is missing!";
  }

  public void setType(ExceptionType t){
      this.type = t;
  }

  public ExceptionType getType() {
      return type;
  }
  
  public AbstractNode getNode(){
      return this.currentNode;
  }

  public void setNode(AbstractNode n){
      this.currentNode = n;
  }
}
