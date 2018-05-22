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
import exceptions.AssocLeftRefNameMissingException;
import exceptions.AssocRightRefNameMissingException;
import exceptions.ClassAttributeNameMissingException;
import exceptions.ClassAttributeTypeMissingException;
import exceptions.ClassNameMissingException;
import exceptions.ConstructorNameMissingException;
import exceptions.EnumConstantNameMissingException;
import exceptions.EnumNameMissingException;
import exceptions.InterfaceNameMissingException;
import exceptions.MethodNameMissingException;
import exceptions.MethodParameterNameMissingException;
import exceptions.MethodParameterTypeMissingException;
import exceptions.MethodReturnTypeMissingException;
import generator.CD4ACodeGenerator;
import javafx.stage.Stage;
import misc.OctoPair;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Graph;
import model.GraphElement;
import model.edges.*;
import model.edges.AbstractEdge.Direction;
import model.nodes.AbstractNode;
import model.nodes.ClassNode;
import model.nodes.PackageNode;
import view.nodes.AbstractNodeView;

public class CD4APlugin implements MontiCorePlugIn {

	private CD4AnalysisNodeFactory cd4aFactory;
	private TypesNodeFactory typesFactory;
	private ASTCDCompilationUnit cdCompUnit;
	private String usageFolderPath;
	private static final CD4APlugin singleTonPlugin = new CD4APlugin();
	private List<OctoPair<GraphElement, ASTNode>> mapGraphAST = new ArrayList<>();
	private List<OctoPair<GraphElement, Group>> mapNodeToView = new ArrayList<>();
	
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
				String[] arr2 = s.split(".");
				List<String> createImportList = new ArrayList<>();
				createImportList.add(s);
				if (arr2[arr2.length - 1].equals("*")) {
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
	public List<MontiCoreException> check(ASTNode node) {
		//TODO add actionlistener to exception pane to handle click -> mark corresponding node/edge
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
				errorList.add(new ClassNameMissingException(currentNode));
			}
			List<ASTCDAttribute> classAttr = c.getCDAttributes();
			for(ASTCDAttribute a : classAttr) {
				if(a.getName() == null) {
					errorList.add(new ClassAttributeNameMissingException(currentNode));
				}
				if(a.getType() == null) {
					errorList.add(new ClassAttributeTypeMissingException(currentNode));
				}
			}
			List<ASTCDConstructor> classConstr = c.getCDConstructors();
			for(ASTCDConstructor con : classConstr) {
				if(con.getName() == null) {
					errorList.add(new ConstructorNameMissingException(currentNode));
				}
			}
			List<ASTCDMethod> classMethods = c.getCDMethods();
			for(ASTCDMethod m : classMethods) {
				if(m.getName() == null) {
					errorList.add(new MethodNameMissingException(currentNode));
				}
				if(m.getReturnType() == null) {
					errorList.add(new MethodReturnTypeMissingException(currentNode));
				}
				List<ASTCDParameter> params = m.getCDParameters();
				for(ASTCDParameter p : params) {
					if(p.getName() == null) {
						errorList.add(new MethodParameterNameMissingException(currentNode));
					}
					if(p.getType() == null) {
						errorList.add(new MethodParameterTypeMissingException(currentNode));
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
        errorList.add(new InterfaceNameMissingException(currentNode));
      }
			List<ASTCDAttribute> classAttr = i.getCDAttributes();
      for(ASTCDAttribute a : classAttr) {
        if(a.getName() == null) {
          errorList.add(new ClassAttributeNameMissingException(currentNode));
        }
        if(a.getType() == null) {
          errorList.add(new ClassAttributeTypeMissingException(currentNode));
        }
      }
      List<ASTCDMethod> classMethods = i.getCDMethods();
      for(ASTCDMethod m : classMethods) {
        if(m.getName() == null) {
          errorList.add(new MethodNameMissingException(currentNode));
        }
        if(m.getReturnType() == null) {
          errorList.add(new MethodReturnTypeMissingException(currentNode));
        }
        List<ASTCDParameter> params = m.getCDParameters();
        for(ASTCDParameter p : params) {
          if(p.getName() == null) {
            errorList.add(new MethodParameterNameMissingException(currentNode));
          }
          if(p.getType() == null) {
            errorList.add(new MethodParameterTypeMissingException(currentNode));
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
        errorList.add(new EnumNameMissingException(currentNode));
      }
			List<ASTCDEnumConstant> enumConstants = e.getCDEnumConstants();
			for(ASTCDEnumConstant ec : enumConstants) {
			  if(ec.getName() == null) {
			    errorList.add(new EnumConstantNameMissingException(currentNode));
			  }
			}
			
			List<ASTCDMethod> classMethods = e.getCDMethods();
      for(ASTCDMethod m : classMethods) {
        if(m.getName() == null) {
          errorList.add(new MethodNameMissingException(currentNode));
        }
        if(m.getReturnType() == null) {
          errorList.add(new MethodReturnTypeMissingException(currentNode));
        }
        List<ASTCDParameter> params = m.getCDParameters();
        for(ASTCDParameter p : params) {
          if(p.getName() == null) {
            errorList.add(new MethodParameterNameMissingException(currentNode));
          }
          if(p.getType() == null) {
            errorList.add(new MethodParameterTypeMissingException(currentNode));
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
			  errorList.add(new AssocLeftRefNameMissingException(abstrEdge.getStartNode()));
			}
			if(abstrEdge.getEndNode().getTitle() == null) {
        errorList.add(new AssocRightRefNameMissingException(abstrEdge.getEndNode()));
      }
		}
		
		return errorList;
	}

	@Override
	public String getGenerator() {
		// own generator because cd4a provides none
		return null;
	}

	@Override
	public boolean generateCode(ASTNode node, String path) {
		CD4ACodeGenerator cd4aGenerator = CD4ACodeGenerator.getInstance();
		cd4aGenerator.generate((ASTCDCompilationUnit) node, path);
		if(cd4aGenerator.wasSuccessfull()) {
		  return true;
		} else {
		  return false;
		}
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
	public List<String> showContainerInfoDialog(Stage stage, List<String> infoList) {
		List<String> resList = new ArrayList<>();
		Dialog<List<String>> dialog = new Dialog<List<String>>();
		dialog.setTitle("Enter Container Info");

		DialogPane pane = new DialogPane();
		GridPane gridPane = new GridPane();
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
		return resList;
	}

	public String getUsageFolderPath() {
		return usageFolderPath;
	}

	public void setUsageFolderPath(String usageFolderPath) {
		this.usageFolderPath = usageFolderPath;
	}
	
	public void mapViewToNode(Graph graph) {
	  //TODO
	}

}
