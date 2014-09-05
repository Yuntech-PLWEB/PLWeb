package org.plweb.jedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.plweb.suite.common.xml.XProject;
import org.plweb.suite.common.xml.XTask;

import chrriis.dj.nativeswing.swtimpl.components.HTMLEditorSaveEvent;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;



import javax.swing.SwingUtilities;
import chrriis.dj.nativeswing.swtimpl.NativeComponent;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;

public class CompilerUserInterface extends JPanel implements ActionListener {

	private ProjectEnvironment env = ProjectEnvironment.getInstance();

	private static final long serialVersionUID = 3256720676110022706L;

	private CompilerRunner runner;

	private JComboBox comboTask;
	private JComboBox comboMode;

	private MessageManager mm = MessageManager.getInstance();

	private MessageConsoleInterface console = MessageConsole.getInstance();

	private BufferChangeListener bcl = BufferChangeListener.getInstance();
	
	private MasteryCore masteryCore;
	
	private JToolBar tb1;
	private Boolean isSubmit[];
	private JToolBar tb2ForSubmit;
	
	private JSONObject _stuGrade;
	private Boolean isInterrupt = false;
	private Encryption fileEncrypt;
	
	private int idx;
	private JLabel toolLabel;
	

	public CompilerUserInterface() throws Exception {
		setBorder(BorderFactory.createEmptyBorder(5, 5, 3, 3));
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(400, 300));

		JPanel p = new JPanel(new BorderLayout());
		p.add(createConsoleTextPane(), BorderLayout.CENTER);
		add(p, BorderLayout.CENTER);

		String[] tasks = {};
		String[] modes;

		if (env.getLessonMode().equals("author")) {
			modes = new String[] { "author", "student", "teacher" };
		} else if (env.getLessonMode().equals("teacher")) {
			modes = new String[] { "teacher", "student" };
		} else {
			modes = new String[] { env.getLessonMode() };
		}

		
		tb1 = new JToolBar();
		tb1.setFloatable(false);
		if(env.getLessonMode().equals("student") && (env.getActiveProject().getPropertyEx("hasMastery") != null && env.getActiveProject().getPropertyEx("hasMastery").equals("true"))){
			toolLabel = new JLabel(" ");
			tb1.addSeparator();
			tb1.add(toolLabel);
		} else {
			tb1.add(createButton("上一題", "control_rewind.png", "task.previous", false));
			tb1.add(comboTask = createComboBox(tasks, "task.select"));
			tb1.add(createButton("下一題", "control_fastforward.png", "task.next", false));
		}
		
		JToolBar tb2 = new JToolBar();
		tb2.setFloatable(false);

		if (env.getLessonMode().equals("author")) {
			tb2.add(createButton("upload project", "database_save.png",
					"project.upload"));
			tb2.add(new JToolBar.Separator());
		}
		tb2.add(comboMode = createComboBox(modes, "mode.select"));
		tb2.add(new JToolBar.Separator());
		
		tb2.add(createButton("還原原始程式碼", "reset.png",
				"task.reset"));
		
		tb2.add(createButton("reload exercise", "arrow_refresh.png",
				"task.reload"));

		if (env.getLessonMode().equals("author")) {
			tb2.add(createButton("mastery learning", "mastery_learning.png", "masteryLearning.edit"));
			tb2.add(createButton("open explorer", "drive.png", "explorer.open"));

			tb2.add(createButton("edit project", "book_edit.png",
					"project.edit"));

			tb2.add(createButton("html editor", "world_edit.png", "html.edit"));

			tb2.add(new JToolBar.Separator());

			tb2.add(createButton("edit exercise", "page_edit.png", "task.edit"));
			tb2.add(createButton("add exercise", "page_add.png", "task.add"));
			tb2.add(createButton("delete exercise", "page_delete.png",
					"task.delete"));
			tb2.add(createButton("move up exercise", "arrow_up.png", "task.up"));
			tb2.add(createButton("move down exercise", "arrow_down.png",
					"task.down"));
		}

		JPanel p3 = new JPanel(new BorderLayout());
		p3.add(tb1, BorderLayout.CENTER);
		p3.add(tb2, BorderLayout.WEST);

		add(p3, BorderLayout.NORTH);

