package exceptions;

import exceptions.CD4APluginErrorLog.ExceptionType;
import model.nodes.AbstractNode;
import model.nodes.Node;
import plugin.MontiCoreException;
import javafx.scene.layout.*;
import javafx.scene.control.*;

public class InterfaceNameMissingException implements MontiCoreException {
  private AbstractNode currentNode;
  private ExceptionType type = ExceptionType.INTERFACE_NAME_MISSING;
  private Pane currentPane;
  
  public InterfaceNameMissingException(Node node){
      this.currentNode = (AbstractNode) node;
      this.setPane();
  }

  @Override
  public String getContentMessage() {
      return "Interface name is missing!";
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
