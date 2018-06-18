package plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;

import controller.AbstractDiagramController;
import controller.CD4AController;
import de.monticore.ast.ASTNode;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.prettyprint.TypesPrettyPrinterConcreteVisitor;
import de.monticore.types.types._ast.*;
import de.monticore.types.types._parser.TypesParser;
import de.monticore.umlcd4a.cd4analysis._ast.*;
import de.monticore.umlcd4a.cd4analysis._cocos.CD4AnalysisCoCoChecker;
import de.monticore.umlcd4a.cocos.ebnf.*;
import de.monticore.umlcd4a.cocos.mcg.*;
import de.monticore.umlcd4a.cocos.mcg2ebnf.*;
import de.monticore.umlcd4a.prettyprint.CDPrettyPrinterConcreteVisitor;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import exceptions.*;
import generator.CD4ACodeGenerator;
import javafx.stage.Stage;
import misc.OctoPair;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Graph;
import model.GraphElement;
import model.edges.*;
import model.edges.AbstractEdge.Direction;
import model.nodes.AbstractNode;
import model.nodes.ClassNode;
import model.nodes.Node;
import model.nodes.PackageNode;
import view.nodes.AbstractNodeView;

public class CD4APlugin implements MontiCorePlugIn {

//test
	private CD4AnalysisNodeFactory cd4aFactory;
	private TypesNodeFactory typesFactory;
	private ASTCDCompilationUnit cdCompUnit;
	private String usageFolderPath;
	private static final CD4APlugin singleTonPlugin = new CD4APlugin();
	private List<OctoPair<GraphElement, ASTNode>> mapGraphAST = new ArrayList<>();
	
	private CD4APlugin() {
		
	}
	
	public static CD4APlugin getInstance() {
		return singleTonPlugin;
	}

	@Override
	public AbstractDiagramController getController() {
		try {
			Class<?> c = getClass().getClassLoader().loadClass("controller.CD4AController");
			CD4AController adc = (CD4AController) c.newInstance();
			return adc;
		} catch (Exception e) {
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
	public ASTCDCompilationUnit shapeToAST(Graph graph, List<String> containerInfoList) {
		// create AST via factory

		// elements for creating ASTCDCompilationUnit
		List<String> r__packages = new ArrayList<>();
		String allImports = containerInfoList.get(1);
		List<ASTImportStatement> imports;
		if (!(allImports == null)) {
			imports = new ArrayList<>();
			String[] arr = allImports.split(";");
			for (String s : arr) {
				List<String> createImportList = new ArrayList<>();
				createImportList.add(s);
				if (s.endsWith("*")) {
					ASTImportStatement importStatement = typesFactory.createASTImportStatement(createImportList, true);
					imports.add(importStatement);
				} else {
					ASTImportStatement importStatement = typesFactory.createASTImportStatement(createImportList, false);
					imports.add(importStatement);
				}
			}
		} else {
			imports = new ArrayList<>();
		}
		r__packages.add(containerInfoList.get(0));

		// elements for creating ASTCDDefinition
		String cdName = containerInfoList.get(2);
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
					String interfName = clazzName.split("<<interface>>")[1];
					List<Edge> edges = graph.getAllEdges();
					for (Edge e : edges) {
						if (e instanceof InheritanceEdge && e.getStartNode() == cNode) {
							String superName = e.getEndNode().getTitle();
							if (superName.contains("<<interface>>")) {
								superInterf.add(superName);
							}
						}
					}
					ASTCDInterface interf = createASTInterface((ClassNode) cNode, interfName, superInterf);
					interfazes.add(interf);
					OctoPair<GraphElement, ASTNode> pair = new OctoPair<>(cNode, interf);
					mapGraphAST.add(pair);

				} else if (clazzName.contains("<<abstract>>")) {
					// cNode is an abstract class
					List<Edge> edges = graph.getAllEdges();
					for (Edge e : edges) {
						if (e instanceof InheritanceEdge && e.getStartNode() == cNode) {
							String superName = e.getEndNode().getTitle();
							if (superName.contains("<<interface>>")) {
								superInterf.add(superName.split("<<interface>>")[1]);
							} else {
								superClazzes.add(superName);
							}
						}
					}

					ASTCDClass clazz = createASTClass((ClassNode) cNode, clazzName, true, superClazzes, superInterf);
					clazzes.add(clazz);
					OctoPair<GraphElement, ASTNode> pair = new OctoPair<>(cNode, clazz);
					mapGraphAST.add(pair);

				} else if (clazzName.contains("<<enum>>")) {
					// cNode is an enum
					String enomName = clazzName.split("<<enum>>")[1];
					ASTCDEnum enom = createASTEnum((ClassNode) cNode, enomName);
					enoms.add(enom);
					OctoPair<GraphElement, ASTNode> pair = new OctoPair<>(cNode, enom);
					mapGraphAST.add(pair);

				} else {
					// cNode is a class
					List<Edge> edges = graph.getAllEdges();
					for (Edge e : edges) {
						if (e instanceof InheritanceEdge && e.getStartNode() == cNode) {
							String superName = e.getEndNode().getTitle();
							if (superName.contains("<<interface>>")) {
								superInterf.add(superName.split("<<interface>>")[1]);
							} else {
								superClazzes.add(superName);
							}
						}
					}

					ASTCDClass clazz = createASTClass((ClassNode) cNode, clazzName, false, superClazzes, superInterf);
					clazzes.add(clazz);
					OctoPair<GraphElement, ASTNode> pair = new OctoPair<>(cNode, clazz);
					mapGraphAST.add(pair);
				}
			}
			if (cNode instanceof PackageNode) {
				// later in container dialog
			}
		}

