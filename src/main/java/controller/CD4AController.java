package controller;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.PopOver;

import de.monticore.prettyprint.IndentPrinter;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.umlcd4a.prettyprint.CDPrettyPrinterConcreteVisitor;
import exceptions.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import model.Graph;
import model.Sketch;
import model.edges.AbstractEdge;
import model.edges.Edge;
import model.edges.InheritanceEdge;
import model.nodes.*;
import plugin.CD4APlugin;
import plugin.MontiCoreException;
import util.commands.CompoundCommand;
import util.commands.MoveGraphElementCommand;
import view.nodes.AbstractNodeView;
import view.nodes.PackageNodeView;

public class CD4AController extends AbstractDiagramController {
  
  @FXML
  Button showErrorLogBtn, editInfoBtn, showCodeBtn;
  @FXML
  Label packageLbl, cdNameLbl, importLbl;
  
  CD4APlugin plugin = CD4APlugin.getInstance();
  private String packageName, modelName, imports;
  private ASTCDCompilationUnit unit;
  
  private CD4APluginErrorLog errorLog = CD4APluginErrorLog.getInstance();
  private int errorCounter = 0;
  
  @FXML
  public void initialize() {
    super.initialize();
    initToolBarActions();
    initDrawPaneActions();
    validateBtn.setDisable(true);
    generateBtn.setDisable(true);
  }
  
  @Override
  public String getTabControllerName() {
    return "CD4A Class diagram";
  }
  
  private void initContainerInfo() {
    List<String> infoList = new ArrayList<>();
    infoList.add(packageName);
    infoList.add(imports);
    infoList.add(modelName);
    List<String> containerInfo = plugin.showContainerInfoDialog(getStage(), infoList);
    packageName = containerInfo.get(0);
    imports = containerInfo.get(1);
    modelName = containerInfo.get(2);
    packageLbl.setText("package " + packageName + ";");
    if (!(imports == null)) {
      String[] arr = imports.split(";");
      String allImports = "";
      for (int i = 0; i < arr.length; i++) {
        allImports += "import " + arr[i] + "; ";
      }
      importLbl.setText(allImports);
    }
    else {
      importLbl.setText("");
    }
    cdNameLbl.setText("classdiagram " + modelName + " {");
  }
  
