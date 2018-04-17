package plugin;

import java.util.List;

import controller.AbstractDiagramController;
import controller.CD4AController;
import de.monticore.ast.ASTNode;
import de.monticore.prettyprint.IndentPrinter;
import model.Graph;

public class CD4APlugin implements MontiCorePlugIn {

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
	public IndentPrinter getPrettyPrinter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class getASTNode() {
		return null;
	}

	@Override
	public ASTNode shapeToAST(Graph graph) {
		// TODO Auto-generated method stub
		return null;
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

}
