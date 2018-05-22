package plugin;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.monticore.types.types._ast.ASTImportStatement;
import de.monticore.types.types._ast.TypesNodeFactory;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDDefinition;
import de.monticore.umlcd4a.cd4analysis._ast.CD4AnalysisNodeFactory;
import model.Graph;

public class CreateImportStatementTest {
  private CD4APlugin plugin;
  private Graph graph;
  private String modelname = "createimportstatementcd";
  
  @Before
  public void initTest() {
    plugin = CD4APlugin.getInstance();
  }
  
  @Test
  public void createImportStatement() {
    graph = new Graph();
    // create test object
    String packageName = "cd4aplugin";
    String imports = "java.util.*;";
    List<String> containerInfo = new ArrayList<>();
    containerInfo.add(packageName);
    containerInfo.add(imports);
    containerInfo.add(modelname);
    
    ASTCDCompilationUnit unit = plugin.shapeToAST(graph, containerInfo);
    
    // create test result
    List<String> r__packages = new ArrayList<>();
    r__packages.add(packageName);
    List<ASTImportStatement> transformedImports;
    if (!(imports == null)) {
      transformedImports = new ArrayList<>();
      String[] arr = imports.split(";");
      for (String s : arr) {
        List<String> createImportList = new ArrayList<>();
        createImportList.add(s);
        if (s.endsWith("*")) {
          ASTImportStatement importStatement = TypesNodeFactory.createASTImportStatement(createImportList, true);
          transformedImports.add(importStatement);
        } else {
          ASTImportStatement importStatement = TypesNodeFactory.createASTImportStatement(createImportList, false);
          transformedImports.add(importStatement);
        }
      }
    } else {
      transformedImports = new ArrayList<>();
    }
    ASTCDDefinition cdDef = CD4AnalysisNodeFactory.createASTCDDefinition();
    
    ASTCDCompilationUnit transformedUnit = CD4AnalysisNodeFactory.createASTCDCompilationUnit(r__packages, transformedImports, cdDef);
    
    // test against each other
    assert(unit.getImportStatements().toString().equals(transformedUnit.getImportStatements().toString()));
  }
}
