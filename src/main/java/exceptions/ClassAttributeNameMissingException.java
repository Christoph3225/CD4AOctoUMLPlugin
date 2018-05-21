package exceptions;

import exceptions.CD4APluginErrorLog.ExceptionType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import model.nodes.AbstractNode;
import model.nodes.Node;
import plugin.MontiCoreException;

public class ClassAttributeNameMissingException implements MontiCoreException {
  private AbstractNode currentNode;
  private ExceptionType type = ExceptionType.CLASS_ATTRIBUTE_NAME_MISSING;
  private Pane currentPane;
  
  public ClassAttributeNameMissingException(Node node){
      this.currentNode = (AbstractNode) node;
      this.setPane();
  }

  @Override
  public String getContentMessage() {
      return "Class attribute name is missing!";
  }

  public void setType(ExceptionType t){
      this.type = t;
  }

  public ExceptionType getType() {
      return type;
  }
  
  @Override
  public Pane getContentPane() {
	  return this.currentPane;
  }
  
  private void setPane() {
	  currentPane = new Pane();
	  Label lbl = new Label(getContentMessage());
	  currentPane.getChildren().add(lbl);
  }
  
  public AbstractNode getNode(){
      return this.currentNode;
  }

  public void setNode(AbstractNode n){
      this.currentNode = n;
  }
}
