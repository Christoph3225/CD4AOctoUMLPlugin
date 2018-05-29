package generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.monticore.umlcd4a.cd4analysis._ast.*;
import plugin.MontiCoreException;

public class CD4ACodeGenerator {
  private static final CD4ACodeGenerator singleTonGenerator = new CD4ACodeGenerator();
  
  public static final String EMPTY_STRING = "";
  
  private int errorCounter = 0;
  private List<MontiCoreException> errorList = new ArrayList<>();
  
  private CD4ACodeGenerator() {
    
  }
  
  public static CD4ACodeGenerator getInstance() {
    return singleTonGenerator;
  }
  
  public void generate(ASTCDCompilationUnit unit, String outputPath) {
    ASTCDDefinition cdDef = unit.getCDDefinition();
    List<ASTCDClass> cdClasses = cdDef.getCDClasses();
    List<ASTCDInterface> cdInterfaces = cdDef.getCDInterfaces();
    List<ASTCDEnum> cdEnums = cdDef.getCDEnums();
    
    for (ASTCDClass c : cdClasses) {
      generateClass(c, outputPath);
    }
    
    for (ASTCDInterface i : cdInterfaces) {
      generateInterface(i, outputPath);
    }
    
    for (ASTCDEnum e : cdEnums) {
      generateEnum(e, outputPath);
    }
    
    // set up the number of exceptions
    errorCounter = errorList.size();
  }
  
  private void generateClass(ASTCDClass c, String path) {
    String code = "";
    String className = c.getName();
    
    String firstLine;
    if (c.printSuperClass().equals(EMPTY_STRING)) {
      if (c.printInterfaces().equals(EMPTY_STRING)) {
        firstLine = "public class " + className + " { \n";
      }
      else {
        firstLine = "public class " + className + " implements " + c.printInterfaces() + " { \n";
      }
    }
    else {
      if (c.printInterfaces().equals(EMPTY_STRING)) {
        firstLine = "public class " + className + " extends " + c.printSuperClass() + " { \n";
      }
      else {
        firstLine = "public class " + className + " extends " + c.printSuperClass() + " implements " + c.printInterfaces() + " { \n";
      }
    }
    
    code += firstLine;
    
    String attrLine;
    for (ASTCDAttribute a : c.getCDAttributes()) {
      attrLine = generateAttribute(a, true);
      code += attrLine;
    }
    
    String constrLine;
    for (ASTCDConstructor con : c.getCDConstructors()) {
      constrLine = generateConstructor(con);
      code += constrLine;
    }
    
    String methodLine;
    for (ASTCDMethod m : c.getCDMethods()) {
      methodLine = generateMethod(m);
      code += methodLine;
    }
    
    code += "\n}";
    
    try {
      FileUtils.writeStringToFile(new File(path + className + ".java"), code);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private String generateAttribute(ASTCDAttribute a, boolean isClass) {
    String result = "";
    
    String attrName = a.getName();
    
    // generate attribute
    String attr = a.printType() + " " + attrName + "; \n \n";
    
    String getter = "";
    String setter = "";
    if (isClass) {
      // generate getter
      String getHead = "public " + a.printType() + " " + "get" + (attrName.substring(0, 1).toUpperCase() + attrName.substring(1)) + "() {\n";
      String getBody = "return this." + attrName + ";\n";
      String getEnd = "} \n\n";
      getter = getHead + getBody + getEnd;
      
      // generate setter
      String setHead = "public void set" + (attrName.substring(0, 1).toUpperCase() + attrName.substring(1)) + "(" + a.printType() + " x) {\n";
      String setBody = "this." + attrName + " = x;\n";
      String setEnd = "} \n\n";
      setter = setHead + setBody + setEnd;
    }
    result = attr + getter + setter;
    
    return result;
  }
  
  private String generateConstructor(ASTCDConstructor con) {
    String result = "";
    
    String name = con.getName();
    if (con.printParametersDecl().equals(EMPTY_STRING)) {
      if (con.printThrowsDecl().equals(EMPTY_STRING)) {
        result = "public " + name + "() { \n }\n";
      }
      else {
        result = "public " + name + "() throws " + con.printThrowsDecl() + "{ \n }\n";
      }
    }
    else {
      if (con.printThrowsDecl().equals(EMPTY_STRING)) {
        result = "public " + name + "(" + con.printParametersDecl() + ") { \n ";
      }
      else {
        result = "public " + name + "(" + con.printParametersDecl() + ") throws " + con.printThrowsDecl() + "{ \n ";
      }
      List<ASTCDParameter> params = con.getCDParameters();
      for(ASTCDParameter p : params) {
        String paramName = (p.getName().substring(0, 1).toUpperCase() + p.getName().substring(1));
        result += "this.set" + paramName + "(" + p.getName() + ");";
      }
      result += "} \n";
    }
    
    return result;
  }
  
  private String generateMethod(ASTCDMethod m) {
    String result = "";
    
    String name = m.getName();
    
    if (m.printParametersDecl().equals(EMPTY_STRING)) {
      if (m.printThrowsDecl().equals(EMPTY_STRING)) {
        result = "public " + m.printReturnType() + name + "() { \n }\n";
      }
      else {
        result = "public " + m.printReturnType() + name + "() throws " + m.printThrowsDecl() + "{ \n }\n";
      }
    }
    else {
      if (m.printThrowsDecl().equals(EMPTY_STRING)) {
        result = "public " + m.printReturnType() + name + "(" + m.printParametersDecl() + ") { \n }\n";
      }
      else {
        result = "public " + m.printReturnType() + name + "(" + m.printParametersDecl() + ") throws " + m.printThrowsDecl() + "{ \n }\n";
      }
    }
    
    return result;
  }
  
  private void generateInterface(ASTCDInterface i, String path) {
    String code = "";
    String interfName = i.getName();
    
    String firstLine;
    if (i.printInterfaces().equals(EMPTY_STRING)) {
      firstLine = "public interface " + interfName + " { \n";
    }
    else {
      firstLine = "public interface " + interfName + " implements " + i.printInterfaces() + " { \n";
    }
    
    code += firstLine;
    
    String attrLine;
    for (ASTCDAttribute a : i.getCDAttributes()) {
      attrLine = generateAttribute(a, false);
      code += attrLine;
    }
    
    String methodLine;
    for (ASTCDMethod m : i.getCDMethods()) {
      methodLine = generateMethod(m);
      code += methodLine;
    }
    
    code += "\n}";
    
    try {
      String generatePath = path + "/src/main/java/";
      FileUtils.writeStringToFile(new File(generatePath + interfName + ".java"), code);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private void generateEnum(ASTCDEnum e, String path) {
    String code = "";
    String enumName = e.getName();
    
    String firstLine = "public enum " + enumName + " { \n";
    code += firstLine;
    
    String contentLine = e.printEnumConstants();
    
    code += contentLine;
    
    String endLine = "}";
    code += endLine;
    
    try {
      String generatePath = path + "/src/main/java/";
      FileUtils.writeStringToFile(new File(generatePath + enumName + ".java"), code);
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }
  
  public boolean wasSuccessfull() {
    if (errorCounter == 0) {
      return true;
    }
    else {
      return false;
    }
  }
  
}
