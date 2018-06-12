package exceptions;

import exceptions.CD4APluginErrorLog.ExceptionType;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import model.nodes.AbstractNode;
import model.nodes.Node;
import plugin.MontiCoreException;
import view.nodes.AbstractNodeView;

public class CoCoException implements MontiCoreException {
  private AbstractNode currentNode;
  private ExceptionType type = ExceptionType.COCO_ERROR;
  private Pane currentPane;
  private AbstractNodeView nodeView;
  private String contentMsg;
  
  public CoCoException(Node node, AbstractNodeView view, String msg) {
    this.currentNode = (AbstractNode) node;
    this.setPane();
    this.nodeView = view;
    this.contentMsg = msg;
  }
  
  @Override
  public String getContentMessage() {
    return contentMsg;
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
    currentPane.setOnMouseClicked(new EventHandler<MouseEvent>() {

      @Override
      public void handle(MouseEvent event) {
        handleActionClickOnPane();
      }
      
    });
  }
  
  public AbstractNode getNode() {
    return this.currentNode;
  }
  
  public void setNode(AbstractNode n) {
    this.currentNode = n;
  }
  
  @Override
  public void handleActionClickOnPane() {
    
  }
}