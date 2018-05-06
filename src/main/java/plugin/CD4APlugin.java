package plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.controlsfx.control.Notifications;

import controller.AbstractDiagramController;
import controller.CD4AController;
import de.monticore.ast.ASTNode;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.types._ast.*;
import de.monticore.umlcd4a.cd4analysis._ast.*;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Graph;
import model.nodes.AbstractNode;
import model.nodes.ClassNode;
import model.nodes.PackageNode;

public class CD4APlugin implements MontiCorePlugIn {

  private CD4AnalysisNodeFactory cd4aFactory;
  private TypesNodeFactory typesFactory;
  private ASTCDCompilationUnit cdCompUnit;
  
	@Override
	public AbstractDiagramController getController() {
		try {
			Class<?> c = getClass().getClassLoader().loadClass("controller.CD4AController");
			CD4AController adc = (CD4AController) c.newInstance();
			return adc;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getView() {
		return "view/fxml/cd4aView.fxml";
	}
	
	@Override
	public String getDSLName() {
		return "CD4Analysis";
	}

	@Override
	public String getFileEnding() {
		return ".cd";
	}

	@Override
	public String getDSLPicture() {
		// nothing to return because class diagrams are supported by OctoUML
		return null;
	}

	@Override
	public IndentPrinter getPrettyPrinter() {
	  //TODO
	  //IndentPrinter i = new IndentPrinter();
    //CDPrettyPrinterConcreteVisitor prettyprinter = new CDPrettyPrinterConcreteVisitor(i);
		return null;
	}

	@Override
	public Class getASTNode() {
	  //TODO
		return null;
	}

	@SuppressWarnings("static-access")
  @Override
	public ASTCDCompilationUnit shapeToAST(Graph graph) {
	  // create AST via factory
	  
	  // elements for creating ASTCDCompilationUnit
	  List<String> r__packages = new ArrayList<>();
	  List<ASTImportStatement> imports = new ArrayList<>();
	  r__packages.add("cd4aplugin");
	  
	  // elements for creating ASTCDDefinition
	  //TODO overwrite with getted from input dialog
	  String cdName = "MalSehenObEsKlappt";
	  List<ASTCDClass> clazzes = new ArrayList<>();
	  List<ASTCDInterface> interfazes = new ArrayList<>();
	  List<ASTCDEnum> enoms = new ArrayList<>();
	  List<ASTCDAssociation> assocs = new ArrayList<>();
	  
	  // create classes, interfaces, enums of classdiagram
	  for(AbstractNode cNode : graph.getAllNodes()) {
      if(cNode instanceof ClassNode) {
        String clazzName = cNode.getTitle();
        if(clazzName.contains("<<interface>>")) {
          //cNode is an interface
          ASTCDInterface interf = createASTInterface((ClassNode) cNode, clazzName);
          interfazes.add(interf);
          
        } else if(clazzName.contains("<<abstract>>")) {
          //cNode is an abstract class
          ASTCDClass clazz = createASTClass((ClassNode) cNode, clazzName);
          clazzes.add(clazz);
          
        } else if(clazzName.contains("<<enum>>")) {
          //cNode is an enum
          ASTCDEnum enom = createASTEnum((ClassNode) cNode, clazzName);
          enoms.add(enom);
          
        } else {
          //cNode is a class
          ASTCDClass clazz = createASTClass((ClassNode) cNode, clazzName);
          clazzes.add(clazz);
        }
      }
      if(cNode instanceof PackageNode) {
        //TODO what do here?
      }
    }
	    
	  ASTCDDefinition cdDef = cd4aFactory.createASTCDDefinition(cdName, clazzes, interfazes, enoms, assocs);
    ASTCDCompilationUnit unit = cd4aFactory.createASTCDCompilationUnit(r__packages, imports, cdDef);
	  
	  cdCompUnit = unit;
	  
		return unit;
	}
	
	private ASTCDInterface createASTInterface(ClassNode node, String name) {
	  return null;
	}
	
	private ASTCDEnum createASTEnum(ClassNode node, String name) {
    return null;
  }
	
	@SuppressWarnings("static-access")
  private ASTCDClass createASTClass(ClassNode node, String name) {
	  
	  ASTStereotype stereotype = cd4aFactory.createASTStereotype();
	  
	  ASTModifier clazzModifier = cd4aFactory.createASTModifier(stereotype, false, false, false, false, false, true, false);
    String clazzName = name;
    //TODO superclasses bekommen
    List<String> superClassNames = new ArrayList<>();
    ASTReferenceType clazzSuperclass = typesFactory.createASTSimpleReferenceType(superClassNames, null);
    List<ASTReferenceType> clazzInterfaces = new ArrayList<>();
    List<ASTCDAttribute> clazzAttributes = new ArrayList<>();
    List<ASTCDConstructor> clazzConstructors = new ArrayList<>();
    List<ASTCDMethod> clazzMethods = new ArrayList<>();
    
    /*String nodeAttributes = node.getAttributes();
    String[] arr = nodeAttributes.split(";");
    for(String s : arr){
        String[] arr2 = s.split("\\s");
        String attrType = arr2[0];
        String attrName = arr2[1];
        
        ASTCDAttribute cdAttr = cd4aFactory.createASTCDAttribute(clazzModifier, null, attrName, null);
        clazzAttributes.add(cdAttr);
    }*/
    //TODO interfaces, attributes, constructors, methods speichern
    
    
    ASTCDClass retClass = cd4aFactory.createASTCDClass(clazzModifier, clazzName, clazzSuperclass, clazzInterfaces, clazzAttributes, clazzConstructors, clazzMethods);
    return retClass;
  }
	
	@Override
	public List<String> check(ASTNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGenerator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean generateCode(ASTNode node, String path) {
		// TODO Auto-generated method stub
		return false;
	}

  @Override
  public String getFlagName() {
    return "CD";
  }

  @Override
  public void addUMLFlag(String name) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<String> showContainerInfoDialog(Stage stage) {
    List<String> resList = new ArrayList<>();
    // for each info an input dialog
    TextInputDialog dialog = new TextInputDialog("");
    dialog.setTitle("Enter Classdiagram Name");
    dialog.setHeaderText("What is the name of your classdiagram?");
    dialog.setContentText("Please enter your name:");

    dialog.initOwner(stage);
    // Traditional way to get the response value.
    Optional<String> result = dialog.showAndWait();
    if (result.isPresent()){
        System.out.println("Your name: " + result.get());
        resList.add(result.get());
    }
    return resList;
  }

}
