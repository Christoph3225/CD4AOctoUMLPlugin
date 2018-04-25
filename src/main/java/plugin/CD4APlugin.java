package plugin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import controller.AbstractDiagramController;
import controller.CD4AController;
import de.monticore.ast.ASTNode;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symboltable.GlobalScope;

import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import model.Graph;
import model.edges.Edge;
import model.nodes.AbstractNode;
import model.nodes.ClassNode;
import model.nodes.PackageNode;

public class CD4APlugin implements MontiCorePlugIn {

  private GlobalScope globalScope = null;
  //private ASTCD4AnalysisNode transformedGraph; 
  
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
	public ASTCDCompilationUnit shapeToAST(Graph graph, String path) {
	  // (1) write graph to model file
	  graphToModel(graph, path, "test");
	  
	  // (2) resolve from model file ast
	  
	  /*Optional<CDSymbol> symbol;
    symbol = getGlobalScope().resolve("cd4aplugin.test", CDSymbol.KIND);
    Scope sc = getGlobalScope().getSubScopes().get(0);
    ASTCDCompilationUnit cu = (ASTCDCompilationUnit) sc.getAstNode().get();
	  */
		// TODO Auto-generated method stub
		return null;
	}
	
	/*public void setTransformedGraph(ASTCD4AnalysisNode node) {
	  this.transformedGraph = node;
	}*/
	
	public void graphToModel(Graph graph, String path, String targetName) {
	  FileWriter fw;
    try {
      fw = new FileWriter(path + "/cd4aplugin/" + targetName + ".cd");
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

}
