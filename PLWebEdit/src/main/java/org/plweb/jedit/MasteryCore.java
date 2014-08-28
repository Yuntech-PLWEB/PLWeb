import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MasteryCore {
	private static MasteryCore instance = null;
	private MessageManager mm = MessageManager.getInstance();
	private ProjectEnvironment env = ProjectEnvironment.getInstance();

	private JSONObject stuRecord = null;
	private JSONObject masteryTime = null;
	private int[][] questionSeq;
	private int currentIndex = 0;
	private Boolean noneMasteryTime = false;
	private int currentGroup = 0;

	public static MasteryCore getInstance(String masteryString) {
		if (instance == null)
			return instance = new MasteryCore(masteryString);
		else
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
			
//			System.out.println(stuRecord.toString());
//			System.out.println(masteryTime.toString());
			
			
			questionSeq = new int[stuRecord.size()][];
			for(int i = 1; i <= ((JSONObject)tmp.get("seq")).size(); i++ ){
				String[] _tmp = ((JSONObject)tmp.get("seq")).get(String.valueOf(i)).toString().split(", ");
				questionSeq[i - 1] = new int[_tmp.length];
				for(int j = 0; j < _tmp.length; j++){
					questionSeq[i - 1][j] = Integer.valueOf(_tmp[j]);
				}
			}
			
			
			for(int i = 0; i < questionSeq.length; i++){
				for(int j = 0; j < questionSeq[i].length; j++)
					System.out.println(questionSeq[i][j]);
				System.out.println();
			}
			
			System.out.println(" #### " + stuRecord.toString());
			System.out.println("currentIndex " + getCurrentIdx());
			System.out.println("-----------------------------------------------------");
			compare(3, 10000000, "test_ok");
			System.out.println(stuRecord.toString());
			System.out.println("currentIndex " + getCurrentIdx());
			System.out.println("111111111currentGroup" + currentGroup);
			//System.out.println(stuRecord.toString());
			System.out.println("-----------------------------------------------------");
			compare(2, 3000, "test_ok");
			System.out.println(stuRecord.toString());
			System.out.println("currentIndex " + getCurrentIdx());
			System.out.println("2222222222currentGroup" + currentGroup);
			//System.out.println(stuRecord.toString());
			System.out.println("-----------------------------------------------------");
			compare(2, 3000, "test_ok");
			System.out.println(stuRecord.toString());
			System.out.println("currentIndex " + getCurrentIdx());
			System.out.println("2222222222currentGroup" + currentGroup);
			System.out.println("-----------------------------------------------------");
			compare(2, 3000, "test_ok");
			System.out.println(stuRecord.toString());
			System.out.println("currentIndex " + getCurrentIdx());
			System.out.println("2222222222currentGroup" + currentGroup);
			System.out.println(stuRecord.toString());
			//System.out.println(stuRecord.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
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
		return currentIndex;
	}
	
	/* taskId, timeUsed, status */
	public Boolean compare(int taskId, long timeUsed, String status) {
		// update stuRecord 
		// upload
		
		Boolean flag = false;
		/* get TaskAvgTime */
		int compareTime = 0;
		if(!noneMasteryTime){
			compareTime = Integer.valueOf(masteryTime.get(String.valueOf(taskId)).toString());
		}
		System.out.println(compareTime);
		
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
				// 
			} else {
				// confirm ui to change task
			}
		}
		return false;
	}
	
	private void changeNextGroup(){
		((JSONObject)stuRecord.get(String.valueOf(currentGroup + 1))).put(String.valueOf(currentIndex), "true");
		((JSONObject)stuRecord.get(String.valueOf(currentGroup + 1))).put("isPass", "true");
		// upload masterySet
	}
	
	private void changeNextTask(int index){
		((JSONObject)stuRecord.get(String.valueOf(currentGroup + 1))).put(String.valueOf(currentIndex), "true");
		((JSONObject)stuRecord.get(String.valueOf(currentGroup + 1))).put("currentIndex", String.valueOf(index));
		// upload masterySet
	}
}

