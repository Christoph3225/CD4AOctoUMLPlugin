package exceptions;

import exceptions.CD4APluginErrorLog.ExceptionType;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import model.nodes.AbstractNode;
import model.nodes.Node;
import plugin.MontiCoreException;

public class PackageNameMissingException implements MontiCoreException {
  
  private AbstractNode currentNode;
  private ExceptionType type = ExceptionType.PACKAGE_NAME_MISSING;
  private Pane currentPane;

  public PackageNameMissingException(Node node){
      this.currentNode = (AbstractNode) node;
      this.setPane();
  }

  @Override
  public String getContentMessage() {
      return "Package name is missing!";
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
  
  @Override
  public AbstractNode getNode(){
      return this.currentNode;
  }

  public void setNode(AbstractNode n){
      this.currentNode = n;
  }
}
