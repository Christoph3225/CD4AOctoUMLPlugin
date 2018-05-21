package exceptions;

import exceptions.CD4APluginErrorLog.ExceptionType;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import model.nodes.AbstractNode;
import model.nodes.Node;
import plugin.MontiCoreException;


public class CodeGenerationException implements MontiCoreException {
  private AbstractNode currentNode;
  private ExceptionType type = ExceptionType.CODE_GENERATION_ERROR;
  private Pane currentPane;
  
  public CodeGenerationException(Node node){
      this.currentNode = (AbstractNode) node;
      this.setPane();
  }

  @Override
  public String getContentMessage() {
      return "Code generation error!";
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
