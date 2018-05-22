package exceptions;

import exceptions.CD4APluginErrorLog.ExceptionType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import model.nodes.AbstractNode;
import model.nodes.Node;
import plugin.MontiCoreException;
import view.nodes.AbstractNodeView;

public class MethodParameterTypeMissingException implements MontiCoreException {
  private AbstractNode currentNode;
  private ExceptionType type = ExceptionType.METHOD_PARAMETER_TYPE_MISSING;
  private Pane currentPane;
  
  private AbstractNodeView nodeView;
  
  public MethodParameterTypeMissingException(Node node, AbstractNodeView view) {
    this.currentNode = (AbstractNode) node;
    this.setPane();
    this.nodeView = view;
  }
  
  @Override
  public String getContentMessage() {
    return "Method parameter type is missing!";
  }
  
  public void setType(ExceptionType t) {
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
  
  public AbstractNode getNode() {
    return this.currentNode;
  }
  
  public void setNode(AbstractNode n) {
    this.currentNode = n;
  }
  
  @Override
  public void handleActionClickOnPane() {
    this.nodeView.setSelected(true);
  }
}
