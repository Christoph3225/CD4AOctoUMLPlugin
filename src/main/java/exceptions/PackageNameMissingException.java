package exceptions;

import org.controlsfx.control.Notifications;

import exceptions.CD4APluginErrorLog.ExceptionType;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import model.nodes.AbstractNode;
import model.nodes.Node;
import plugin.MontiCoreException;
import view.nodes.AbstractNodeView;

public class PackageNameMissingException implements MontiCoreException {
  
  private AbstractNode currentNode;
  private ExceptionType type = ExceptionType.PACKAGE_NAME_MISSING;
  private Pane currentPane;
  
  private AbstractNodeView nodeView;
  
  public PackageNameMissingException(Node node, AbstractNodeView view) {
    this.currentNode = (AbstractNode) node;
    this.setPane();
    this.nodeView = view;
  }
  
  @Override
  public String getContentMessage() {
    return "Package name is missing!";
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
  
  @Override
  public AbstractNode getNode() {
    return this.currentNode;
  }
  
  public void setNode(AbstractNode n) {
    this.currentNode = n;
  }
  
  @Override
  public void handleActionClickOnPane() {
    Notifications.create().title("Error").text(getContentMessage()).showInformation();
  }
}
