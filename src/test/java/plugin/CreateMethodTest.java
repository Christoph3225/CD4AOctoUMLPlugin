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
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDClass;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDConstructor;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDMethod;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDParameter;
import de.monticore.umlcd4a.cd4analysis._ast.ASTModifier;
import de.monticore.umlcd4a.cd4analysis._ast.CD4AnalysisNodeFactory;
import model.Graph;
import model.nodes.ClassNode;

public class CreateMethodTest {
  private CD4APlugin plugin;
  private Graph graph;
  private TypesParser typeParser;
  private String modelname = "createclasscd";
  
  @Before
  public void initTest() {
    plugin = new CD4APlugin();
    typeParser = new TypesParser();
  }
  
  @Test
  public void createSingleClassWithMethodsTest() {
    graph = new Graph();
    // create test object
    ClassNode node = new ClassNode();
    node.setTitle("Student");
    node.setOperations("int getAge();");
    graph.addNode(node, false);
    
    ASTCDCompilationUnit unit = plugin.shapeToAST(graph, modelname);
    
    // create test result
    ASTModifier modifier = CD4AnalysisNodeFactory.createASTModifier(null, false, false, false, false, false, true, false);;
    String name = "Student";
    ASTReferenceType superclass = null;
    List<ASTReferenceType> interfaces = new ArrayList<>();
    List<ASTCDAttribute> attributes = new ArrayList<>();
    List<ASTCDConstructor> constructors = new ArrayList<>();
    List<ASTCDMethod> methods = new ArrayList<>();
    
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
    
    methods.add(method);
    
    ASTCDClass clazz = CD4AnalysisNodeFactory.createASTCDClass(modifier, name, superclass, interfaces, attributes, constructors, methods);
    
    // test classes against each other
    ASTCDClass transformedClass = unit.getCDDefinition().getCDClasses().get(0);
    
    assert(clazz.deepEquals(transformedClass));
  }
  
  @Test
  public void createSingleClassWithAttributeAndMethodsTest() {
    graph = new Graph();
 // create test object
    ClassNode node = new ClassNode();
    node.setTitle("Student");
    node.setAttributes("int age;");
    node.setOperations("Student();");
    graph.addNode(node, false);
    
    ASTCDCompilationUnit unit = plugin.shapeToAST(graph, modelname);
    
    // create test result
    ASTModifier modifier = CD4AnalysisNodeFactory.createASTModifier(null, false, false, false, false, false, true, false);;
    String name = "Student";
    ASTReferenceType superclass = null;
    List<ASTReferenceType> interfaces = new ArrayList<>();
    List<ASTCDAttribute> attributes = new ArrayList<>();
    List<ASTCDConstructor> constructors = new ArrayList<>();
    List<ASTCDMethod> methods = new ArrayList<>();
    
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
    attributes.add(cdAttr);
    
    String method = "Student()";
    
    List<ASTCDParameter> constructParams = new ArrayList<>();
    String[] paramArr = method.split("\\(|\\)");
    if (paramArr.length > 1) {
      String strParam = paramArr[1];
      String[] paramAttrArr = strParam.split(",");
      for (String a : paramAttrArr) {
        String[] arr3 = a.split("\\s");
        String attrType = arr3[0];
        String attrName2 = arr3[1];
        
        Optional<de.monticore.types.types._ast.ASTType> typeResult = null;
        try {
          typeResult = typeParser.parse_String(attrType);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        
        ASTType typeOfAttr2 = typeResult.get();
        
        ASTCDParameter constructParam;
        if (a.contains("...")) {
          constructParam = CD4AnalysisNodeFactory.createASTCDParameter(typeOfAttr2, attrName2, true);
        }
        else {
          constructParam = CD4AnalysisNodeFactory.createASTCDParameter(typeOfAttr2, attrName2, false);
        }
        constructParams.add(constructParam);
      }
    }
    List<ASTQualifiedName> constructExcepts = new ArrayList<>();
    List<String> exceptions = new ArrayList<>();
    String[] excepArr = method.split("throws");
    
    if (excepArr.length > 1) {
      String exceptAttr = excepArr[0];
      String[] exceptAttrArr = exceptAttr.split(",");
      for (String e : exceptAttrArr) {
        exceptions.add(e);
      }
      ASTQualifiedName methodExcept = TypesNodeFactory.createASTQualifiedName(exceptions);
      constructExcepts.add(methodExcept);
    }
    ASTCDConstructor constructor = CD4AnalysisNodeFactory.createASTCDConstructor(modifier, name, constructParams, constructExcepts);
    constructors.add(constructor);
    
    ASTCDClass clazz = CD4AnalysisNodeFactory.createASTCDClass(modifier, name, superclass, interfaces, attributes, constructors, methods);
    
    // test classes against each other
    ASTCDClass transformedClass = unit.getCDDefinition().getCDClasses().get(0);
    
    assert(clazz.deepEquals(transformedClass));
  }
}
