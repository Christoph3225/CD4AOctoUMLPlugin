package exceptions;

import model.nodes.AbstractNode;
import plugin.MontiCoreException;

public class MethodParameterTypeMissingException implements MontiCoreException {
  private AbstractNode currentNode;
  private ExceptionType type = ExceptionType.METHOD_PARAMETER_TYPE_MISSING;

  public MethodParameterTypeMissingException(AbstractNode node){
      this.currentNode = node;
  }

  @Override
  public String getContentMessage() {
      return "Method parameter type is missing!";
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
