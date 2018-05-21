package plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import de.monticore.types.types._ast.*;
import de.monticore.types.types._parser.TypesParser;
import de.monticore.umlcd4a.cd4analysis._ast.*;
import model.Graph;
import model.edges.InheritanceEdge;
import model.nodes.ClassNode;

public class CreateClassTest {
  
  private CD4APlugin plugin;
  private Graph graph;
  private TypesParser typeParser;
  private String modelname = "createclasscd";
  
  @Before
  public void initTest() {
	  plugin = CD4APlugin.getInstance();
    typeParser = new TypesParser();
  }
  
  @Test
  public void createSingleClassWithNameTest() {
    graph = new Graph();
    // create test object
    ClassNode node = new ClassNode();
    node.setTitle("Student");
    graph.addNode(node, false);
    
    String packageName = "cd4aplugin";
    String imports = "";
    List<String> containerInfo = new ArrayList<>();
    containerInfo.add(packageName);
    containerInfo.add(imports);
    containerInfo.add(modelname);
    
    ASTCDCompilationUnit unit = plugin.shapeToAST(graph, containerInfo);
    
    // create test result
    ASTModifier modifier = CD4AnalysisNodeFactory.createASTModifier(null, false, false, false, false, false, true, false);;
    String name = "Student";
    ASTReferenceType superclass = null;
    List<ASTReferenceType> interfaces = new ArrayList<>();
    List<ASTCDAttribute> attributes = new ArrayList<>();
    List<ASTCDConstructor> constructors = new ArrayList<>();
    List<ASTCDMethod> methods = new ArrayList<>();
    
    ASTCDClass clazz = CD4AnalysisNodeFactory.createASTCDClass(modifier, name, superclass, interfaces, attributes, constructors, methods);
    
    // test classes against each other
    ASTCDClass transformedClass = unit.getCDDefinition().getCDClasses().get(0);
    
    assert(clazz.deepEquals(transformedClass));
  }
  
  @Test
  public void createSingleClassWithAttributeTest() {
    graph = new Graph();
    // create test object
    ClassNode node = new ClassNode();
    node.setTitle("Student");
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
    
    ASTCDClass clazz = CD4AnalysisNodeFactory.createASTCDClass(modifier, name, superclass, interfaces, attributes, constructors, methods);
    
    
    // Test classes against each other
    ASTCDClass transformedClass = unit.getCDDefinition().getCDClasses().get(0);
    
    assert(clazz.deepEquals(transformedClass));
  }
  
