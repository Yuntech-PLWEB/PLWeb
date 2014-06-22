package org.plweb.jedit;

import java.io.*;
import java.util.ArrayList;

public class ProgramTester {
	
	private String rootPath;
	private String _javac = "javac";
	private String _java = "java";
	private String _javaExtension = ".java";
	private String cmd;
	private String extension;
	
	private static ProgramTester instance = null;
	
	public static ProgramTester getInstance(String rootPath){
		if(instance == null)
			return instance = new ProgramTester(rootPath);
		else
			return instance;
	}
	
	public ProgramTester(String rootPath){
		this.rootPath = rootPath;
	}
	
	// output the file.
	public void printer(String fileName, String content) throws IOException {
		PrintWriter printer = new PrintWriter(rootPath + "\\" + fileName, "UTF-8");
		printer.println(content);
		printer.close();
	}
	
	// compiler the source code
	public void compiler(String programLanguage, String srcName) throws IOException, InterruptedException {
		setCompilerCmd(programLanguage);
		
		Process compiler = Runtime.getRuntime().exec(cmd + " " + rootPath + "\\" + srcName + extension);
		compiler.waitFor(); // wait for compiler.
	}
	
	private void setCompilerCmd(String programLanguage){
		if(programLanguage.equalsIgnoreCase("java")){
			this.cmd = _javac;
			this.extension = _javaExtension;
		}
	}
	
	private void setRunCmd(String programLanguage){
		if(programLanguage.equalsIgnoreCase("java")){
			this.cmd = _java;
			this.extension = "";
		}
	}
	
	// run the binary code (with param) 
	public String executeSrc(String programLanguage, String param, String srcName) throws IOException, InterruptedException {
			setRunCmd(programLanguage);
			
			File directPath = new File(this.rootPath);			
			Process p = Runtime.getRuntime().exec(cmd + " " + srcName + extension, null, directPath);
			InputStream inputStream = p.getInputStream();
			InputStreamReader isr = new InputStreamReader(inputStream);
				
			// give input(s) to process p.
			OutputStream out = p.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
			writer.write(param);
			writer.flush();
				
			// print the output from process p.
			int n1;
			char[] c1 = new char[1024];
			StringBuffer standardOutput = new StringBuffer();
			while ((n1 = isr.read(c1)) > 0) {
				standardOutput.append(c1, 0, n1);
			}
					
			return standardOutput.toString();
	}
	
	// read exam file for get parameter
	public ArrayList<String> readFile(String src) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(this.rootPath + "\\" + src));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			ArrayList<String> parameter = new ArrayList<String>();
			
			while(line != null){
				if(line.equals("#") || line == null){
					parameter.add(sb.toString());
					sb.setLength(0);
					line = br.readLine();
					continue;
				}
				sb.append(line);
				sb.append("\n ");
				line = br.readLine();
			}
			return parameter;
		} finally {
			br.close();
		}
	}

}