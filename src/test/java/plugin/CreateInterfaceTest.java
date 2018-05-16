package plugin;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.monticore.types.types._ast.ASTReferenceType;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDAttribute;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDInterface;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDMethod;
import de.monticore.umlcd4a.cd4analysis._ast.ASTModifier;
import de.monticore.umlcd4a.cd4analysis._ast.CD4AnalysisNodeFactory;
import model.Graph;
import model.nodes.ClassNode;

public class CreateInterfaceTest {
  private CD4APlugin plugin;
  private Graph graph;
  private String modelname = "createinterfacecd";
  
  @Before
  public void initTest() {
    plugin = new CD4APlugin();
  }
  
  @Test
  public void createSingleInterfaceWithNameTest() {
    graph = new Graph();
    // create test object
    ClassNode node = new ClassNode();
    node.setTitle("<<interface>> Student");
    graph.addNode(node, false);
    
    ASTCDCompilationUnit unit = plugin.shapeToAST(graph, modelname);
    
    // create test result
    ASTModifier modifier = CD4AnalysisNodeFactory.createASTModifier(null, false, false, false, false, false, true, false);
    String interfName = " Student";
    List<ASTReferenceType> interfaces = new ArrayList<>();
    List<ASTCDAttribute> interfAttributes = new ArrayList<>();
    List<ASTCDMethod> interfMethods = new ArrayList<>();
    
    // test interfaces against each other
    ASTCDInterface interf = CD4AnalysisNodeFactory.createASTCDInterface(modifier, interfName, interfaces, interfAttributes, interfMethods);
    
    ASTCDInterface transformedInterf = unit.getCDDefinition().getCDInterfaces().get(0);
    
    assert(interf.deepEquals(transformedInterf));
  }
  
  @Test
  public void createSingleInterfaceWithAttributeTest() {
    
  }
  
}
