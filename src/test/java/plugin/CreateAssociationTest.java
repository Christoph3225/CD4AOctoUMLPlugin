package plugin;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.types._ast.ASTQualifiedName;
import de.monticore.types.types._ast.TypesNodeFactory;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDAssociation;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDQualifier;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCardinality;
import de.monticore.umlcd4a.cd4analysis._ast.ASTModifier;
import de.monticore.umlcd4a.cd4analysis._ast.ASTStereotype;
import de.monticore.umlcd4a.cd4analysis._ast.CD4AnalysisNodeFactory;
import de.monticore.umlcd4a.prettyprint.CDPrettyPrinterConcreteVisitor;
import model.Graph;
import model.edges.AssociationEdge;
import model.edges.AbstractEdge.Direction;
import model.nodes.ClassNode;

public class CreateAssociationTest {
  private CD4APlugin plugin;
  private Graph graph;
  private String modelname = "createassoccd";
  
  @Before
  public void initTest() {
    plugin = new CD4APlugin();
  }
  
  @Test
  public void createUnspecifiedAssocTest() {
    // create test object
    graph = new Graph();
    ClassNode node1 = new ClassNode();
    ClassNode node2 = new ClassNode();
    node1.setTitle("Student");
    node2.setTitle("Vorlesung");
    AssociationEdge edge = new AssociationEdge();
    edge.setStartNode(node1);
    edge.setEndNode(node2);
    edge.setStartMultiplicity("0..1");
    edge.setEndMultiplicity("0..1");
    edge.setDirection(Direction.NO_DIRECTION);
    edge.setLabel("besucht");
    graph.addNode(node1, false);
    graph.addNode(node2, false);
    graph.addEdge(edge, false);
    
    ASTCDCompilationUnit unit = plugin.shapeToAST(graph, modelname);
    IndentPrinter i = new IndentPrinter();
    CDPrettyPrinterConcreteVisitor prettyprinter = new CDPrettyPrinterConcreteVisitor(i);
    System.out.println(prettyprinter.prettyprint(unit));
    
    // create test result
    ASTStereotype stereotype = null;
    String name = "besucht";
    ASTModifier leftModifier = CD4AnalysisNodeFactory.createASTModifier();
    ASTCardinality leftCardinality = CD4AnalysisNodeFactory.createASTCardinality(false, false, false, true);;
    
    String leftNodeName = "Student";
    String rightNodeName = "Vorlesung";
    List<String> leftNameList = new ArrayList<>();
    List<String> rightNameList = new ArrayList<>();
    leftNameList.add(leftNodeName);
    rightNameList.add(rightNodeName);
    
    ASTQualifiedName leftReferenceName = TypesNodeFactory.createASTQualifiedName(leftNameList);
    ASTCDQualifier leftQualifier = null;
    
    // roles are not supported by OctoUML!
    String leftRole = "";
    String rightRole = "";
    
    // qualifier are not supported by OctoUML!
    ASTCDQualifier rightQualifier = null;
    
    ASTQualifiedName rightReferenceName = TypesNodeFactory.createASTQualifiedName(rightNameList);
    
    ASTCardinality rightCardinality = CD4AnalysisNodeFactory.createASTCardinality(false, false, false, true);;
    ASTModifier rightModifier = CD4AnalysisNodeFactory.createASTModifier();
    boolean r__association = true;
    boolean r__composition = false;
    boolean r__derived = false;
    boolean leftToRight = false;
    boolean rightToLeft = false;
    boolean bidirectional = false;
    boolean unspecified = true;
    
    ASTCDAssociation assoc = CD4AnalysisNodeFactory.createASTCDAssociation(stereotype, name, leftModifier, leftCardinality, leftReferenceName, leftQualifier, leftRole, rightRole, rightQualifier, rightReferenceName, rightCardinality, rightModifier, r__association, r__composition, r__derived, leftToRight, rightToLeft, bidirectional, unspecified);
    
    System.out.println(prettyprinter.prettyprint(assoc));
    
    // compare associations against each other
    
    ASTCDAssociation transformedAssoc = unit.getCDDefinition().getCDAssociations().get(0);
    assert(assoc.deepEquals(transformedAssoc));
  }
  
  @Test
  public void createDirectionAssocTest() {
    
  }
  
  @Test
  public void createUnspecifiedCompoTest() {
    
  }
  
  @Test
  public void createDirectionCompoTest() {
    
  }
}