  @Test
  public void createSingleClassWithConstructorTest() {
    graph = new Graph();
    // create test object
    ClassNode node = new ClassNode();
    node.setTitle("Student");
    node.setOperations("Student();");
    graph.addNode(node, false);
    
    String packageName = "cd4aplugin";
    String imports = "";
    List<String> containerInfo = new ArrayList<>();
    containerInfo.add(packageName);
    containerInfo.add(imports);
    containerInfo.add(modelname);
    
    ASTCDCompilationUnit unit = plugin.shapeToAST(graph, containerInfo);
    
    // create test result
    ASTModifier modifier = CD4AnalysisNodeFactory.createASTModifier(null, false, false, false, false, false, true, false);;
    String name = "Student";
    ASTReferenceType superclass = null;
    List<ASTReferenceType> interfaces = new ArrayList<>();
    List<ASTCDAttribute> attributes = new ArrayList<>();
    List<ASTCDConstructor> constructors = new ArrayList<>();
    List<ASTCDMethod> methods = new ArrayList<>();
    
    String method = "Student()";
    
    List<ASTCDParameter> constructParams = new ArrayList<>();
    String[] paramArr = method.split("\\(|\\)");
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
        
        ASTCDParameter constructParam;
        if (a.contains("...")) {
          constructParam = CD4AnalysisNodeFactory.createASTCDParameter(typeOfAttr, attrName, true);
        }
        else {
          constructParam = CD4AnalysisNodeFactory.createASTCDParameter(typeOfAttr, attrName, false);
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
  
  @Test
  public void createSingleClassWithMethodsTest() {
    graph = new Graph();
    // create test object
    ClassNode node = new ClassNode();
    node.setTitle("Student");
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
    
    String packageName = "cd4aplugin";
    String imports = "";
    List<String> containerInfo = new ArrayList<>();
    containerInfo.add(packageName);
    containerInfo.add(imports);
    containerInfo.add(modelname);
    
    ASTCDCompilationUnit unit = plugin.shapeToAST(graph, containerInfo);
    
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
  
  @Test
  public void createSingleClassWithSuperClassTest() {
    graph = new Graph();
    // create test object
    ClassNode node = new ClassNode();
    node.setTitle("Student");
    
    ClassNode superNode = new ClassNode();
    superNode.setTitle("Person");
    
    graph.addNode(node, false);
    graph.addNode(superNode, false);
    
    InheritanceEdge inherit = new InheritanceEdge();
    inherit.setStartNode(node);
    inherit.setEndNode(superNode);
    
    graph.addEdge(inherit, false);
    
    String packageName = "cd4aplugin";
    String imports = "";
    List<String> containerInfo = new ArrayList<>();
    containerInfo.add(packageName);
    containerInfo.add(imports);
    containerInfo.add(modelname);
    
    ASTCDCompilationUnit unit = plugin.shapeToAST(graph, containerInfo);
    
    // create test result
    ASTModifier modifier = CD4AnalysisNodeFactory.createASTModifier(null, false, false, false, false, false, true, false);;
    String name = "Student";
    
    // Superclass of the class if present else null
    List<String> superClassNames = new ArrayList<>();
    superClassNames.add("Person");
    
    ASTReferenceType clazzSuperClass = TypesNodeFactory.createASTSimpleReferenceType(superClassNames, null);
    
    List<ASTReferenceType> interfaces = new ArrayList<>();
    List<ASTCDAttribute> attributes = new ArrayList<>();
    List<ASTCDConstructor> constructors = new ArrayList<>();
    List<ASTCDMethod> methods = new ArrayList<>();
    
    ASTCDClass clazz = CD4AnalysisNodeFactory.createASTCDClass(modifier, name, clazzSuperClass, interfaces, attributes, constructors, methods);
    
    // test classes against each other
    ASTCDClass transformedClass = unit.getCDDefinition().getCDClasses().get(0);
    
    assert(clazz.deepEquals(transformedClass));
  }
  
  @Test
  public void createSingleClassWithInterfaceTest() {
    graph = new Graph();
    // create test object
    ClassNode node = new ClassNode();
    node.setTitle("Student");
    
    ClassNode superNode = new ClassNode();
    superNode.setTitle("<<interface>> Person");
    
    graph.addNode(node, false);
    graph.addNode(superNode, false);
    
    InheritanceEdge inherit = new InheritanceEdge();
    inherit.setStartNode(node);
    inherit.setEndNode(superNode);
    
    graph.addEdge(inherit, false);
    
    String packageName = "cd4aplugin";
    String imports = "";
    List<String> containerInfo = new ArrayList<>();
    containerInfo.add(packageName);
    containerInfo.add(imports);
    containerInfo.add(modelname);
    
    ASTCDCompilationUnit unit = plugin.shapeToAST(graph, containerInfo);
    
    // create test result
    ASTModifier modifier = CD4AnalysisNodeFactory.createASTModifier(null, false, false, false, false, false, true, false);;
    String name = "Student";
    
    ASTReferenceType clazzSuperClass = null;
    
    List<ASTReferenceType> interfaces = new ArrayList<>();
    List<String> extendsInterfNames = new ArrayList<>();
    extendsInterfNames.add(" Person");
    for (String s : extendsInterfNames) {
      List<String> eINames = new ArrayList<>();
      eINames.add(s);
      ASTReferenceType interfExtend = TypesNodeFactory.createASTSimpleReferenceType(eINames, null);
      interfaces.add(interfExtend);
    }
    List<ASTCDAttribute> attributes = new ArrayList<>();
    List<ASTCDConstructor> constructors = new ArrayList<>();
    List<ASTCDMethod> methods = new ArrayList<>();
    
    ASTCDClass clazz = CD4AnalysisNodeFactory.createASTCDClass(modifier, name, clazzSuperClass, interfaces, attributes, constructors, methods);
    
    // test classes against each other
    ASTCDClass transformedClass = unit.getCDDefinition().getCDClasses().get(0);
    
    assert(clazz.deepEquals(transformedClass));
    
  }
}