  void initDrawPaneActions() {
    drawPane.setOnMousePressed(event -> {
      if (mode == Mode.NO_MODE) {
        if (event.getButton() == MouseButton.SECONDARY) { // Create context menu
                                                          // on right-click.
          mode = Mode.CONTEXT_MENU;
          copyPasteController.copyPasteCoords = new double[] { event.getX(), event.getY() };
          aContextMenu.show(drawPane, event.getScreenX(), event.getScreenY());
        }
        else if (tool == ToolEnum.SELECT || tool == ToolEnum.EDGE) { // Start
                                                                     // selecting
                                                                     // elements.
          selectController.onMousePressed(event);
        }
        else if ((tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE) && mouseCreationActivated) { // Start
                                                                                                                 // creation
                                                                                                                 // of
                                                                                                                 // package
                                                                                                                 // or
                                                                                                                 // class.
          mode = Mode.CREATING;
          createNodeController.onMousePressed(event);
        }
        else if (tool == ToolEnum.MOVE_SCENE) { // Start panning of graph.
          mode = Mode.MOVING;
          graphController.movePaneStart(event);
        }
        else if (tool == ToolEnum.DRAW && mouseCreationActivated) { // Start
                                                                    // drawing.
          mode = Mode.DRAWING;
          sketchController.onTouchPressed(event);
        }
        
      }
      else if (mode == Mode.CONTEXT_MENU) {
        if (event.getButton() == MouseButton.SECONDARY) {
          copyPasteController.copyPasteCoords = new double[] { event.getX(), event.getY() };
          aContextMenu.show(drawPane, event.getScreenX(), event.getScreenY());
        }
        else {
          aContextMenu.hide();
        }
      }
      event.consume();
    });
    
    drawPane.setOnMouseDragged(event -> {
      if (tool == ToolEnum.SELECT && mode == Mode.SELECTING) { // Continue
                                                               // selection of
                                                               // elements.
        selectController.onMouseDragged(event);
      }
      else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING && mouseCreationActivated) { // Continue
                                                                                          // drawing.
        sketchController.onTouchMoved(event);
      }
      else if ((tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE) && mode == Mode.CREATING && mouseCreationActivated) { // Continue
                                                                                                                                        // creation
                                                                                                                                        // of
                                                                                                                                        // class
                                                                                                                                        // or
                                                                                                                                        // package.
        createNodeController.onMouseDragged(event);
      }
      else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) { // Continue
                                                                     // panning
                                                                     // of
                                                                     // graph.
        graphController.movePane(event);
      }
      event.consume();
    });
    
    drawPane.setOnMouseReleased(event -> {
      if (tool == ToolEnum.SELECT && mode == Mode.SELECTING) { // Finish
                                                               // selecting
                                                               // elements.
        selectController.onMouseReleased();
      }
      else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING && mouseCreationActivated) { // Finish
                                                                                          // drawing.
        sketchController.onTouchReleased(event);
        // We only want to move out of drawing mode if there are no other
        // current drawings.
        if (!sketchController.currentlyDrawing()) {
          mode = Mode.NO_MODE;
        }
      }
      else if (tool == ToolEnum.CREATE_CLASS && mode == Mode.CREATING && mouseCreationActivated) { // Finish
                                                                                                   // creation
                                                                                                   // of
                                                                                                   // class.
        createNodeController.onMouseReleasedClass();
        if (!createNodeController.currentlyCreating()) {
          mode = Mode.NO_MODE;
        }
        
      }
      else if (tool == ToolEnum.CREATE_PACKAGE && mode == Mode.CREATING && mouseCreationActivated) { // Finish
                                                                                                     // creation
                                                                                                     // of
                                                                                                     // package.
        createNodeController.onMouseReleasedPackage();
        if (!createNodeController.currentlyCreating()) {
          mode = Mode.NO_MODE;
        }
      }
      else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) { // Finish
                                                                     // panning
                                                                     // of
                                                                     // graph.
        graphController.movePaneFinished();
        mode = Mode.NO_MODE;
      }
    });
    
    // ------------------------- Touch ---------------------------------
    // There are specific events for touch when creating and drawing to utilize
    // multitouch. //todo edge creation multi-user support.
    drawPane.setOnTouchPressed(event -> {
      if ((tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE) && !mouseCreationActivated) {
        mode = Mode.CREATING;
        createNodeController.onTouchPressed(event);
      }
      else if (tool == ToolEnum.DRAW && !mouseCreationActivated) {
        mode = Mode.DRAWING;
        sketchController.onTouchPressed(event);
      }
    });
    
    drawPane.setOnTouchMoved(event -> {
      if ((tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE) && mode == Mode.CREATING && !mouseCreationActivated) {
        createNodeController.onTouchDragged(event);
      }
      else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING && !mouseCreationActivated) {
        sketchController.onTouchMoved(event);
      }
      event.consume();
    });
    
    drawPane.setOnTouchReleased(event -> {
      if (tool == ToolEnum.CREATE_CLASS && mode == Mode.CREATING && !mouseCreationActivated) {
        createNodeController.onTouchReleasedClass(event);
        if (!createNodeController.currentlyCreating()) {
          mode = Mode.NO_MODE;
        }
        
      }
      else if (tool == ToolEnum.CREATE_PACKAGE && mode == Mode.CREATING && !mouseCreationActivated) {
        createNodeController.onTouchReleasedPackage(event);
        if (!createNodeController.currentlyCreating()) {
          mode = Mode.NO_MODE;
        }
      }
      else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING && !mouseCreationActivated) {
        sketchController.onTouchReleased(event);
        if (!sketchController.currentlyDrawing()) {
          mode = Mode.NO_MODE;
        }
      }
      event.consume();
    });
  }
  
  boolean wasAlreadySelected = false;
  
  @Override
  void initNodeActions(AbstractNodeView nodeView) {
    nodeView.setOnMousePressed(event -> {
      if (event.getClickCount() == 2) { // Open dialog window on double click.
        nodeController.onDoubleClick(nodeView);
        tool = ToolEnum.SELECT;
        setButtonClicked(selectBtn);
      }
      else if (tool == ToolEnum.MOVE_SCENE) { // Start panning of graph.
        mode = Mode.MOVING;
        graphController.movePaneStart(event);
        event.consume();
      }
      else if (event.getButton() == MouseButton.SECONDARY) { // Open context
                                                             // menu on left
                                                             // click.
        copyPasteController.copyPasteCoords = new double[] { nodeView.getX() + event.getX(), nodeView.getY() + event.getY() };
        aContextMenu.show(nodeView, event.getScreenX(), event.getScreenY());
      }
      else if (tool == ToolEnum.SELECT || tool == ToolEnum.CREATE_CLASS) { // Select
                                                                           // node
        setTool(ToolEnum.SELECT);
        setButtonClicked(selectBtn);
        if (!(nodeView instanceof PackageNodeView)) {
          nodeView.toFront();
        }
        if (mode == Mode.NO_MODE) { // Either drag selected elements or resize
                                    // node.
          Point2D.Double eventPoint = new Point2D.Double(event.getX(), event.getY());
          if (eventPoint.distance(new Point2D.Double(nodeView.getWidth(), nodeView.getHeight())) < 20) { // Resize
                                                                                                         // if
                                                                                                         // event
                                                                                                         // is
                                                                                                         // close
                                                                                                         // to
                                                                                                         // corner
                                                                                                         // of
                                                                                                         // node
            mode = Mode.RESIZING;
            nodeController.resizeStart(nodeView);
          }
          else {
            mode = Mode.DRAGGING;
            if (!selectedNodes.contains(nodeView)) { // Drag
              wasAlreadySelected = false;
              selectedNodes.add(nodeView);
            }
            else {
              wasAlreadySelected = true;
            }
            drawSelected();
            nodeController.moveNodesStart(event);
            sketchController.moveSketchStart(event);
          }
        }
      }
      else if (tool == ToolEnum.EDGE) { // Start edge creation.
        mode = Mode.CREATING;
        edgeController.onMousePressedOnNode(event);
      }
      event.consume();
    });
    
    nodeView.setOnMouseDragged(event -> {
      if ((tool == ToolEnum.SELECT || tool == ToolEnum.CREATE_CLASS) && mode == Mode.DRAGGING) { // Continue
                                                                                                 // dragging
                                                                                                 // selected
                                                                                                 // elements
        nodeController.moveNodes(event);
        sketchController.moveSketches(event);
      }
      else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) { // Continue
                                                                     // panning
                                                                     // graph.
        graphController.movePane(event);
      }
      else if ((tool == ToolEnum.SELECT || tool == ToolEnum.CREATE_CLASS) && mode == Mode.RESIZING) { // Continue
                                                                                                      // resizing
                                                                                                      // node.
        nodeController.resize(event);
      }
      else if (tool == ToolEnum.EDGE && mode == Mode.CREATING) { // Continue
                                                                 // creating
                                                                 // edge.
        edgeController.onMouseDragged(event);
      }
      event.consume();
      
    });
    
    nodeView.setOnMouseReleased(event -> {
      if ((tool == ToolEnum.SELECT || tool == ToolEnum.CREATE_CLASS) && mode == Mode.DRAGGING) { // Finish
                                                                                                 // dragging
                                                                                                 // nodes
                                                                                                 // and
                                                                                                 // create
                                                                                                 // a
                                                                                                 // compound
                                                                                                 // command.
        double[] deltaTranslateVector = nodeController.moveNodesFinished(event);
        sketchController.moveSketchFinished(event);
        if (deltaTranslateVector[0] != 0 || deltaTranslateVector[1] != 0) { // If
                                                                            // it
                                                                            // was
                                                                            // actually
                                                                            // moved
          CompoundCommand compoundCommand = new CompoundCommand();
          for (AbstractNodeView movedView : selectedNodes) {
            compoundCommand.add(new MoveGraphElementCommand(nodeMap.get(movedView), deltaTranslateVector[0], deltaTranslateVector[1]));
          }
          for (Sketch sketch : selectedSketches) {
            compoundCommand.add(new MoveGraphElementCommand(sketch, deltaTranslateVector[0], deltaTranslateVector[1]));
          }
          undoManager.add(compoundCommand);
        }
        else {
          if (wasAlreadySelected) {
            selectedNodes.remove(nodeView);
          }
          drawSelected();
        }
      }
      else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) { // Finish
                                                                     // panning
                                                                     // of
                                                                     // graph.
        graphController.movePaneFinished();
        mode = Mode.NO_MODE;
      }
      else if ((tool == ToolEnum.SELECT || tool == ToolEnum.CREATE_CLASS) && mode == Mode.RESIZING) { // Finish
                                                                                                      // resizing
                                                                                                      // node.
        nodeController.resizeFinished(nodeMap.get(nodeView));
      }
      else if (tool == ToolEnum.EDGE && mode == Mode.CREATING) { // Finish
                                                                 // creation of
                                                                 // edge.
        edgeController.onMouseReleasedRelation();
      }
      mode = Mode.NO_MODE;
      event.consume();
    });
    
    ////////////////////////////////////////////////////////////////
    
    nodeView.setOnTouchPressed(event -> {
      if (nodeView instanceof PackageNodeView && (tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE)) {
        mode = Mode.CREATING;
        createNodeController.onTouchPressed(event);
      }
      else if (tool == ToolEnum.DRAW) {
        mode = Mode.DRAWING;
        sketchController.onTouchPressed(event);
      }
      event.consume();
    });
    
    nodeView.setOnTouchMoved(event -> {
      if (nodeView instanceof PackageNodeView && (tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE) && mode == Mode.CREATING) {
        createNodeController.onTouchDragged(event);
      }
      else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING) {
        sketchController.onTouchMoved(event);
      }
      event.consume();
      
    });
    
    nodeView.setOnTouchReleased(event -> {
      if (nodeView instanceof PackageNodeView && tool == ToolEnum.CREATE_CLASS && mode == Mode.CREATING) {
        createNodeController.onTouchReleasedClass(event);
        if (!createNodeController.currentlyCreating()) {
          mode = Mode.NO_MODE;
        }
        
      }
      else if (nodeView instanceof PackageNodeView && tool == ToolEnum.CREATE_PACKAGE && mode == Mode.CREATING) {
        createNodeController.onTouchReleasedPackage(event);
        if (!createNodeController.currentlyCreating()) {
          mode = Mode.NO_MODE;
        }
      }
      else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING) {
        sketchController.onTouchReleased(event);
        if (!sketchController.currentlyDrawing()) {
          mode = Mode.NO_MODE;
        }
      }
      else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) {
        mode = Mode.NO_MODE;
      }
      event.consume();
    });
  }
  
  // Init Buttons
  public void initToolBarActions() {
    
    Image image = new Image("/icons/classw.png");
    createBtn.setGraphic(new ImageView(image));
    createBtn.setText("");
    image = new Image("/icons/packagew.png");
    packageBtn.setGraphic(new ImageView(image));
    packageBtn.setText("");
    image = new Image("/icons/edgew.png");
    edgeBtn.setGraphic(new ImageView(image));
    edgeBtn.setText("");
    image = new Image("/icons/selectw.png");
    selectBtn.setGraphic(new ImageView(image));
    selectBtn.setText("");
    image = new Image("/icons/undow.png");
    undoBtn.setGraphic(new ImageView(image));
    undoBtn.setText("");
    image = new Image("/icons/redow.png");
    redoBtn.setGraphic(new ImageView(image));
    redoBtn.setText("");
    image = new Image("/icons/movew.png");
    moveBtn.setGraphic(new ImageView(image));
    moveBtn.setText("");
    image = new Image("/icons/deletew.png");
    deleteBtn.setGraphic(new ImageView(image));
    deleteBtn.setText("");
    image = new Image("/icons/draww.png");
    drawBtn.setGraphic(new ImageView(image));
    drawBtn.setText("");
    image = new Image("/icons/recow.png");
    recognizeBtn.setGraphic(new ImageView(image));
    recognizeBtn.setText("");
    image = new Image("/icons/micw.png");
    voiceBtn.setGraphic(new ImageView(image));
    voiceBtn.setText("");
    image = new Image("/icons/validatew.png");
    validateBtn.setGraphic(new ImageView(image));
    validateBtn.setText("");
    image = new Image("/icons/generatew.png");
    generateBtn.setGraphic(new ImageView(image));
    generateBtn.setText("");
    image = new Image("/icons/editinfow.png");
    editInfoBtn.setGraphic(new ImageView(image));
    editInfoBtn.setText("");
    image = new Image("/icons/showerrorlogw.png");
    showErrorLogBtn.setGraphic(new ImageView(image));
    showErrorLogBtn.setText("");
    image = new Image("/icons/showcodew.png");
    showCodeBtn.setGraphic(new ImageView(image));
    showCodeBtn.setText("");
    buttonInUse = createBtn;
    buttonInUse.getStyleClass().add("button-in-use"); //
    // ---------------------- Actions for buttons ----------------------------
    createBtn.setOnAction(event -> {
      tool = ToolEnum.CREATE_CLASS;
      setButtonClicked(createBtn);
    });
    packageBtn.setOnAction(event -> {
      tool = ToolEnum.CREATE_PACKAGE;
      setButtonClicked(packageBtn);
    });
    edgeBtn.setOnAction(event -> {
      tool = ToolEnum.EDGE;
      setButtonClicked(edgeBtn);
    });
    selectBtn.setOnAction(event -> {
      tool = ToolEnum.SELECT;
      setButtonClicked(selectBtn);
    });
    drawBtn.setOnAction(event -> {
      tool = ToolEnum.DRAW;
      setButtonClicked(drawBtn);
    });
    moveBtn.setOnAction(event -> {
      setButtonClicked(moveBtn);
      tool = ToolEnum.MOVE_SCENE;
    });
    undoBtn.setOnAction(event -> undoManager.undoCommand());
    redoBtn.setOnAction(event -> undoManager.redoCommand());
    deleteBtn.setOnAction(event -> deleteSelected());
    recognizeBtn.setOnAction(event -> {
      checkSketchErrors(getGraphModel(), getNodeMap());
      recognizeController.recognize(selectedSketches);
      if (errorCounter == 0) {
        validateBtn.setDisable(false);
        Notifications.create().title("Recognization").text("Recognization of the graph successfull.").showInformation();
      }
    });
    voiceBtn.setOnAction(event -> {
      if (voiceController.voiceEnabled) {
        Notifications.create().title("Voice disabled").text("Voice commands are now disabled.").showInformation();
      }
      else {
        Notifications.create().title("Voice enabled").text("Voice commands are now enabled.").showInformation();
      }
      voiceController.onVoiceButtonClick();
    });
    
    editInfoBtn.setOnAction(event -> {
      initContainerInfo();
    });
    
    showErrorLogBtn.setOnAction(event -> {
      showErrorLog();
    });
    
    validateBtn.setOnAction(event -> {
      if (errorCounter == 0) {
        List<String> containerInfo = new ArrayList<>();
        containerInfo.add(packageName);
        containerInfo.add(imports);
        containerInfo.add(modelName);
        unit = plugin.shapeToAST(getGraphModel(), containerInfo);
        IndentPrinter i = new IndentPrinter();
        CDPrettyPrinterConcreteVisitor prettyprinter = new CDPrettyPrinterConcreteVisitor(i);
        
        try {
          String path = plugin.getUsageFolderPath() + "/src/main/resources/classdiagram/";
          FileUtils.writeStringToFile(new File(path + modelName + ".cd"), prettyprinter.prettyprint(unit));
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
      
      // remove existing corresponding errors to check if they were corrected
      for (MontiCoreException ex : errorLog.getAllLogs()) {
        if (ex instanceof AssocLeftRefNameMissingException) {
          errorLog.getAllLogs().remove(ex);
        }
        if (ex instanceof AssocRightRefNameMissingException) {
          errorLog.getAllLogs().remove(ex);
        }
        if (ex instanceof ClassAttributeNameMissingException) {
          errorLog.getAllLogs().remove(ex);
        }
        if (ex instanceof ClassAttributeTypeMissingException) {
          errorLog.getAllLogs().remove(ex);
        }
        if (ex instanceof ClassNameMissingException) {
          errorLog.getAllLogs().remove(ex);
        }
        if (ex instanceof ConstructorNameMissingException) {
          errorLog.getAllLogs().remove(ex);
        }
        if (ex instanceof EnumConstantNameMissingException) {
          errorLog.getAllLogs().remove(ex);
        }
        if (ex instanceof EnumNameMissingException) {
          errorLog.getAllLogs().remove(ex);
        }
        if (ex instanceof InterfaceNameMissingException) {
          errorLog.getAllLogs().remove(ex);
        }
        if (ex instanceof MethodNameMissingException) {
          errorLog.getAllLogs().remove(ex);
        }
        if (ex instanceof MethodParameterNameMissingException) {
          errorLog.getAllLogs().remove(ex);
        }
        if (ex instanceof MethodParameterTypeMissingException) {
          errorLog.getAllLogs().remove(ex);
        }
        if (ex instanceof MethodReturnTypeMissingException) {
          errorLog.getAllLogs().remove(ex);
        }
      }
      
      List<MontiCoreException> astErrorList = plugin.check(unit, getNodeMap());
      for (MontiCoreException ex : astErrorList) {
        errorLog.addLog(ex);
      }
      errorCounter = errorLog.getAllLogs().size();
      showErrorLogBtn.setText("(" + errorCounter + ")");
      
      if (errorCounter == 0) {
        generateBtn.setDisable(false);
        Notifications.create().title("Validation").text("Validation of the graph successfull.").showInformation();
      }
      
    });
    
    generateBtn.setOnAction(event -> {
      boolean generateSuccess = plugin.generateCode(unit, plugin.getUsageFolderPath() + "/src/main/java/");
      if (generateSuccess) {
        Notifications.create().title("Code Generator").text("Code generation was successfull.").showInformation();
      }
      else {
        errorLog.addLog(new CodeGenerationException(null, null));
      }
    });
    
    showCodeBtn.setOnAction(event -> {
      if(selectedNodes.size() > 0) {
        for(AbstractNodeView view : selectedNodes) {
          PopOver pop = new PopOver();
          String fileTitle = view.getRefNode().getTitle();
          String folder = plugin.getUsageFolderPath();
          
          String filename = folder + "/src/main/java/" + fileTitle + ".java";
          File file = new File(filename);
          if(file.exists()) {
            VBox box = new VBox();
            try {
              List<String> allLines = Files.readAllLines(Paths.get(filename));
              String code = "";
              for(String line : allLines) {
                code += line + "\n";
              }
              TextArea textField = new TextArea(code);
              Button saveBtn = new Button("Save");
              saveBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override 
                public void handle(ActionEvent e) {
                  try {
                    FileUtils.writeStringToFile(new File(filename), textField.getText());
                    Notifications.create().title("Code Display").text("Code file was saved.").showInformation();
                  }
                  catch (IOException e1) {
                    e1.printStackTrace();
                  }
                }
              });
              box.getChildren().add(textField);
              box.getChildren().add(saveBtn);
            }
            catch (IOException e) {
              e.printStackTrace();
            }
            pop.setContentNode(box);
          } else {
            Label label = new Label("No generated code available.");
            pop.setContentNode(label);
          }
          
          //pop.setContentNode();
          
          pop.show(view);
        }
      } else {
        Notifications.create().title("Code Display").text("No Node was selected.").showInformation();
      }
      
    });
  }
  
  private void checkSketchErrors(Graph g, HashMap<AbstractNodeView, AbstractNode> map) {
    // remove all existing corresponding exceptions for checking if they are
    // corrected
    for (MontiCoreException ex : errorLog.getAllLogs()) {
      if (ex instanceof PackageNameMissingException) {
        errorLog.getAllLogs().remove(ex);
      }
      if (ex instanceof ClassDiagramNameMissingException) {
        errorLog.getAllLogs().remove(ex);
      }
      if (ex instanceof NoMultiplicityOnAssociationException) {
        errorLog.getAllLogs().remove(ex);
      }
      if (ex instanceof NodeNameMissingException) {
        errorLog.getAllLogs().remove(ex);
      }
    }
    for (AbstractNode n : g.getAllNodes()) {
      if (n.getTitle() == null) {
        errorLog.addLog(new NodeNameMissingException(n, getCorrespondingNodeView(n)));
      }
    }
    for (Edge e : g.getAllEdges()) {
      AbstractEdge abstrEdge = (AbstractEdge) e;
      if (!(abstrEdge instanceof InheritanceEdge)) {
        if (abstrEdge.getStartMultiplicity() == null) {
          errorLog.addLog(new NoMultiplicityOnAssociationException(abstrEdge.getStartNode(), getCorrespondingNodeView(abstrEdge.getStartNode())));
        }
        if (abstrEdge.getEndMultiplicity() == null) {
          errorLog.addLog(new NoMultiplicityOnAssociationException(abstrEdge.getEndNode(), getCorrespondingNodeView(abstrEdge.getEndNode())));
        }
      }
    }
    if (packageName == null) {
      errorLog.addLog(new PackageNameMissingException(null, null));
    }
    if (modelName == null) {
      errorLog.addLog(new ClassDiagramNameMissingException(null, null));
    }
    errorCounter = errorLog.getAllLogs().size();
    showErrorLogBtn.setText("(" + errorCounter + ")");
  }
  
  private void showErrorLog() {
    PopOver pop = new PopOver();
    if (errorLog.getAllLogs().size() > 0) {
      VBox box = new VBox();
      for (MontiCoreException ex : errorLog.getAllLogs()) {
        box.getChildren().add(ex.getContentPane());
      }
      pop.setContentNode(box);
    }
    
    pop.show(showErrorLogBtn);
  }
  
  private AbstractNodeView getCorrespondingNodeView(Node n) {
    AbstractNodeView view = null;
    for (Entry<AbstractNodeView, AbstractNode> entry : getNodeMap().entrySet()) {
      if (n.equals(entry.getValue())) {
        view = entry.getKey();
      }
    }
    return view;
  }
  
  @FXML
  public void testload() {
    System.out.println("Loading is working... :D");
  }
  
}
