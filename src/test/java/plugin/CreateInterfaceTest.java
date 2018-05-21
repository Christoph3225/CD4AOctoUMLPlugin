package plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import de.monticore.types.types._ast.ASTQualifiedName;
import de.monticore.types.types._ast.ASTReferenceType;
import de.monticore.types.types._ast.ASTReturnType;
import de.monticore.types.types._ast.ASTType;
import de.monticore.types.types._ast.TypesNodeFactory;
import de.monticore.types.types._parser.TypesParser;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDAttribute;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDInterface;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDMethod;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDParameter;
import de.monticore.umlcd4a.cd4analysis._ast.ASTModifier;
import de.monticore.umlcd4a.cd4analysis._ast.CD4AnalysisNodeFactory;
import model.Graph;
import model.nodes.ClassNode;

public class CreateInterfaceTest {
  private CD4APlugin plugin;
  private Graph graph;
  private TypesParser typeParser;
  private String modelname = "createinterfacecd";
  
  @Before
  public void initTest() {
	  plugin = CD4APlugin.getInstance();
    typeParser = new TypesParser();
  }
  
  @Test
  public void createSingleInterfaceWithNameTest() {
    graph = new Graph();
    // create test object
    ClassNode node = new ClassNode();
    node.setTitle("<<interface>> Student");
    graph.addNode(node, false);
    
    String packageName = "cd4aplugin";
    String imports = "";
    List<String> containerInfo = new ArrayList<>();
    containerInfo.add(packageName);
    containerInfo.add(imports);
    containerInfo.add(modelname);
    
    ASTCDCompilationUnit unit = plugin.shapeToAST(graph, containerInfo);
    
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
    graph = new Graph();
    // create test object
    ClassNode node = new ClassNode();
    node.setTitle("<<interface>> Student");
    node.setAttributes("int age;");
    graph.addNode(node, false);
    
    String packageName = "cd4aplugin";
    String imports = "";
    List<String> containerInfo = new ArrayList<>();
    containerInfo.add(packageName);
    containerInfo.add(imports);
    containerInfo.add(modelname);
    
    ASTCDCompilationUnit unit = plugin.shapeToAST(graph, containerInfo);
    
    // create test result
    ASTModifier modifier = CD4AnalysisNodeFactory.createASTModifier(null, false, false, false, false, false, true, false);
    String interfName = " Student";
    List<ASTReferenceType> interfaces = new ArrayList<>();
    List<ASTCDAttribute> interfAttributes = new ArrayList<>();
    List<ASTCDMethod> interfMethods = new ArrayList<>();
    
    Optional<ASTType> type;
    ASTType typeOfAttr = null;
    try {
      type = typeParser.parse_String("int");
      typeOfAttr = type.get();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
     
    String attrName = "age";
    
    ASTCDAttribute cdAttr = CD4AnalysisNodeFactory.createASTCDAttribute(modifier, typeOfAttr, attrName, null);
    interfAttributes.add(cdAttr);
    
    // test interfaces against each other
    ASTCDInterface interf = CD4AnalysisNodeFactory.createASTCDInterface(modifier, interfName, interfaces, interfAttributes, interfMethods);
    
    ASTCDInterface transformedInterf = unit.getCDDefinition().getCDInterfaces().get(0);
    
    assert(interf.deepEquals(transformedInterf));
  }

  @Test
  public void createSingleInterfaceWithMethod() {
    graph = new Graph();
    // create test object
    ClassNode node = new ClassNode();
    node.setTitle("<<interface>> Student");
    node.setOperations("int getAge();");
    graph.addNode(node, false);
    
    String packageName = "cd4aplugin";
    String imports = "";
    List<String> containerInfo = new ArrayList<>();
    containerInfo.add(packageName);
    containerInfo.add(imports);
    containerInfo.add(modelname);
    
    ASTCDCompilationUnit unit = plugin.shapeToAST(graph, containerInfo);
    
    // create test result
    ASTModifier modifier = CD4AnalysisNodeFactory.createASTModifier(null, false, false, false, false, false, true, false);
    String interfName = " Student";
    List<ASTReferenceType> interfaces = new ArrayList<>();
    List<ASTCDAttribute> interfAttributes = new ArrayList<>();
    List<ASTCDMethod> interfMethods = new ArrayList<>();
    
    String m = "int getAge()";
    
    ASTReturnType returnType;
    String[] arr = m.split("\\(");
    String methodNameAndReturn = arr[0];
    String[] arr2 = methodNameAndReturn.split("\\s");
    String mRetTypeStr = arr2[0];
    String mName = arr2[1];
    
    if (mRetTypeStr.equals("void")) {
      returnType = TypesNodeFactory.createASTVoidType();
    }
    else {
      Optional<de.monticore.types.types._ast.ASTType> mRetTypeResult = null;
      try {
        mRetTypeResult = typeParser.parse_String(mRetTypeStr);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
      
      ASTType mRetType = mRetTypeResult.get();
      returnType = mRetType;
    }
    
    
    
    List<ASTCDParameter> methodParams = new ArrayList<>();
    List<ASTQualifiedName> methodExcepts = new ArrayList<>();
    String[] paramArr = m.split("\\(|\\)");
    if (paramArr.length > 1) {
      String strParam = paramArr[1];
      String[] paramAttrArr = strParam.split(",");
      for (String a : paramAttrArr) {
        String[] arr3 = a.split("\\s");
        String attrType = arr3[0];
        String attrName = arr3[1];
        
        Optional<de.monticore.types.types._ast.ASTType> typeResult = null;
        try {
          typeResult = typeParser.parse_String(attrType);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        
        ASTType typeOfAttr = typeResult.get();
        
        ASTCDParameter methodParam;
        if (a.contains("...")) {
          methodParam = CD4AnalysisNodeFactory.createASTCDParameter(typeOfAttr, attrName, true);
        }
        else {
          methodParam = CD4AnalysisNodeFactory.createASTCDParameter(typeOfAttr, attrName, false);
        }
        methodParams.add(methodParam);
      }
    }
    
    List<String> exceptions = new ArrayList<>();
    String[] excepArr = m.split("throws");
    if (excepArr.length > 1) {
      String exceptAttr = excepArr[0];
      String[] exceptAttrArr = exceptAttr.split(",");
      for (String e : exceptAttrArr) {
        exceptions.add(e);
      }
      ASTQualifiedName methodExcept = TypesNodeFactory.createASTQualifiedName(exceptions);
      methodExcepts.add(methodExcept);
    }
    ASTCDMethod method = CD4AnalysisNodeFactory.createASTCDMethod(modifier, returnType, mName, methodParams, methodExcepts);
    
    interfMethods.add(method);
    
    // test interfaces against each other
    ASTCDInterface interf = CD4AnalysisNodeFactory.createASTCDInterface(modifier, interfName, interfaces, interfAttributes, interfMethods);
    
    ASTCDInterface transformedInterf = unit.getCDDefinition().getCDInterfaces().get(0);
    
    assert(interf.deepEquals(transformedInterf));
  }
}
