package org.plweb.jedit;

import java.util.Scanner;
import javax.swing.JOptionPane;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class MasteryCore {
	private static MasteryCore instance = null;
	private static MessageManager mm = MessageManager.getInstance();
	private static ProjectEnvironment env = ProjectEnvironment.getInstance();

	private JSONObject stuRecord = null;
	private JSONObject masteryTime = null;
	private int[][] questionSeq;
	private int currentIndex = 0;
	private Boolean noneMasteryTime = false;
	private int currentGroup = 0;
	private int tmpIndex = 0;
	private Boolean isDialog = false;

	public static MasteryCore getInstance() {
		if (instance == null) {
			String masteryString = mm.getStuMastery(env.getClassId(), env.getCourseId(), env.getLessonId(), env.getUserId());
			return instance = new MasteryCore(masteryString);
		} else
			return instance;
	}

	public MasteryCore(String masteryString) {

		JSONParser parser = new JSONParser();
		JSONObject tmp = new JSONObject();
		
		try {
			tmp = (JSONObject) parser.parse(masteryString);
			stuRecord = (JSONObject) tmp.get("stuRecord");

			if(!tmp.get("MasteryTime").equals("false"))
				masteryTime = (JSONObject) tmp.get("MasteryTime");
			else
				noneMasteryTime = true;
			
			questionSeq = new int[stuRecord.size()][];
			for(int i = 1; i <= ((JSONObject)tmp.get("seq")).size(); i++ ){
				String[] _tmp = ((JSONObject)tmp.get("seq")).get(String.valueOf(i)).toString().split(", ");
				questionSeq[i - 1] = new int[_tmp.length];
				for(int j = 0; j < _tmp.length; j++){
					questionSeq[i - 1][j] = Integer.valueOf(_tmp[j]);
				}
			}
			
			
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}
	
	public int getCurrentIdx(){
		currentIndex = 0;
		for(int i = 1; i <= stuRecord.size(); i++){
			if(((JSONObject) stuRecord.get(String.valueOf(i))).get("isPass").equals("false")) {
				if(((JSONObject) stuRecord.get(String.valueOf(i))).containsKey("currentIndex")){
					currentIndex = Integer.valueOf(((JSONObject) stuRecord.get(String.valueOf(i))).get("currentIndex").toString());
					currentGroup = i - 1;
					break;
				} else {
					// upload new masterySet & add currentIndex
					currentIndex = questionSeq[i - 1][0];
					((JSONObject) stuRecord.get(String.valueOf(i))).put("currentIndex", String.valueOf(currentIndex));
					currentGroup = i - 1;
					/////// upload ???????
					break;
				}
			}
		}
		return currentIndex - 1;
	}
	
	/* taskId, timeUsed, status */
	public void compare(int taskId, long timeUsed, String status) {
		// update stuRecord 
		// upload
		
		Boolean flag = false;
		/* get TaskAvgTime */
		int compareTime = 0;
		if(!noneMasteryTime){
			compareTime = Integer.valueOf(masteryTime.get(String.valueOf(taskId)).toString());
		}
		
		
		if((int)(timeUsed/1000) < compareTime){
			if(status.equalsIgnoreCase("test_ok")){
				
				for(int i = 0; i < questionSeq[currentGroup].length; i++){
					if(currentIndex == questionSeq[currentGroup][i]){
						if(i > 0){
							for(int j = i - 1; j >=0; j--){
								if( ((JSONObject) stuRecord.get(String.valueOf(currentGroup + 1))).get(String.valueOf(questionSeq[currentGroup][j])).equals("false") ){
									changeNextTask(questionSeq[currentGroup][j]);
									flag = true;
									break;
								}
							}
						}
						if(i == 0 || !flag) {
							changeNextGroup();
							break;
						}
					}
				}
			
			}
		} else if((int)(timeUsed/1000) > compareTime){
			if(status.equalsIgnoreCase("test_ok")){
				((JSONObject)stuRecord.get(String.valueOf(currentGroup + 1))).put(String.valueOf(currentIndex), "true");
				Boolean changeGroup = true;
				Boolean allTrue = true;
				int tmp = 0;
				if(currentIndex != questionSeq[currentGroup].length - 1){
					for(int i = 0; i < questionSeq[currentGroup].length; i++){
						if(currentIndex == questionSeq[currentGroup][i]){
							tmp = i;
							for(int j = i; j < questionSeq[currentGroup].length; j++){
								if( ((JSONObject) stuRecord.get(String.valueOf(currentGroup + 1))).get(String.valueOf(questionSeq[currentGroup][j])).equals("false") ){
									((JSONObject)stuRecord.get(String.valueOf(currentGroup + 1))).put("currentIndex", String.valueOf(questionSeq[currentGroup][j]));
									allTrue = false;
									changeGroup = false;
									break;
								}
							}
							break;
						}
					}
				}
				if(allTrue){
					for(int j = tmp; j >=0; j--){
						if( ((JSONObject) stuRecord.get(String.valueOf(currentGroup + 1))).get(String.valueOf(questionSeq[currentGroup][j])).equals("false") ){
							((JSONObject)stuRecord.get(String.valueOf(currentGroup + 1))).put("currentIndex", String.valueOf(questionSeq[currentGroup][j]));
							changeGroup = false;
							break;
						}
					}
				}
				if(changeGroup)
					changeNextGroup();
				
				
		
			} else {
				// confirm ui to change task
				//int dialogResult = 0;
				for(int i = 0; i < questionSeq[currentGroup].length; i++){
					if(questionSeq[currentGroup][i] == currentIndex && i != questionSeq[currentGroup].length - 1){
						for(int j = i + 1; j < questionSeq[currentGroup].length; j++){
							if( ((JSONObject) stuRecord.get(String.valueOf(currentGroup + 1))).get(String.valueOf(questionSeq[currentGroup][j])).equals("false")){
								isDialog = true;
								tmpIndex = questionSeq[currentGroup][j];
								break;
							}
						}
						break;
					}
				}
				
				
			}
		}
		

	}
	
	public void setIsDialog(){
		isDialog = false;
	}
	
	public Boolean getIsDialog(){
		return isDialog;
	}
	
	public void setCurrentIndex(){
		((JSONObject)stuRecord.get(String.valueOf(currentGroup + 1))).put("currentIndex", String.valueOf(tmpIndex));
	}
	
	private void changeNextGroup(){
		((JSONObject)stuRecord.get(String.valueOf(currentGroup + 1))).put(String.valueOf(currentIndex), "true");
		((JSONObject)stuRecord.get(String.valueOf(currentGroup + 1))).put("isPass", "true");
		currentGroup++;
		// upload masterySet
	}
	
	private void changeNextTask(int index){
		((JSONObject)stuRecord.get(String.valueOf(currentGroup + 1))).put(String.valueOf(currentIndex), "true");
		((JSONObject)stuRecord.get(String.valueOf(currentGroup + 1))).put("currentIndex", String.valueOf(index));
		// upload masterySet
	}
}
