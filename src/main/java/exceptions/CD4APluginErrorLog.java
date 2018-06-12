package exceptions;

import java.util.ArrayList;
import java.util.List;

import plugin.MontiCoreException;

public class CD4APluginErrorLog {
  enum ExceptionType {
    COCO_ERROR, NODE_NAME_MISSING, CODE_GENERATION_ERROR, CLASS_NAME_MISSING, CLASSDIAGRAM_NAME_MISSING, INTERFACE_NAME_MISSING, ENUM_NAME_MISSING, CLASS_ATTRIBUTE_NAME_MISSING, CLASS_ATTRIBUTE_TYPE_MISSING, ENUM_CONSTANT_NAME_MISSING, CONSTRUCTOR_NAME_MISSING, METHOD_RETURNTYPE_MISSING, METHOD_NAME_MISSING, METHOD_PARAMETER_NAME_MISSING, METHOD_PARAMETER_TYPE_MISSING, LEFT_REFERENCE_NAME_MISSING, RIGHT_REFERENCE_NAME_MISSING, NO_MULTIPLITY_SET, PACKAGE_NAME_MISSING
  };

  private static final CD4APluginErrorLog singleTonLog = new CD4APluginErrorLog();
  private List<MontiCoreException> allLogs = new ArrayList<MontiCoreException>();

  private CD4APluginErrorLog() {
    
  }

  public static CD4APluginErrorLog getInstance() {
    return singleTonLog;
  }

  public void addLog(MontiCoreException ex) {
    allLogs.add(ex);
  }

  public List<MontiCoreException> getAllLogs() {
    return allLogs;
  }
  
}