		// �Ȯ�, �ѨM�s���
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException ex) {
		// ex.printStackTrace();
		// }
		
		// default the project doesn't have exam file.
		XProject project = env.getActiveProject();
		if(project.getPropertyEx("hasExamFile") == null)
			project.setProperty("hasExamFile", "false");
			
		// clear system copy clipboard
		clearCopyClipBoard();
		
		// check project if has Exam file & if Exam mode
		if(env.getIsExam().equals("true") && project.getPropertyEx("hasExamFile").equals("true")){
			tb2ForSubmit = new JToolBar();
			tb2ForSubmit.setFloatable(false);
			tb2ForSubmit.addSeparator();
			tb2ForSubmit.add(createButton("提交此題", "submit.png", "task.submit", false));
			try {
				//check whitch task has submited.
				String gradeString = mm.getGrade(env.getClassId(), env.getCourseId(), env.getLessonId(), env.getUserId());
				JSONParser parser = new JSONParser();
				
				_stuGrade = (JSONObject) parser.parse(gradeString);
				isSubmit = new Boolean[_stuGrade.size()];
				for(int i = 0; i < _stuGrade.size(); i++){
					if((_stuGrade.get(String.valueOf(i + 1)).toString()).equals("false"))
						isSubmit[i] = Boolean.valueOf(_stuGrade.get(String.valueOf(i + 1)).toString());
					else
						isSubmit[i] = true;
				}
			} catch(ParseException e) {
			}
		}
		
		if(env.getIsExam().equals("true"))
			detectTaskMgr();
			
		// descrypt file(*.exam.enc)	
		if(env.getLessonMode().equals("author")){
			//XProject project = env.getActiveProject();
			File _directory = new File(project.getRootPath());
			File[] files = _directory.listFiles();
			File del;
			
			fileEncrypt = new Encryption();
			for(File file : files) {
				if(file.getName().endsWith(".exam.enc") || file.getName().endsWith(".cond2.enc")){
					fileEncrypt.decrypt(project.getRootPath() + "\\" + file.getName());
					
					del = new File(project.getRootPath() + "\\" + file.getName());
					del.delete();				
				}
			}
			
		}
		
		// check if project has Mastery set & get
		if((project.getPropertyEx("hasMastery") != null && project.getPropertyEx("hasMastery").equals("true")) && env.getLessonMode().equals("author")){
			String _masteryString = mm.getMasterySet(env.getCourseId(), env.getLessonId());
			MasteryLearningSet.getInstance().setMasterySet(_masteryString);
		} else if((project.getPropertyEx("hasMastery") != null && project.getPropertyEx("hasMastery").equals("true")) && env.getLessonMode().equals("student")) {
			//String _stuMastery = mm.getStuMastery(env.getClassId(), env.getCourseId(), env.getLessonId(), env.getUserId());
			///////////////////////////////////////////////
			masteryCore = MasteryCore.getInstance();
			
		}
		
		/*
		 * First Time reload
		 */
		 if(env.getLessonMode().equals("author") || (env.getActiveProject().getPropertyEx("hasMastery") != null && env.getActiveProject().getPropertyEx("hasMastery").equals("false")) || env.getActiveProject().getPropertyEx("hasMastery") == null)
			refreshTaskComboBox();
		reloadTask();
	}
	
	// in ExamMode detect taskmgr.exe
	private void detectTaskMgr(){
		Runnable runnable = new Runnable() {
			Process killProcess;
			public void run() {
				while(true){
					try {
						killProcess = Runtime.getRuntime().exec("taskkill /f /im taskmgr.exe");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					} catch (IOException e) {
					}
				}
			}
		};
		new Thread(runnable).start();
	}
	
	// clear system copy clipboard
	private void clearCopyClipBoard(){
		try {
			java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(new Transferable() {
				public DataFlavor[] getTransferDataFlavors() {
					return new DataFlavor[0];
				}
				public boolean isDataFlavorSupported(DataFlavor flavor) {
					return false;
				}
				public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
					throw new UnsupportedFlavorException(flavor);
				}
			}, null);
		} catch (IllegalStateException e){
		}
		
	}
	

	private JComponent createConsoleTextPane() {
		JTextPane textPaneConsole = new JTextPane();
		((MessageConsole) console).setTextPaneConsole(textPaneConsole);
		env.setActiveConsole(console);

		textPaneConsole.setFont(new Font("Courier New", Font.PLAIN, 13));
		textPaneConsole.setForeground(Color.BLACK);
		textPaneConsole.setBackground(Color.WHITE);
		textPaneConsole.setEditable(false);

		return new JScrollPane(textPaneConsole);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		System.out.println("Action Performed: ".concat(cmd));
		if (cmd.equals("task.select")) {
			reloadTask();
		} else if (cmd.equals("explorer.open")) {
			openExplorer();
		} else if (cmd.equals("task.up")) {
			upTask();
		} else if (cmd.equals("task.down")) {
			downTask();
		} else if (cmd.equals("task.add")) {
			addTask();
		} else if (cmd.equals("task.delete")) {
			delTask();
		} else if (cmd.equals("task.reload")) {
			reloadTask();
		} else if (cmd.equals("task.previous")) {
			//int idx = comboTask.getSelectedIndex();
			setIndex();
			if (idx > 0) {
				comboTask.setSelectedIndex(idx - 1);
			}
			reloadTask();
		} else if (cmd.equals("task.next")) {
			//int idx = comboTask.getSelectedIndex();
			setIndex();
			if (idx + 1 < comboTask.getItemCount()) {
				comboTask.setSelectedIndex(idx + 1);
			}
			reloadTask();
		} else if (cmd.equals("task.edit")) {
			//int idx = comboTask.getSelectedIndex();
			setIndex();
			if (idx >= 0) {
				XTask task = env.getActiveProject().getTasks().get(idx);
				if (task != null) {
					new FrameTaskEditor(this, task);
				}
			}
		} else if (cmd.equals("project.upload")) {
			try{
				saveProject();
			} catch (Exception _e){
			}
		} else if (cmd.equals("project.edit")) {
			new FrameProjectEditor(env.getActiveProject());
		} else if (cmd.equals("html.edit")) {
			html();
		} else if (cmd.equals("task.submit")) {
			int dialogResult = JOptionPane.showConfirmDialog(null, "若提交後則無法再修改程式碼，確認提交？", "Submit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(dialogResult == JOptionPane.YES_OPTION){
				submitTask();
			}	
		} else if (cmd.equals("masteryLearning.edit")){
		
			MasteryLearningSet.getInstance().displayPanel();
			
		} else if (cmd.equals("task.reset")) {
			//從 .part 載入
			//System.out.println("task.reset!!");
			//int idx = comboTask.getSelectedIndex();
			setIndex();
			if (idx >= 0) {
				XProject project = env.getActiveProject();
				XTask task = project.getTasks().get(idx);
				if (task != null) {
					String fileMain = project.getTaskPropertyEx(task, "file.main");
					String filePart = project.getTaskPropertyEx(task, "file.part");
					if (fileMain != null) {
						
						String rootPath = project.getRootPath();
						
						String filePath = new File(rootPath, fileMain).getPath();
						Buffer buffer = jEdit.getBuffer(filePath);
						buffer.remove(0, buffer.getLength());
						
						String filePathSrc = new File(rootPath, filePart).getPath();
						
						if (new File(filePathSrc).exists()) {
							try{								
								buffer.insertFile(jEdit.getActiveView(), filePathSrc);
								/* original
								String content = TextfileUtilities.readText(filePathSrc);
								buffer.insert(0, content);
								*/
							}catch(Exception ex){}
						}
					}
				}
			}
		}
	}
	
	private void submitTask(){
		if (runner != null && runner.isAlive()) {
			View v = jEdit.getActiveView();
			GUIUtilities.message(v, "compiler.dialog0", null);
		} else {
		
			Runnable runnable = new Runnable() {
				public void run() {
					try {
						XTask task = env.getActiveTask();
						XProject project = env.getActiveProject();
						String language = project.getProperty("language");
								
						ProgramTester testRobot = ProgramTester.getInstance(project.getRootPath());
						testRobot.compiler(language, task.getProperty("ExName"));
						
						// decrypt file
						fileEncrypt = new Encryption();
						fileEncrypt.decrypt(project.getRootPath() + "\\" + task.getProperty("ExName") + ".cond2.enc");
						fileEncrypt.decrypt(project.getRootPath() + "\\" + task.getProperty("ExName") + ".exam.enc");
						
						ArrayList<String> correctAns = testRobot.readFile(task.getProperty("ExName") + ".cond2", "#####");
						ArrayList<String> _param = testRobot.readFile(task.getProperty("ExName") + ".exam", "#");
						
						String stuAns = new String();
						String corAns = new String();
						Boolean[] stuGrade = new Boolean[_param.size()];
						Map<Integer, Boolean> map = new HashMap<Integer, Boolean>();
						
						console.print("\n===============Begin Test===============\n");
						if(_param.size() == 0){
							stuAns = regStr(testRobot.executeSrc(language, "", task.getProperty("ExName")));
							corAns = regStr(correctAns.get(0));
							Boolean isPass;;
							if(stuAns.equals(corAns))
								isPass = true;
							else
								isPass = false;
							
							map.put(1, isPass);
							printTest(1, isPass, corAns, stuAns);
						} else
							for(int i = 0; i < _param.size(); i++){
								if(isInterrupt.equals(true))
									break;
								stuAns = regStr(testRobot.executeSrc(language, _param.get(i), task.getProperty("ExName")));
								corAns = regStr(correctAns.get(i));
								if(stuAns.equals(corAns))
									stuGrade[i] = true;
								else
									stuGrade[i] = false;
									
								map.put(i + 1, stuGrade[i]);
									
								printTest(i + 1, stuGrade[i], corAns, stuAns);
							}
							isInterrupt = false;
						_stuGrade.put(String.valueOf(comboTask.getSelectedIndex() + 1), new HashMap<Integer, Boolean>(map));
						
						// saveGrade to Server
						mm.saveGrade(_stuGrade.toString(), env.getClassId(), env.getCourseId(), env.getLessonId(), env.getUserId());
						
					} catch (Exception e) {
					
					}
				}
			};
			
			new Thread(runnable).start();
			//remove submit button
			tb1.remove(tb2ForSubmit);
			tb1.revalidate();
			tb1.repaint();
			
			// set isSubmit
			isSubmit[comboTask.getSelectedIndex()] = true;
		}
	}
	
	private void printTest(int number, Boolean isPass, String corAns, String stuAns){
		console.print("TEST " + number + "\n", Color.red);
		console.print("EXPECTED RESULT：\n", Color.blue);
		console.print(corAns);
		console.print("\nYOUR RESULT：\n", Color.blue);
		console.print(stuAns);
		if(isPass)
			console.print("\nDESCRIPTION： " + "match\n", Color.green);
		else
			console.print("\nDESCRIPTION： " + "Mismatch\n", Color.red);
					
		console.print("---------------------------\n");		
	}
	
	private String regStr(String str){
		return str.trim().replaceAll(Character.toString((char) 13), "");
	}

	private void openExplorer() {
		XProject project = env.getActiveProject();
		Runtime runtime = Runtime.getRuntime();
		try {
			String root = new File(project.getRootPath()).getPath();
			String[] shell = env.getShell(env.getExplorer(root));
			runtime.exec(shell).waitFor();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void saveProject() throws Exception {
		// file encryption (*.exam, *.cond2)
		XProject project = env.getActiveProject();
		File _directory = new File(project.getRootPath());
		File[] files = _directory.listFiles();
		File del;
		
		fileEncrypt = new Encryption();
		for(File file : files) {
			if(file.getName().endsWith(".exam") || file.getName().endsWith(".cond2")){
				fileEncrypt.encrypt(project.getRootPath() + "\\" + file.getName());
				del = new File(project.getRootPath() + "\\" + file.getName());
				del.delete();				
			}
		}
		
		//fileEncrypt = new Encryption();
		//fileEncrypt.decrypt(project.getRootPath() + "\\" + task.getProperty("ExName") + ".cond2.enc");
		
		
		new Thread(new ProjectUploader()).start();
		new Thread(new SaveGradeSet()).start();
		new Thread(new SaveMasterySet()).start();
	}

	/**
	 * Task Move Backward
	 */
	private void upTask() {
		XProject actProject = env.getActiveProject();
		//int idx = comboTask.getSelectedIndex();
		setIndex();
		if (idx > 0) {
			XTask task = actProject.getTask(idx);

			actProject.insertTask(idx - 1, task);
			actProject.removeTask(idx + 1);
		}
		refreshTaskComboBox();
		int newIdx = idx - 1;
		if (newIdx < 0) {
			newIdx = 0;
		}
		comboTask.setSelectedIndex(newIdx);
	}

	/**
	 * Task Move Forward
	 */
	private void downTask() {
		XProject actProject = env.getActiveProject();
		//int idx = comboTask.getSelectedIndex();
		setIndex();
		if (idx < comboTask.getItemCount()) {
			XTask task = actProject.getTask(idx + 1);

			actProject.insertTask(idx, task);
			actProject.removeTask(idx + 2);
		}
		refreshTaskComboBox();
		int newIdx = idx + 1;
		if (newIdx > comboTask.getItemCount()) {
			newIdx = comboTask.getItemCount();
		}
		comboTask.setSelectedIndex(newIdx);
	}

	/**
	 * Add New Task
	 */
	private void addTask() {
		XProject project = env.getActiveProject();

		XTask newTask = new XTask();

		int taskCount = getTaskCount();
		project.setProperty("task.identity", String.valueOf(taskCount + 1));
		newTask.setId(String.valueOf(taskCount));
		newTask.setTitle("New Task");

		//int idx = comboTask.getSelectedIndex();
		setIndex();
		if (project.getTasks().size() > 0 && idx >= 0) {
			XTask srcTask = env.getActiveProject().getTask(idx);
			if (srcTask != null) {
				newTask.setTitle(srcTask.getTitle());
				newTask.setCommands(srcTask.getCommands());

				Map<String, String> newProps = new HashMap<String, String>();
				newProps.putAll(srcTask.getProperties());
				newTask.setProperties(newProps);

				for (String key : newTask.getPropertyKeys("askfor.")) {
					String question = project.getTaskPropertyEx(newTask, key);

					String varName = key.substring(7); // length("askfor.")
					String varValue = JOptionPane.showInputDialog(question);

					newTask.setProperty(varName, varValue);
				}

			}
		}

		project.insertTask(idx + 1, newTask);
		newTask.setProperty("paramNum", "null");

		refreshTaskComboBox();
		int newIdx = idx + 1;
		if (newIdx > comboTask.getItemCount()) {
			newIdx = comboTask.getItemCount();
		}
		comboTask.setSelectedIndex(newIdx);
	}

	/**
	 * Remove Specified Task
	 */
	private void delTask() {
		//int idx = comboTask.getSelectedIndex();
		setIndex();
		if (idx >= 0) {
			env.getActiveProject().removeTask(idx);
		}
		refreshTaskComboBox();
		int newIdx = idx - 1;
		if (newIdx < 0)
			newIdx = 0;
		comboTask.setSelectedIndex(newIdx);
	}

	private int getTaskCount() {
		int result = 1;
		String taskCount = env.getActiveProject().getProperty("task.identity");
		if (taskCount != null) {
			try {
				result = Integer.valueOf(taskCount);
			} catch (NumberFormatException ex) {
			}
		}
		return result;
	}

	public void refreshTaskComboBox() {
		refreshTaskComboBox(false);
	}

	private void setIndex(){
		if(env.getLessonMode().equals("student") && (env.getActiveProject().getPropertyEx("hasMastery") != null && env.getActiveProject().getPropertyEx("hasMastery").equals("true"))){
			idx = masteryCore.getCurrentIdx();
		} else {
			idx = comboTask.getSelectedIndex();
		}
	}
	
	public void refreshTaskComboBox(boolean keepIdx) {
		//int idx = comboTask.getSelectedIndex();
		setIndex();
		comboTask.removeActionListener(this);
		comboTask.removeAllItems();
		int c = 1;
		
		XProject project = env.getActiveProject();
		
		if (project==null) {
			System.err.println("Project not loaded.");
			return;
		}
		
		for (XTask xtask : env.getActiveProject().getTasks()) {
			comboTask.addItem(String.valueOf(c).concat(" - ")
					.concat(xtask.getTitle()));
			c++;
		}
		comboTask.addActionListener(this);
		if (keepIdx) {
			comboTask.setSelectedIndex(idx);
		}
	}

	/**
	 * Reload Task
	 */
	public void reloadTask() {
		XProject project = env.getActiveProject();

		if (project == null || project.getTasks().size() == 0) {
			return;
		}

		// send 'end' message for before task
		XTask beforeTask = env.getActiveTask();

		// if (beforeTask != null) {
		// mm.saveMessage(
		// beforeTask.getId(),
		// (Long) beforeTask.getTempAttribute("time.begin"),
		// (Long) beforeTask.getTempAttribute("time.begin"),
		// 0,
		// "end",
		// "",
		// "End of exercise.",
		// getMainBufferText(),
		// "",
		// "",
		// 0,
		// ""
		// );
		// }

		View actView = jEdit.getActiveView();
		jEdit.closeAllBuffers(actView);

		//int idx = comboTask.getSelectedIndex();
		setIndex();
		XTask task = project.getTask(idx);

		if (task == null) {
			return;
		}

		env.setActiveTask(task);

		String rootPath = project.getRootPath();

		String fileMain = project.getTaskPropertyEx(task, "file.main");

		Buffer mainBuffer = null;

		if (fileMain != null) {
			String filePath = new File(rootPath, fileMain).getPath();
			mainBuffer = jEdit.openFile(actView, filePath);
		}

		if (mainBuffer == null) {
			return;
		}

		task.setTempAttribute("time.begin", new Date().getTime());

		// mm.saveMessage(
		// task.getId(),
		// (Long) task.getTempAttribute("time.begin"),
		// (Long) task.getTempAttribute("time.begin"),
		// 0,
		// "begin",
		// "",
		// "Begin of exercise.",
		// "",
		// "",
		// "",
		// 0,
		// ""
		// );

		openProjectHtml(task);

		for (String key : task.getProperties().keySet()) {
			if (env.getLessonMode().equals("author")
					&& key.startsWith("file.author")) {
				String file = new File(rootPath, project.getTaskPropertyEx(
						task, key)).getPath();
				jEdit.openFile(actView, file);
			} else if (key.startsWith("file.attach")) {
				String file = new File(rootPath, project.getTaskPropertyEx(
						task, key)).getPath();
				jEdit.openFile(actView, file);
			}
		}

		openFileViewer(task);

		jEdit.getActiveView().setBuffer(mainBuffer);

		mainBuffer.addBufferListener(bcl);

		console.switchTo(idx);

		// set Submit button.
		if(env.getIsExam().equals("true") && project.getPropertyEx("hasExamFile").equals("true")){
			tb1.remove(tb2ForSubmit);
			tb1.revalidate();
			tb1.repaint();
			if(isSubmit[idx].equals(false)){
				tb1.add(tb2ForSubmit);
			}
		}
		// mm.saveMessage(task.getId(), "start", "", "");
		
		if(env.getLessonMode().equals("student") && (env.getActiveProject().getPropertyEx("hasMastery") != null && env.getActiveProject().getPropertyEx("hasMastery").equals("true")))
			toolLabel.setText(task.getProperty("ExName"));
	}

	private void openFileViewer(XTask task) {
		XProject project = env.getActiveProject();

		if (project == null) {
			return;
		}

		String rootPath = project.getRootPath();

		env.getActiveViewer().clear();

		for (String key : task.getProperties().keySet()) {
			if (key.startsWith("file.view")) {
				String keyLabel = key.replace("file.view", "label.view");
				String title = project.getTaskPropertyEx(task, keyLabel);
				String path = new File(rootPath, project.getTaskPropertyEx(
						task, key)).getPath();
				String content = TextfileUtilities.readText(path);
				env.getActiveViewer().showText(title, content);
			}
		}
	}

	private void openProjectHtml(XTask task) {

		XProject project = env.getActiveProject();

		if (project == null) {
			return;
		}

		String rootPath = project.getRootPath();

		String fileHtml = project.getTaskPropertyEx(task, "file.html");

		if (fileHtml != null) {

			JWebBrowser browser = env.getActiveBrowser();
			String url = null;

			if(browser == null || browser.equals(null))
				console.print("fhdkfdhsjfkldjkl;fjdsalk;fjdlk;asf");
			
			
			
			if (isValidHttpURL(fileHtml)) {
				url = fileHtml.trim();
			} else {
				url = new File(rootPath, fileHtml).toURI().toString();
			}
			// browser
			// .setHTMLContent("<html><head><meta http-equiv=content-type content=\"text/html; charset=UTF-8\"></head><body></body></html>");

			if (url != null) {
				//console.print("\n" + url + "\n");
				browser.navigate(url);
				
			}
		}
	}

	/**
	 * URL Validation
	 * 
	 * @param url
	 * @return
	 */
	private boolean isValidHttpURL(String url) {
		return url.startsWith("http://") || url.startsWith("https://");
	}

	/**
	 * Read all buffer text from jEdit
	 * 
	 * @return
	 */
	protected String getMainBufferText() {
		String result = null;

		XProject project = env.getActiveProject();
		XTask task = env.getActiveTask();
		String rootPath = env.getActiveProject().getRootPath();
		View view = jEdit.getActiveView();

		if (task != null) {
			String fileMain = project.getTaskPropertyEx(task, "file.main");

			if (fileMain != null) {
				String pathMain = new File(rootPath, fileMain).getPath();
				Buffer buffer = jEdit.getBuffer(pathMain);

				if (buffer == null) {
					buffer = jEdit.openFile(view, pathMain);
				}
				result = buffer.getText(0, buffer.getLength());
			}
		}

		return result;
	}

	protected ImageIcon createImageIcon(String path) {
		URL imgURL = this.getClass().getResource(path);
		return new ImageIcon(imgURL);
	}

	protected JButton createButton(String text, String icon, String cmd) {
        return createButton(text, icon, cmd, true);
    }

	protected JButton createButton(String text, String icon, String cmd, boolean iconOnly) {
		JButton button = new JButton(createImageIcon(icon));
		button.setMargin(new Insets(5, 5, 5, 5));
		button.setBorder(BorderFactory.createEmptyBorder());
		button.setToolTipText(text);
        if (!iconOnly) {
            button.setText(text);
        }
		button.setFont(new Font("Serif", Font.PLAIN, 13));
		button.setActionCommand(cmd);
		button.addActionListener(this);
		return button;
	}

	protected JComboBox createComboBox(String[] data, String cmd) {
		JComboBox obj = new JComboBox(data);
		obj.setActionCommand(cmd);
		obj.addActionListener(this);
		return obj;
	}

	public void _reloadTask(){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				reloadTask();
			}
		});
	}
	
	/**
	 * Action: Run
	 */
	public void run() {
		if (runner != null && runner.isAlive()) {
			View v = jEdit.getActiveView();
			GUIUtilities.message(v, "compiler.dialog0", null);
		} else {
			View actView = jEdit.getActiveView();
			jEdit.saveAllBuffers(actView, false);

			String mode = (String) comboMode.getSelectedItem();

			runner = new CompilerRunner(console, mode, getMainBufferText());
			
			if(env.getIsExam().equals("true") && env.getActiveProject().getPropertyEx("hasExamFile").equals("true")){
				runner.setIsSubmit(isSubmit);
				runner.setCurrentTaskIdx(comboTask.getSelectedIndex());
			}
			
			runner.start();
			
			
			if((env.getActiveProject().getPropertyEx("hasMastery") != null && env.getActiveProject().getPropertyEx("hasMastery").equals("true")) && env.getLessonMode().equals("student")){
				
				
				Runnable _runnable = new Runnable(){
					//public Boolean isReloadTask = false;
					public void run(){
					/*
						while(runner.isAlive()){
							try {
								//console.print("\nrunner still alive........" + masteryCore.getCurrentIdx() + "   Idx: " + idx + "\n");
								Thread.sleep(500);
							} catch (InterruptedException ex) {
							}
						}*/
						try {
							runner.join();
						} catch(InterruptedException e){
						}
						//console.print("### " + masteryCore.getCurrentIdx() + "\n");
						if(masteryCore.getIsDialog()){
							int dialogResult = JOptionPane.showConfirmDialog(null, "更換較簡單的題目？", "Change Task", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							if(dialogResult == JOptionPane.YES_OPTION){
								masteryCore.setCurrentIndex();
							}
							masteryCore.setIsDialog();
						}
						//console.print("#@# " + masteryCore.getCurrentIdx() + "\n");
						if(masteryCore.getCurrentIdx() == -1) {
							//console.print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!#@# " + masteryCore.getCurrentIdx() + "\n");
								//env.getActiveProject().setProperty("hasMastery", "false");
								
								
								
						} else if(idx != masteryCore.getCurrentIdx()){
							//console.print("\n != \n");
							mm.saveStuMastery(env.getClassId(), env.getCourseId(), env.getLessonId(), env.getUserId(), masteryCore.getMasteryString());
							try {
								//CompilerUserInterface.this.reloadTask(); // refresh
								CompilerUserInterface.this._reloadTask();
							} catch(Exception e){
								console.print(e.toString());
							}
								
						}
						//console.print("#@# " + masteryCore.getCurrentIdx() + "\n");
							//mm.saveGrade(_stuGrade.toString(), env.getClassId(), env.getCourseId(), env.getLessonId(), env.getUserId());
					}
				};
				new Thread(_runnable).start();
			
			}
			
		}
	}

	/**
	 * Action Interrupt
	 */
	public void interrupt()throws IOException, InterruptedException {
		
		ProgramTester.getInstance(env.getActiveProject().getRootPath()).interrupt();
		isInterrupt = true;
	
		if (runner != null && runner.isAlive()) {
			runner.interrupt();
		} else {
			View v = jEdit.getActiveView();
			GUIUtilities.message(v, "compiler.dialog1", null);
		}
	}

	//private JHTMLEditor htmlEditor = null;
	private JFrame htmlFrame = null;
	private Buffer htmlBuffer = null;

	/**
	 * Action: HTML Edit
	 */
	public void html() {
        /*
		if (htmlFrame == null) {
			htmlEditor = new JHTMLEditor();
			htmlFrame = new JFrame();
			htmlFrame.setSize(640, 480);
			htmlFrame.add(htmlEditor);
			htmlEditor.addHTMLEditorListener(this);
		}

		Buffer buffer = jEdit.getActiveView().getBuffer();
		String bufferText = buffer.getText(0, buffer.getLength());

		htmlBuffer = buffer;
		htmlEditor.setHTMLContent(bufferText);

		htmlFrame.setTitle("HTML Editor - ".concat(buffer.getPath()));
		htmlFrame.setVisible(true);
        */
	}

	public void saveHTML(HTMLEditorSaveEvent arg0) {
		/*String htmlContent = htmlEditor.getHTMLContent();
		htmlContent = htmlContent.replace("\r\n", "\n");
		htmlBuffer.remove(0, htmlBuffer.getLength());
		htmlBuffer.insert(0, htmlContent);
		htmlFrame.setVisible(false);*/
	}
	
	class SaveGradeSet implements Runnable {
		@SuppressWarnings("unchecked")
		public void run() {
			XProject project = env.getActiveProject();
			if(project.getProperty("hasExamFile").equalsIgnoreCase("true")){
				List<XTask> tasks = project.getTasks();
				JSONObject obj = new JSONObject();
				Map<Integer, Integer> map = new HashMap<Integer, Integer>();
				
				for(int i = 0; i < tasks.size(); i++){
					if(tasks.get(i).getPropertyEx("paramNum") != null && !tasks.get(i).getPropertyEx("paramNum").equals("null")){
						for(int j = 0; j < Integer.parseInt(tasks.get(i).getProperty("paramNum")); j++){
							map.put(j + 1, 0);
						}
					} else {
						map.put(0, 0);
					}
					obj.put(i + 1, new HashMap<Integer, Integer>(map));
					map.clear();
				}
				
				Thread thread = mm.saveGradeSetting(obj.toString());
				while (thread.isAlive()) {
					console.print(".");
					try {
						Thread.sleep(500);
					} catch (InterruptedException ex) {

					}
				}
			}
				
		}
	}
	
	class SaveMasterySet implements Runnable {
		public void run() {
			XProject project = env.getActiveProject();
			if(project.getProperty("hasMastery").equalsIgnoreCase("true")) {
				Thread thread = mm.saveMasterySet(MasteryLearningSet.getInstance().getMasterySet());
				while (thread.isAlive()) {
					console.print(".");
					try {
						Thread.sleep(500);
					} catch (InterruptedException ex) {

					}
				}
			}
		}
	}
	

	class ProjectUploader implements Runnable {

		public void run() {
			console.print("Saving project ");
			XProject project = env.getActiveProject();
			project.readFromDisk();

			Thread thread = mm.saveProject(project);
			while (thread.isAlive()) {
				console.print(".");
				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {

				}
			}
			console.println("done");
		}

	}
}
