package exceptions;

import exceptions.CD4APluginErrorLog.ExceptionType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import model.nodes.AbstractNode;
import model.nodes.Node;
import plugin.MontiCoreException;
import view.nodes.AbstractNodeView;

public class AssocLeftRefNameMissingException implements MontiCoreException {
  private AbstractNode currentNode;
  private ExceptionType type = ExceptionType.LEFT_REFERENCE_NAME_MISSING;
  private Pane currentPane;
  
  private AbstractNodeView nodeView;
  
  public AssocLeftRefNameMissingException(Node node, AbstractNodeView view) {
    this.currentNode = (AbstractNode) node;
    this.setPane();
    this.nodeView = view;
  }
  
  @Override
  public String getContentMessage() {
    return "Left reference name is missing!";
  }
  
  public void setType(ExceptionType t) {
    this.type = t;
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
  
  public ExceptionType getType() {
    return type;
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
