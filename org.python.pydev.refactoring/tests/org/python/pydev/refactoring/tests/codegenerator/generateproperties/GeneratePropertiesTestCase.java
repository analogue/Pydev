package org.python.pydev.refactoring.tests.codegenerator.generateproperties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.python.pydev.refactoring.ast.adapters.ClassDefAdapter;
import org.python.pydev.refactoring.ast.adapters.ModuleAdapter;
import org.python.pydev.refactoring.ast.visitors.VisitorFactory;
import org.python.pydev.refactoring.codegenerator.generateproperties.edit.DeleteMethodEdit;
import org.python.pydev.refactoring.codegenerator.generateproperties.edit.GetterMethodEdit;
import org.python.pydev.refactoring.codegenerator.generateproperties.edit.PropertyEdit;
import org.python.pydev.refactoring.codegenerator.generateproperties.edit.SetterMethodEdit;
import org.python.pydev.refactoring.codegenerator.generateproperties.request.GeneratePropertiesRequest;
import org.python.pydev.refactoring.codegenerator.generateproperties.request.SelectionState;
import org.python.pydev.refactoring.tests.core.AbstractIOTestCase;

import com.thoughtworks.xstream.XStream;

public class GeneratePropertiesTestCase extends AbstractIOTestCase {

	private ArrayList<TextEdit> multiEdit;

	public GeneratePropertiesTestCase(String name) {
		super(name);
	}

	protected void addEdit(TextEdit edit) {
		multiEdit.add(edit);
	}

	@Override
	public void runTest() throws Throwable {
		MockupGeneratePropertiesConfig config = initConfig();

		MockupGeneratePropertiesRequestProcessor requestProcessor = setupRequestProcessor(config);

		IDocument refactoringDoc = applyGenerateProperties(requestProcessor);

		this.setTestGenerated(refactoringDoc.get());
		assertEquals(getExpected(), getGenerated());
	}

	private IDocument applyGenerateProperties(MockupGeneratePropertiesRequestProcessor requestProcessor) throws BadLocationException {
		IDocument refactoringDoc = new Document(getSource());
		MultiTextEdit multi = new MultiTextEdit();
		for (GeneratePropertiesRequest req : requestProcessor.getRefactoringRequests()) {
			SelectionState state = req.getSelectionState();

			if (state.isGetter()) {
				multi.addChild(new GetterMethodEdit(req).getEdit());
			}
			if (state.isSetter()) {
				multi.addChild(new SetterMethodEdit(req).getEdit());
			}
			if (state.isDelete()) {
				multi.addChild(new DeleteMethodEdit(req).getEdit());
			}
			multi.addChild(new PropertyEdit(req).getEdit());
		}
		multi.apply(refactoringDoc);
		return refactoringDoc;
	}

	private MockupGeneratePropertiesRequestProcessor setupRequestProcessor(MockupGeneratePropertiesConfig config) throws Throwable {
		ModuleAdapter module = VisitorFactory.createModuleAdapter(null, null, new Document(getSource()));
		List<ClassDefAdapter> classes = module.getClasses();
		assertTrue(classes.size() > 0);

		MockupGeneratePropertiesRequestProcessor requestProcessor = new MockupGeneratePropertiesRequestProcessor(module, config);
		return requestProcessor;
	}

	private MockupGeneratePropertiesConfig initConfig() {
		MockupGeneratePropertiesConfig config = null;
		XStream xstream = new XStream();
		xstream.alias("config", MockupGeneratePropertiesConfig.class);

		if (getConfig().length() > 0) {
			config = (MockupGeneratePropertiesConfig) xstream.fromXML(getConfig());
		} else {
			fail("Could not unserialize configuration");
		}
		return config;
	}

	@Override
	public String getExpected() {
		return getResult();
	}

}