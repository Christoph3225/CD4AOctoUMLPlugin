package plugin;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.monticore.types.types._ast.ASTReferenceType;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDConstructor;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDEnum;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDEnumConstant;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDMethod;
import de.monticore.umlcd4a.cd4analysis._ast.ASTModifier;
import de.monticore.umlcd4a.cd4analysis._ast.CD4AnalysisNodeFactory;
import model.Graph;
import model.nodes.ClassNode;

public class CreateEnumTest {
  private CD4APlugin plugin;
  private Graph graph;
  private String modelname = "createenumcd";
  
  @Before
  public void initTest() {
    plugin = new CD4APlugin();
  }
  
  @Test
  public void createSingleEnumWithNameTest() {
    graph = new Graph();
    // create test object
    ClassNode node = new ClassNode();
    node.setTitle("<<enum>> AGE");
    graph.addNode(node, false);
    
    ASTCDCompilationUnit unit = plugin.shapeToAST(graph, modelname);
    
    // create test result
    ASTModifier modifier = CD4AnalysisNodeFactory.createASTModifier(null, false, false, false, false, false, true, false);
    String enumName = " AGE";
    List<ASTReferenceType> interfaces = new ArrayList<>();
    List<ASTCDEnumConstant> enumConstants = new ArrayList<>();
    List<ASTCDConstructor> enumConstructors = new ArrayList<>();
    List<ASTCDMethod> enumMethods = new ArrayList<>();
    
    // test interfaces against each other
    ASTCDEnum enom = CD4AnalysisNodeFactory.createASTCDEnum(modifier, enumName, interfaces, enumConstants, enumConstructors, enumMethods);
    
    ASTCDEnum transformedEnum = unit.getCDDefinition().getCDEnums().get(0);
    
    assert(enom.deepEquals(transformedEnum));
  }
}
