package exceptions;

import exceptions.CD4APluginErrorLog.ExceptionType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import model.nodes.AbstractNode;
import model.nodes.Node;
import plugin.MontiCoreException;

public class EnumConstantNameMissingException implements MontiCoreException {
  private AbstractNode currentNode;
  private ExceptionType type = ExceptionType.ENUM_CONSTANT_NAME_MISSING;
  private Pane currentPane;
  
  public EnumConstantNameMissingException(Node node){
      this.currentNode = (AbstractNode) node;
      this.setPane();
  }

  @Override
  public String getContentMessage() {
      return "Enum constant name is missing!";
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
