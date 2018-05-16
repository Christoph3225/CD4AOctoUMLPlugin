package exceptions;

import model.nodes.AbstractNode;
import plugin.MontiCoreException;

public class ClassAttributeTypeMissingException implements MontiCoreException {
  private AbstractNode currentNode;
  private ExceptionType type = ExceptionType.CLASS_ATTRIBUTE_TYPE_MISSING;

  public ClassAttributeTypeMissingException(AbstractNode node){
      this.currentNode = node;
  }

  @Override
  public String getContentMessage() {
      return "Class attribute type is missing!";
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
