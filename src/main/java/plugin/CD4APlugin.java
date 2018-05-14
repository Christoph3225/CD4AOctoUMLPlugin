package plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import controller.AbstractDiagramController;
import controller.CD4AController;
import de.monticore.ast.ASTNode;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.prettyprint.TypesPrettyPrinterConcreteVisitor;
import de.monticore.types.types._ast.*;
import de.monticore.types.types._parser.TypesParser;
import de.monticore.umlcd4a.cd4analysis._ast.*;
import de.monticore.umlcd4a.prettyprint.CDPrettyPrinterConcreteVisitor;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import model.Graph;
import model.edges.Edge;
import model.edges.InheritanceEdge;
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
    }
    catch (Exception e) {
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
  public TypesPrettyPrinterConcreteVisitor getPrettyPrinter() {
    IndentPrinter i = new IndentPrinter();
    CDPrettyPrinterConcreteVisitor prettyprinter = new CDPrettyPrinterConcreteVisitor(i);
    return prettyprinter;
  }
  
  @Override
  public ASTNode getASTNode() {
    return cdCompUnit;
  }
  
  @SuppressWarnings("static-access")
  @Override
  public ASTCDCompilationUnit shapeToAST(Graph graph, String modelName) {
    // create AST via factory
    
    // elements for creating ASTCDCompilationUnit
    List<String> r__packages = new ArrayList<>();
    List<ASTImportStatement> imports = new ArrayList<>();
    r__packages.add("cd4aplugin");
    
    // elements for creating ASTCDDefinition
    String cdName = modelName;
    List<ASTCDClass> clazzes = new ArrayList<>();
    List<ASTCDInterface> interfazes = new ArrayList<>();
    List<ASTCDEnum> enoms = new ArrayList<>();
    List<ASTCDAssociation> assocs = new ArrayList<>();
    
    // create classes, interfaces, enums of classdiagram
    for (AbstractNode cNode : graph.getAllNodes()) {
      if (cNode instanceof ClassNode) {
        // Superklassen einer Klasse
        List<String> superClazzes = new ArrayList<>();
        List<String> superInterf = new ArrayList<>();
        
        String clazzName = cNode.getTitle();
        if (clazzName.contains("<<interface>>")) {
          // cNode is an interface
          List<Edge> edges = graph.getAllEdges();
          for (Edge e : edges) {
            if (e instanceof InheritanceEdge && e.getStartNode() == cNode) {
              String superName = e.getEndNode().getTitle();
              if(superName.contains("<<interface>>")) {
                superInterf.add(superName);
              }
            }
          }
          ASTCDInterface interf = createASTInterface((ClassNode) cNode, clazzName, superInterf);
          interfazes.add(interf);
          
        }
        else if (clazzName.contains("<<abstract>>")) {
          // cNode is an abstract class
          List<Edge> edges = graph.getAllEdges();
          for (Edge e : edges) {
            if (e instanceof InheritanceEdge && e.getStartNode() == cNode) {
              String superName = e.getEndNode().getTitle();
              if(superName.contains("<<interface>>")) {
                superInterf.add(superName);
              } else {
                superClazzes.add(superName);
              }
            }
          }
          
          ASTCDClass clazz = createASTClass((ClassNode) cNode, clazzName, true, superClazzes, superInterf);
          clazzes.add(clazz);
          
        }
        else if (clazzName.contains("<<enum>>")) {
          // cNode is an enum
          ASTCDEnum enom = createASTEnum((ClassNode) cNode, clazzName);
          enoms.add(enom);
          
        }
        else {
          // cNode is a class
          List<Edge> edges = graph.getAllEdges();
          for (Edge e : edges) {
            if (e instanceof InheritanceEdge && e.getStartNode() == cNode) {
              String superName = e.getEndNode().getTitle();
              if(superName.contains("<<interface>>")) {
                superInterf.add(superName);
              } else {
                superClazzes.add(superName);
              }
            }
          }
          
          ASTCDClass clazz = createASTClass((ClassNode) cNode, clazzName, false, superClazzes, superInterf);
          clazzes.add(clazz);
        }
      }
      if (cNode instanceof PackageNode) {
        // later in container dialog
      }
    }
    
    //TODO associations
    
    ASTCDDefinition cdDef = cd4aFactory.createASTCDDefinition(cdName, clazzes, interfazes, enoms, assocs);
    ASTCDCompilationUnit unit = cd4aFactory.createASTCDCompilationUnit(r__packages, imports, cdDef);
    
    cdCompUnit = unit;
    
    return unit;
  }
  
  @SuppressWarnings("static-access")
  private ASTCDInterface createASTInterface(ClassNode node, String name, List<String> superInterf) {
    
    ASTModifier modifier = cd4aFactory.createASTModifier(null, false, false, false, false, false, true, false);
    String interfName = name;
    List<ASTReferenceType> interfaces = new ArrayList<>();
    List<String> extendsInterfNames = superInterf;
    for (String s : extendsInterfNames) {
      List<String> eINames = new ArrayList<>();
      eINames.add(s);
      ASTReferenceType interfExtend = typesFactory.createASTSimpleReferenceType(eINames, null);
      interfaces.add(interfExtend);
    }
    
    List<ASTCDAttribute> interfAttributes = new ArrayList<>();
    String nodeAttributes = node.getAttributes();
    if (nodeAttributes.equals("") || nodeAttributes == null) {
      interfAttributes = new ArrayList<>();
    }
    else {
      interfAttributes = new ArrayList<>();
      String[] arr = nodeAttributes.split(";");
      for (String s : arr) {
        ASTCDAttribute cdAttr = createASTCDAttribute(s);
        interfAttributes.add(cdAttr);
      }
    }
    List<ASTCDMethod> interfMethods = new ArrayList<>();
    String nodeOperations = node.getOperations();
    String[] methodArr = nodeOperations.split(";");
    if (nodeOperations.equals("") || nodeOperations == null) {
      interfMethods = new ArrayList<>();
    }
    else {
      interfMethods = new ArrayList<>();
      for (String s : methodArr) {
        s = s.replace("\n", "");
        ASTCDMethod method = createASTCDMethod(s);
        interfMethods.add(method);
      }
    }
    
    ASTCDInterface retInterf = cd4aFactory.createASTCDInterface(modifier, interfName, interfaces, interfAttributes, interfMethods);
    return retInterf;
  }
  
  @SuppressWarnings("static-access")
  private ASTCDEnum createASTEnum(ClassNode node, String name) {
    ASTModifier modifier = cd4aFactory.createASTModifier(null, false, false, false, false, false, true, false);
    String enumName = name;
    List<ASTReferenceType> interfaces = new ArrayList<>();
    List<String> extendsInterfNames = new ArrayList<>();
    for (String s : extendsInterfNames) {
      List<String> eINames = new ArrayList<>();
      eINames.add(s);
      ASTReferenceType interfExtend = typesFactory.createASTSimpleReferenceType(eINames, null);
      interfaces.add(interfExtend);
    }
    
    List<ASTCDEnumConstant> enumConstants;
    String nodeAttributes = node.getAttributes();
    if (nodeAttributes.equals("") || nodeAttributes == null) {
      enumConstants = new ArrayList<>();
    }
    else {
      enumConstants = new ArrayList<>();
      String[] arr = nodeAttributes.split(",");
      for (String s : arr) {
        List<ASTCDEnumParameter> enomParams = new ArrayList<>();
        ASTCDEnumConstant enomConst = cd4aFactory.createASTCDEnumConstant(s, enomParams);
        enumConstants.add(enomConst);
      }
    }
    
    String nodeOperations = node.getOperations();
    String[] methodArr = nodeOperations.split(";");
    List<ASTCDConstructor> enumConstructors;
    
    // Constructors of the class if present else empty list
    if (nodeOperations.equals("") || nodeOperations == null) {
      enumConstructors = new ArrayList<>();
    }
    else {
      enumConstructors = new ArrayList<>();
      for (String s : methodArr) {
        if (s.contains(enumName)) {
          ASTCDConstructor constructor = createASTCDConstructor(enumName, s);
          enumConstructors.add(constructor);
        }
      }
    }
    
    List<ASTCDMethod> enumMethods;
    if (nodeOperations.equals("") || nodeOperations == null) {
      enumMethods = new ArrayList<>();
    }
    else {
      enumMethods = new ArrayList<>();
      for (String s : methodArr) {
        s = s.replace("\n", "");
        if (!s.contains(enumName)) {
          ASTCDMethod method = createASTCDMethod(s);
          enumMethods.add(method);
        }
      }
    }
    
    ASTCDEnum enom = cd4aFactory.createASTCDEnum(modifier, enumName, interfaces, enumConstants, enumConstructors, enumMethods);
    return enom;
  }
  
  @SuppressWarnings("static-access")
  private ASTCDClass createASTClass(ClassNode node, String name, boolean abstrakt, List<String> superClz, List<String> superInterf) {
    
    // Stereotype of the class always not present
    // ASTStereotype stereotype = cd4aFactory.createASTStereotype();
    
    // class modifier is always public
    ASTModifier clazzModifier = cd4aFactory.createASTModifier(null, false, false, false, false, false, true, false);
    
    // name of the class given by graph
    String clazzName = name;
    
    // Superclass of the class if present else null
    List<String> superClassNames = superClz;
    
    ASTReferenceType clazzSuperclass = typesFactory.createASTSimpleReferenceType(superClassNames, null);
    
    // Interfaces of the class if present else empty list
    List<ASTReferenceType> clazzInterfaces = new ArrayList<>();
    List<String> extendsInterfNames = superInterf;
    for (String s : extendsInterfNames) {
      List<String> eINames = new ArrayList<>();
      eINames.add(s);
      ASTReferenceType interfExtend = typesFactory.createASTSimpleReferenceType(eINames, null);
      clazzInterfaces.add(interfExtend);
    }
    
    // Attributes of the class if present else empty list
    List<ASTCDAttribute> clazzAttributes;
    String nodeAttributes = node.getAttributes();
    if (nodeAttributes.equals("") || nodeAttributes == null) {
      clazzAttributes = new ArrayList<>();
    }
    else {
      clazzAttributes = new ArrayList<>();
      String[] arr = nodeAttributes.split(";");
      for (String s : arr) {
        ASTCDAttribute cdAttr = createASTCDAttribute(s);
        clazzAttributes.add(cdAttr);
      }
    }
    
    String nodeOperations = node.getOperations();
    
    // Constructors of the class if present else empty list
    List<ASTCDConstructor> clazzConstructors;
    if (nodeOperations.equals("") || nodeOperations == null) {
      clazzConstructors = new ArrayList<>();
    }
    else {
      clazzConstructors = new ArrayList<>();
      String[] methodArr = nodeOperations.split(";");
      for (String s : methodArr) {
        if (s.contains(clazzName)) {
          ASTCDConstructor constructor = createASTCDConstructor(clazzName, s);
          clazzConstructors.add(constructor);
        }
      }
    }
    
    // Methods of the class if present else empty list
    List<ASTCDMethod> clazzMethods;
    if (nodeOperations.equals("") || nodeOperations == null) {
      clazzMethods = new ArrayList<>();
    }
    else {
      clazzMethods = new ArrayList<>();
      String[] methodArr = nodeOperations.split(";");
      for (String s : methodArr) {
        s = s.replace("\n", "");
        if (!s.contains(clazzName)) {
          ASTCDMethod method = createASTCDMethod(s);
          clazzMethods.add(method);
        }
      }
    }
    
    ASTCDClass retClass;
    if (superClassNames.isEmpty()) {
      retClass = cd4aFactory.createASTCDClass(clazzModifier, clazzName, null, clazzInterfaces, clazzAttributes, clazzConstructors, clazzMethods);
    }
    else {
      retClass = cd4aFactory.createASTCDClass(clazzModifier, clazzName, clazzSuperclass, clazzInterfaces, clazzAttributes, clazzConstructors, clazzMethods);
    }
    
    return retClass;
  }
  
  @SuppressWarnings("static-access")
  private ASTCDConstructor createASTCDConstructor(String name, String method) {
    TypesParser typeParser = new TypesParser();
    ASTModifier clazzModifier = cd4aFactory.createASTModifier(null, false, false, false, false, false, true, false);
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
        if(a.contains("...")) {
          constructParam = cd4aFactory.createASTCDParameter(typeOfAttr, attrName, true);
        } else {
          constructParam = cd4aFactory.createASTCDParameter(typeOfAttr, attrName, false);
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
      ASTQualifiedName methodExcept = typesFactory.createASTQualifiedName(exceptions);
      constructExcepts.add(methodExcept);
    }
    ASTCDConstructor constructor = cd4aFactory.createASTCDConstructor(clazzModifier, name, constructParams, constructExcepts);
    return constructor;
  }
  
  @SuppressWarnings("static-access")
  private ASTCDMethod createASTCDMethod(String m) {
    TypesParser typeParser = new TypesParser();
    ASTModifier clazzModifier = cd4aFactory.createASTModifier(null, false, false, false, false, false, true, false);
    ASTReturnType returnType;
    String[] arr = m.split("\\(");
    String methodNameAndReturn = arr[0];
    String[] arr2 = methodNameAndReturn.split("\\s");
    String mRetTypeStr = arr2[0];
    String mName = arr2[1];
    
    if (mRetTypeStr.equals("void")) {
      returnType = typesFactory.createASTVoidType();
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
        if(a.contains("...")) {
          methodParam = cd4aFactory.createASTCDParameter(typeOfAttr, attrName, true);
        } else {
          methodParam = cd4aFactory.createASTCDParameter(typeOfAttr, attrName, false);
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
      ASTQualifiedName methodExcept = typesFactory.createASTQualifiedName(exceptions);
      methodExcepts.add(methodExcept);
    }
    ASTCDMethod method = cd4aFactory.createASTCDMethod(clazzModifier, returnType, mName, methodParams, methodExcepts);
    return method;
  }
  
  @SuppressWarnings("static-access")
  private ASTCDAttribute createASTCDAttribute(String nodeAttr) {
    ASTModifier clazzModifier = cd4aFactory.createASTModifier(null, false, false, false, false, false, true, false);
    
    nodeAttr = nodeAttr.replace("\n", "");
    String[] arr2 = nodeAttr.split("\\s");
    String attrType = arr2[0];
    String attrName = arr2[1];
    
    TypesParser typeParser = new TypesParser();
    Optional<de.monticore.types.types._ast.ASTType> typeResult = null;
    try {
      typeResult = typeParser.parse_String(attrType);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    
    ASTType typeOfAttr = typeResult.get();
    
    ASTCDAttribute cdAttr = cd4aFactory.createASTCDAttribute(clazzModifier, typeOfAttr, attrName, null);
    return cdAttr;
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
    Optional<String> result = dialog.showAndWait();
    if (result.isPresent()) {
      resList.add(result.get());
    }
    return resList;
  }
  
}