		List<Edge> graphEdges = graph.getAllEdges();
		for (Edge e : graphEdges) {
			if (!(e instanceof InheritanceEdge)) {
				ASTCDAssociation cdAssoc = createASTCDAssociation(e);
				assocs.add(cdAssoc);
				OctoPair<GraphElement, ASTNode> pair = new OctoPair<>(e, cdAssoc);
				mapGraphAST.add(pair);
			}
		}

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

		List<ASTCDAttribute> interfAttributes;
		String nodeAttributes = node.getAttributes();
		if (nodeAttributes != null) {
			interfAttributes = new ArrayList<>();
			String[] arr = nodeAttributes.split(";");
			for (String s : arr) {
				ASTCDAttribute cdAttr = createASTCDAttribute(s);
				interfAttributes.add(cdAttr);
			}
		} else {
			interfAttributes = new ArrayList<>();
		}

		List<ASTCDMethod> interfMethods;
		String nodeOperations = node.getOperations();
		if (nodeOperations != null) {
			interfMethods = new ArrayList<>();
			String[] methodArr = nodeOperations.split(";");
			for (String s : methodArr) {
				s = s.replace("\n", "");
				ASTCDMethod method = createASTCDMethod(s);
				interfMethods.add(method);
			}
		} else {
			interfMethods = new ArrayList<>();
		}

		ASTCDInterface retInterf = cd4aFactory.createASTCDInterface(modifier, interfName, interfaces, interfAttributes,
				interfMethods);
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
		if (nodeAttributes != null) {
			enumConstants = new ArrayList<>();
			String[] arr = nodeAttributes.split(",");
			for (String s : arr) {
				List<ASTCDEnumParameter> enomParams = new ArrayList<>();
				ASTCDEnumConstant enomConst = cd4aFactory.createASTCDEnumConstant(s, enomParams);
				enumConstants.add(enomConst);
			}
		} else {
			enumConstants = new ArrayList<>();
		}

		String nodeOperations = node.getOperations();
		List<ASTCDConstructor> enumConstructors;
		if (nodeOperations != null) {
			String[] methodArr = nodeOperations.split(";");
			enumConstructors = new ArrayList<>();
			for (String s : methodArr) {
				if (s.contains(enumName)) {
					ASTCDConstructor constructor = createASTCDConstructor(enumName, s);
					enumConstructors.add(constructor);
				}
			}
		} else {
			enumConstructors = new ArrayList<>();
		}

		List<ASTCDMethod> enumMethods;
		if (nodeOperations != null) {
			String[] methodArr = nodeOperations.split(";");
			enumMethods = new ArrayList<>();
			for (String s : methodArr) {
				s = s.replace("\n", "");
				if (!s.contains(enumName)) {
					ASTCDMethod method = createASTCDMethod(s);
					enumMethods.add(method);
				}
			}
		} else {
			enumMethods = new ArrayList<>();
		}

