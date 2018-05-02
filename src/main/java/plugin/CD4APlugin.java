package plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import controller.AbstractDiagramController;
import controller.CD4AController;
import de.monticore.ast.ASTNode;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symboltable.GlobalScope;
import de.monticore.types.types._ast.*;
import de.monticore.umlcd4a.cd4analysis._ast.*;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Graph;
import model.edges.Edge;
import model.nodes.AbstractNode;
import model.nodes.ClassNode;
import model.nodes.PackageNode;

public class CD4APlugin implements MontiCorePlugIn {

  private GlobalScope globalScope = null;
  //private ASTCD4AnalysisNode transformedGraph; 
  private CD4AnalysisNodeFactory cd4aFactory;
  
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
	  //IndentPrinter i = new IndentPrinter();
    //CDPrettyPrinterConcreteVisitor prettyprinter = new CDPrettyPrinterConcreteVisitor(i);
		return null;
	}

	@Override
	public Class getASTNode() {
		return null;
	}

	@Override
	public ASTCDCompilationUnit shapeToAST(Graph graph) {
	  // (1) write graph to model file
	  //graphToModel(graph, path, "TestCD");
	  
	  // (2) resolve from model file ast
	  
	  /*Optional<CDSymbol> symbol;
    symbol = getGlobalScope().resolve("cd4aplugin.test", CDSymbol.KIND);
    Scope sc = getGlobalScope().getSubScopes().get(0);
    ASTCDCompilationUnit cu = (ASTCDCompilationUnit) sc.getAstNode().get();
	  */
		// TODO Auto-generated method stub
	 
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
	  
		return unit;
	}
	
	private ASTCDInterface createASTInterface(ClassNode node, String name) {
	  return null;
	}
	
	private ASTCDEnum createASTEnum(ClassNode node, String name) {
    return null;
  }
	
	private ASTCDClass createASTClass(ClassNode node, String name) {
	  
	  ASTStereotype stereotype = cd4aFactory.createASTStereotype();
	  
	  ASTModifier clazzModifier = cd4aFactory.createASTModifier(stereotype, false, false, false, false, false, true, false);
    String clazzName = name;
    ASTReferenceType clazzSuperclass = null;
    List<ASTReferenceType> clazzInterfaces = new ArrayList<>();
    List<ASTCDAttribute> clazzAttributes = new ArrayList<>();
    List<ASTCDConstructor> clazzConstructors = new ArrayList<>();
    List<ASTCDMethod> clazzMethods = new ArrayList<>();
    
    //TODO interfaces, attributes, constructors, methods speichern
    //TODO wie superclasses setzen
    
    ASTCDClass retClass = cd4aFactory.createASTCDClass(clazzModifier, clazzName, clazzSuperclass, clazzInterfaces, clazzAttributes, clazzConstructors, clazzMethods);
    return retClass;
  }
	
	
	
	/*public void setTransformedGraph(ASTCD4AnalysisNode node) {
	  this.transformedGraph = node;
	}*/
	
	public void graphToModel(Graph graph, String path, String targetName) {
	  FileWriter fw;
    try {
      File file = new File(path + "/cd4aplugin/" + targetName + ".cd");
      fw = new FileWriter(file);
      BufferedWriter bw = new BufferedWriter(fw);
      
      // write the package statement
      bw.write("package cd4aplugin;");
      bw.newLine();
      
      // write all import statements
      //nothing to do here
      
      // start here the new classdiagram
      bw.write("classdiagram " + targetName + "{");
      bw.newLine();
      
      // write all interfaces of the classdiagram
      for(AbstractNode cNode : graph.getAllNodes()) {
        if(cNode instanceof ClassNode) {
          String clazzName = cNode.getTitle();
          if(clazzName.contains("<<interface>>")) {
            //cNode is an interface
            bw.write("interface ");
            bw.write(cNode.getTitle());
            bw.write(" {");
            bw.newLine();
            if(((ClassNode) cNode).getAttributes() != null) {
              //TODO print attributes
            }
            bw.write("};");
            bw.newLine();
          } else if(clazzName.contains("<<abstract>>")) {
            //cNode is an abstract class
            bw.write("abstract class ");
            bw.write(cNode.getTitle());
            bw.write(" {");
            bw.newLine();
            if(((ClassNode) cNode).getAttributes() != null) {
              //TODO print attributes
            }
            bw.write("};");
            bw.newLine();
          } else if(clazzName.contains("<<enum>>")) {
            //cNode is an enum
            bw.write("enum ");
            bw.write(cNode.getTitle());
            bw.write(" {");
            bw.newLine();
            if(((ClassNode) cNode).getAttributes() != null) {
              //TODO print enum constants
            }
            bw.write("};");
            bw.newLine();
          } else {
            //cNode is a class
            //TODO vererbung und interfaces beachten!!!
            bw.write("class ");
            bw.write(cNode.getTitle());
            bw.write(" {");
            bw.newLine();
            if(((ClassNode) cNode).getAttributes() != null) {
              //TODO print attributes
            }
            bw.write("};");
            bw.newLine();
          }
        }
        if(cNode instanceof PackageNode) {
          //TODO what do here?
        }
      }
      
      for(Edge cEdge : graph.getAllEdges()) {
        //TODO
      }
      
      bw.newLine();
      bw.write("}");
      bw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
	}
	
	public GlobalScope getGlobalScope() {
    /*if (this.globalScope == null) {
      CD4AnalysisLanguage cdlang = new CD4AnalysisLanguage();
      
      ResolverConfiguration resolverConfig = new ResolverConfiguration();
      resolverConfig.addTopScopeResolvers(cdlang.getResolvers());
      
      ModelPath modelPath = new ModelPath(Paths.get("/"));
      
      this.globalScope = new GlobalScope(modelPath, cdlang, resolverConfig);
    }
    */
    return this.globalScope;
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
    Dialog<List<String>> dialog = new Dialog<>();
    dialog.setTitle("Container Info Dialog");
    dialog.setHeaderText(null);
    
    ButtonType okButtonType = new ButtonType("Start", ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().add(okButtonType);
    
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20,150,10,10));
    
    TextField modelNameTF = new TextField();
    
    grid.add(new Label("Class Diagram Name:"), 0,0);
    grid.add(modelNameTF, 1,0);
    
    dialog.setResultConverter(dialogButton -> {
      if(dialogButton == okButtonType) {
        List<String> res = new ArrayList<>();
        res.add(modelNameTF.getText());
        return res;
      }
      return null;
    });
    
    dialog.initOwner(stage);
    
    Optional<List<String>> diagResult = dialog.showAndWait(); 
    
    List<String> result = new ArrayList<>();
    diagResult.ifPresent(result::addAll);
    
    return result;
  }

}