		ASTCDEnum enom = cd4aFactory.createASTCDEnum(modifier, enumName, interfaces, enumConstants, enumConstructors,
				enumMethods);
		return enom;
	}

	@SuppressWarnings("static-access")
	private ASTCDClass createASTClass(ClassNode node, String name, boolean abstrakt, List<String> superClz,
			List<String> superInterf) {

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
		if (nodeAttributes != null) {
			clazzAttributes = new ArrayList<>();
			String[] arr = nodeAttributes.split(";");
			for (String s : arr) {
				ASTCDAttribute cdAttr = createASTCDAttribute(s);
				clazzAttributes.add(cdAttr);
			}
		} else {
			clazzAttributes = new ArrayList<>();
		}

		String nodeOperations = node.getOperations();

		// Constructors of the class if present else empty list
		List<ASTCDConstructor> clazzConstructors;
		if (nodeOperations != null) {
			clazzConstructors = new ArrayList<>();
			String[] methodArr = nodeOperations.split(";");
			for (String s : methodArr) {
				if (s.contains(clazzName)) {
					ASTCDConstructor constructor = createASTCDConstructor(clazzName, s);
					clazzConstructors.add(constructor);
				}
			}
		} else {
			clazzConstructors = new ArrayList<>();
		}

		// Methods of the class if present else empty list
		List<ASTCDMethod> clazzMethods;
		if (nodeOperations != null) {
			clazzMethods = new ArrayList<>();
			String[] methodArr = nodeOperations.split(";");
			for (String s : methodArr) {
				s = s.replace("\n", "");
				if (!s.contains(clazzName)) {
					ASTCDMethod method = createASTCDMethod(s);
					clazzMethods.add(method);
				}
			}
		} else {
			clazzMethods = new ArrayList<>();
		}

		ASTCDClass retClass;
		if (superClassNames.isEmpty()) {
			retClass = cd4aFactory.createASTCDClass(clazzModifier, clazzName, null, clazzInterfaces, clazzAttributes,
					clazzConstructors, clazzMethods);
		} else {
			retClass = cd4aFactory.createASTCDClass(clazzModifier, clazzName, clazzSuperclass, clazzInterfaces,
					clazzAttributes, clazzConstructors, clazzMethods);
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
				} catch (IOException e) {
					e.printStackTrace();
				}

				ASTType typeOfAttr = typeResult.get();

				ASTCDParameter constructParam;
				if (a.contains("...")) {
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
		ASTCDConstructor constructor = cd4aFactory.createASTCDConstructor(clazzModifier, name, constructParams,
				constructExcepts);
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
		} else {
			Optional<de.monticore.types.types._ast.ASTType> mRetTypeResult = null;
			try {
				mRetTypeResult = typeParser.parse_String(mRetTypeStr);
			} catch (IOException e) {
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
				} catch (IOException e) {
					e.printStackTrace();
				}

				ASTType typeOfAttr = typeResult.get();

				ASTCDParameter methodParam;
				if (a.contains("...")) {
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
		ASTCDMethod method = cd4aFactory.createASTCDMethod(clazzModifier, returnType, mName, methodParams,
				methodExcepts);
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
		} catch (IOException e) {
			e.printStackTrace();
		}

		ASTType typeOfAttr = typeResult.get();

		ASTCDAttribute cdAttr = cd4aFactory.createASTCDAttribute(clazzModifier, typeOfAttr, attrName, null);
		return cdAttr;
	}

	@SuppressWarnings("static-access")
	private ASTCDAssociation createASTCDAssociation(Edge e) {
		// Associations müssen immer einen Namen haben sonst Fehler !!!
		ASTStereotype stereotype = null;
		String name = "";
		ASTModifier leftModifier = cd4aFactory.createASTModifier();
		ASTCardinality leftCardinality;

		String leftNodeName = e.getStartNode().getTitle();
		String rightNodeName = e.getEndNode().getTitle();
		List<String> leftNameList = new ArrayList<>();
		List<String> rightNameList = new ArrayList<>();
		leftNameList.add(leftNodeName);
		rightNameList.add(rightNodeName);

		ASTQualifiedName leftReferenceName = typesFactory.createASTQualifiedName(leftNameList);
		ASTCDQualifier leftQualifier = null;

		// roles are not supported by OctoUML!
		String leftRole = "";
		String rightRole = "";

		// qualifier are not supported by OctoUML!
		ASTCDQualifier rightQualifier = null;

		ASTQualifiedName rightReferenceName = typesFactory.createASTQualifiedName(rightNameList);

		ASTCardinality rightCardinality;
		ASTModifier rightModifier = cd4aFactory.createASTModifier();
		boolean r__association = false;
		boolean r__composition = false;
		boolean r__derived = false;
		boolean leftToRight = false;
		boolean rightToLeft = false;
		boolean bidirectional = false;
		boolean unspecified = false;

		name = ((AbstractEdge) e).getLabel();

		String leftCard = ((AbstractEdge) e).getStartMultiplicity();
		if (leftCard.equals("*")) {
			leftCardinality = cd4aFactory.createASTCardinality(true, false, false, false);
		} else if (leftCard.equals("1")) {
			leftCardinality = cd4aFactory.createASTCardinality(false, true, false, false);
		} else if (leftCard.equals("1..*")) {
			leftCardinality = cd4aFactory.createASTCardinality(false, false, true, false);
		} else {
			leftCardinality = cd4aFactory.createASTCardinality(false, false, false, true);
		}

		String rightCard = ((AbstractEdge) e).getEndMultiplicity();
		if (rightCard.equals("*")) {
			rightCardinality = cd4aFactory.createASTCardinality(true, false, false, false);
		} else if (rightCard.equals("1")) {
			rightCardinality = cd4aFactory.createASTCardinality(false, true, false, false);
		} else if (rightCard.equals("1..*")) {
			rightCardinality = cd4aFactory.createASTCardinality(false, false, true, false);
		} else {
			rightCardinality = cd4aFactory.createASTCardinality(false, false, false, true);
		}

		if (e instanceof AssociationEdge) {
			r__association = true;
		}

		if (e instanceof AggregationEdge) {
			// so nicht ganz unterstützt in CD4A
			// in der Logik ersetzbar durch Association
			r__association = true;
		}

		if (e instanceof CompositionEdge) {
			r__composition = true;
		}

		Direction direc = ((AbstractEdge) e).getDirection();
		if (direc == Direction.NO_DIRECTION) {
			unspecified = true;
		}
		if (direc == Direction.START_TO_END) {
			leftToRight = true;
		}
		if (direc == Direction.END_TO_START) {
			rightToLeft = true;
		}
		if (direc == Direction.BIDIRECTIONAL) {
			bidirectional = true;
		}

		ASTCDAssociation assoc = cd4aFactory.createASTCDAssociation(stereotype, name, leftModifier, leftCardinality,
				leftReferenceName, leftQualifier, leftRole, rightRole, rightQualifier, rightReferenceName,
				rightCardinality, rightModifier, r__association, r__composition, r__derived, leftToRight, rightToLeft,
				bidirectional, unspecified);

		return assoc;
	}

	@Override
	public List<MontiCoreException> check(ASTNode node, HashMap<AbstractNodeView, AbstractNode> map) {
		List<MontiCoreException> errorList = new ArrayList<>();
		ASTCDCompilationUnit unit = (ASTCDCompilationUnit) node;
		ASTCDDefinition cdDef = unit.getCDDefinition();
		List<ASTCDClass> cdClasses = cdDef.getCDClasses();
		List<ASTCDInterface> cdInterfaces = cdDef.getCDInterfaces();
		List<ASTCDEnum> cdEnums = cdDef.getCDEnums();
		List<ASTCDAssociation> cdAssociations = cdDef.getCDAssociations();
		
		// check classes for errors
		for(ASTCDClass c : cdClasses) {
			AbstractNode currentNode = null;
			for(OctoPair<GraphElement, ASTNode> pair : mapGraphAST) {
				if(c.deepEquals(pair.getR())) {
					currentNode = (AbstractNode) pair.getL();
				}
			}
			if(c.getName() == null) {
				errorList.add(new ClassNameMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
			}
			List<ASTCDAttribute> classAttr = c.getCDAttributes();
			for(ASTCDAttribute a : classAttr) {
				if(a.getName() == null) {
					errorList.add(new ClassAttributeNameMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
				}
				if(a.getType() == null) {
					errorList.add(new ClassAttributeTypeMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
				}
			}
			List<ASTCDConstructor> classConstr = c.getCDConstructors();
			for(ASTCDConstructor con : classConstr) {
				if(con.getName() == null) {
					errorList.add(new ConstructorNameMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
				}
			}
			List<ASTCDMethod> classMethods = c.getCDMethods();
			for(ASTCDMethod m : classMethods) {
				if(m.getName() == null) {
					errorList.add(new MethodNameMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
				}
				if(m.getReturnType() == null) {
					errorList.add(new MethodReturnTypeMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
				}
				List<ASTCDParameter> params = m.getCDParameters();
				for(ASTCDParameter p : params) {
					if(p.getName() == null) {
						errorList.add(new MethodParameterNameMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
					}
					if(p.getType() == null) {
						errorList.add(new MethodParameterTypeMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
					}
				}
			}
		}
		
		// check interfaces for errors
		for(ASTCDInterface i : cdInterfaces) {
			AbstractNode currentNode = null;
			for(OctoPair<GraphElement, ASTNode> pair : mapGraphAST) {
				if(i.deepEquals(pair.getR())) {
					currentNode = (AbstractNode) pair.getL();
				}
			}
			if(i.getName() == null) {
        errorList.add(new InterfaceNameMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
      }
			List<ASTCDAttribute> classAttr = i.getCDAttributes();
      for(ASTCDAttribute a : classAttr) {
        if(a.getName() == null) {
          errorList.add(new ClassAttributeNameMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
        }
        if(a.getType() == null) {
          errorList.add(new ClassAttributeTypeMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
        }
      }
      List<ASTCDMethod> classMethods = i.getCDMethods();
      for(ASTCDMethod m : classMethods) {
        if(m.getName() == null) {
          errorList.add(new MethodNameMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
        }
        if(m.getReturnType() == null) {
          errorList.add(new MethodReturnTypeMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
        }
        List<ASTCDParameter> params = m.getCDParameters();
        for(ASTCDParameter p : params) {
          if(p.getName() == null) {
            errorList.add(new MethodParameterNameMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
          }
          if(p.getType() == null) {
            errorList.add(new MethodParameterTypeMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
          }
        }
      }
		}
		
		// check enums for errors
		for(ASTCDEnum e : cdEnums) {
			AbstractNode currentNode = null;
			for(OctoPair<GraphElement, ASTNode> pair : mapGraphAST) {
				if(e.deepEquals(pair.getR())) {
					currentNode = (AbstractNode) pair.getL();
				}
			}
			if(e.getName() == null) {
        errorList.add(new EnumNameMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
      }
			List<ASTCDEnumConstant> enumConstants = e.getCDEnumConstants();
			for(ASTCDEnumConstant ec : enumConstants) {
			  if(ec.getName() == null) {
			    errorList.add(new EnumConstantNameMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
			  }
			}
			
			List<ASTCDMethod> classMethods = e.getCDMethods();
      for(ASTCDMethod m : classMethods) {
        if(m.getName() == null) {
          errorList.add(new MethodNameMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
        }
        if(m.getReturnType() == null) {
          errorList.add(new MethodReturnTypeMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
        }
        List<ASTCDParameter> params = m.getCDParameters();
        for(ASTCDParameter p : params) {
          if(p.getName() == null) {
            errorList.add(new MethodParameterNameMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
          }
          if(p.getType() == null) {
            errorList.add(new MethodParameterTypeMissingException(currentNode, getCorrespondingNodeView(currentNode, map)));
          }
        }
      }
			
		}
		
		// check associations for errors
		for(ASTCDAssociation a : cdAssociations) {
			Edge currentEdge = null;
			for(OctoPair<GraphElement, ASTNode> pair : mapGraphAST) {
				if(a.deepEquals(pair.getR())) {
					currentEdge = (Edge) pair.getL();
				}
			}
			AbstractEdge abstrEdge = (AbstractEdge) currentEdge;
			if(abstrEdge.getStartNode().getTitle() == null) {
			  errorList.add(new AssocLeftRefNameMissingException(abstrEdge.getStartNode(), getCorrespondingNodeView(abstrEdge.getStartNode(), map)));
			}
			if(abstrEdge.getEndNode().getTitle() == null) {
        errorList.add(new AssocRightRefNameMissingException(abstrEdge.getEndNode(), getCorrespondingNodeView(abstrEdge.getEndNode(), map)));
      }
		}
		
		// check existing CoCos
		/*
		CD4AnalysisCoCoChecker cocoChecker = new CD4AnalysisCoCoChecker();
		cocoChecker.addCoCo(new AssociationNameLowerCase());
		cocoChecker.addCoCo(new AssociationNameNoConflictWithAttribute());
		cocoChecker.addCoCo(new AssociationNameUnique());
		cocoChecker.addCoCo(new AssociationOrderedCardinalityGreaterOne());
		cocoChecker.addCoCo(new AssociationQualifierAttributeExistsInTarget());
		cocoChecker.addCoCo(new AssociationQualifierOnCorrectSide());
		cocoChecker.addCoCo(new AssociationQualifierTypeExists());
		cocoChecker.addCoCo(new AssociationRoleNameLowerCase());
		cocoChecker.addCoCo(new AssociationRoleNameNoConflictWithAttribute());
		cocoChecker.addCoCo(new AssociationRoleNameNoConflictWithOtherRoleNames());
		cocoChecker.addCoCo(new AssociationSourceNotEnum());
		cocoChecker.addCoCo(new AssociationSourceTypeNotExternal());
		cocoChecker.addCoCo(new AssociationSourceTypeNotGenericChecker());
		cocoChecker.addCoCo(new AssociationSrcAndTargetTypeExistChecker());
		cocoChecker.addCoCo(new AttributeNameLowerCase());
		cocoChecker.addCoCo(new AttributeOverriddenTypeMatch());
		cocoChecker.addCoCo(new AttributeTypeCompatible());
		cocoChecker.addCoCo(new AttributeTypeExists());
		cocoChecker.addCoCo(new AttributeUniqueInClassCoco());
		cocoChecker.addCoCo(new ClassExtendExternalType());
		cocoChecker.addCoCo(new ClassExtendsOnlyClasses());
		cocoChecker.addCoCo(new ClassImplementOnlyInterfaces());
		cocoChecker.addCoCo(new CompositionCardinalityValid());
		cocoChecker.addCoCo(new DiagramNameUpperCase());
		cocoChecker.addCoCo(new EnumConstantsUnique());
		cocoChecker.addCoCo(new EnumImplementOnlyInterfaces());
		cocoChecker.addCoCo(new ExtendsNotCyclic());
		cocoChecker.addCoCo(new GenericParameterCountMatch());
		cocoChecker.addCoCo(new GenericsNotNested());
		cocoChecker.addCoCo(new GenericTypeHasParameters());
		cocoChecker.addCoCo(new InterfaceExtendsOnlyInterfaces());
		cocoChecker.addCoCo(new TypeNameUpperCase());
		cocoChecker.addCoCo(new TypeNoInitializationOfDerivedAttribute());
		cocoChecker.addCoCo(new UniqueTypeNames());
		cocoChecker.addCoCo(new AssociationModifierCoCo());
		cocoChecker.addCoCo(new AttributeNotAbstractCoCo());
		cocoChecker.addCoCo(new ClassInvalidModifiersCoCo());
		cocoChecker.addCoCo(new EnumInvalidModifiersCoCo());
		cocoChecker.addCoCo(new InterfaceInvalidModifiersCoCo());
		cocoChecker.addCoCo(new ModifierNotMultipleVisibilitiesCoCo());
		cocoChecker.addCoCo(new AssociationEndModifierRestrictionCoCo());
		cocoChecker.addCoCo(new AssociationNoStereotypesCoCo());
		cocoChecker.addCoCo(new AttributeModifierOnlyDerivedCoCo());
		cocoChecker.addCoCo(new ClassModifierOnlyAbstractCoCo());
		cocoChecker.addCoCo(new ClassNoConstructorsCoCo());
		cocoChecker.addCoCo(new ClassNoMethodsCoCo());
		cocoChecker.addCoCo(new EnumNoConstructorsCoCo());
		cocoChecker.addCoCo(new EnumConstantNameUpperCase());
		cocoChecker.addCoCo(new EnumNoConstructorsCoCo());
		cocoChecker.addCoCo(new EnumNoMethodsCoCo());
		cocoChecker.addCoCo(new EnumNoModifierCoCo());
		cocoChecker.addCoCo(new InterfaceNoAttributesCoCo());
		cocoChecker.addCoCo(new InterfaceNoMethodsCoCo());
		cocoChecker.addCoCo(new InterfaceNoModifierCoCo());
		cocoChecker.addCoCo(new StereoValueNoValueCoCo());
		
		cocoChecker.checkAll(cdCompUnit);
		/*List<Finding> allCoCoLogs = Log.getFindings();
		for(Finding f : allCoCoLogs) {
		  errorList.add(new CoCoException(null, null, f.getMsg()));
		}*/
		
		return errorList;
	}

	@Override
	public String getGenerator() {
		// own generator because cd4a provides none
		return null;
	}

	@Override
	public boolean generateCode(ASTNode node, String packageName, String path) {
		CD4ACodeGenerator cd4aGenerator = CD4ACodeGenerator.getInstance();
		cd4aGenerator.generate((ASTCDCompilationUnit) node, packageName, path);
		if(cd4aGenerator.wasSuccessfull()) {
		  return true;
		} else {
		  return false;
		}
	}

	private AbstractNodeView getCorrespondingNodeView(Node n, HashMap<AbstractNodeView, AbstractNode> map) {
    AbstractNodeView view = null;
    for (Entry<AbstractNodeView, AbstractNode> entry : map.entrySet()) {
      if (n.equals(entry.getValue())) {
        view = entry.getKey();
      }
    }
    return view;
  }
	
	@Override
	public String getFlagName() {
		return "CD";
	}

	@Override
	public void addUMLFlag(String name) {
		// todo later on

	}

	@Override
	public List<String> showContainerInfoDialog(Stage stage, List<String> infoNames, List<String> infoList) {
		List<String> resList = new ArrayList<>();
		Dialog<List<String>> dialog = new Dialog<List<String>>();
		dialog.setTitle("Enter Container Info");

		DialogPane pane = new DialogPane();
		GridPane gridPane = new GridPane();
		/* alte Vorgehensweise
		Label packageLbl = new Label("Enter package name:");
		Label importLbl = new Label("Enter imports:");
		Label nameLbl = new Label("Enter classdiagram name:");
		TextField packageTF = new TextField(infoList.get(0));
		TextField importTF = new TextField(infoList.get(1));
		TextField nameTF = new TextField(infoList.get(2));
		gridPane.getColumnConstraints().add(new ColumnConstraints(170));
		gridPane.getColumnConstraints().add(new ColumnConstraints(200));
		gridPane.setHgap(30);
		gridPane.setVgap(10);
		gridPane.add(packageLbl, 0, 0);
		gridPane.add(packageTF, 1, 0);
		gridPane.add(importLbl, 0, 1);
		gridPane.add(importTF, 1, 1);
		gridPane.add(nameLbl, 0, 2);
		gridPane.add(nameTF, 1, 2);

		pane.getChildren().add(gridPane);
		pane.setPrefSize(400, 160);
		pane.getButtonTypes().add(ButtonType.OK);
		dialog.setDialogPane(pane);
		dialog.initOwner(stage);
		Optional<List<String>> result = dialog.showAndWait();
		if (result.isPresent()) {
			resList.add(packageTF.getText());
			resList.add(importTF.getText());
			resList.add(nameTF.getText());
		}
		*/
		for(int i=0; i<infoNames.size(); i++) {
		  String name = infoNames.get(i);
		  Label lbl = new Label("Enter " + name + ":");
		  TextField tf = new TextField(infoList.get(i));
		  gridPane.add(lbl, 0, i);
		  gridPane.add(tf, 1, i);
		}
		
	  gridPane.getColumnConstraints().add(new ColumnConstraints(170));
	  gridPane.getColumnConstraints().add(new ColumnConstraints(200));
    gridPane.setHgap(30);
    gridPane.setVgap(10);
    pane.getChildren().add(gridPane);
    pane.setPrefSize(400, 160);
    pane.getButtonTypes().add(ButtonType.OK);
    dialog.setDialogPane(pane);
    dialog.initOwner(stage);
    Optional<List<String>> result = dialog.showAndWait();
    if (result.isPresent()) {
      for(javafx.scene.Node t : gridPane.getChildren()) {
        if(t instanceof TextField) {
          resList.add(((TextField)t).getText());
        }
      }
    }
		
		return resList;
	}

	public String getUsageFolderPath() {
		return usageFolderPath;
	}

	public void setUsageFolderPath(String usageFolderPath) {
		this.usageFolderPath = usageFolderPath;
	}
}
