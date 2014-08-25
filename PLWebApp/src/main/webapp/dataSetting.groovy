import javax.naming.InitialContext
import groovy.sql.Sql
import org.plweb.webapp.helper.CommonHelper
import java.util.HashMap
import java.util.Map
import org.json.simple.JSONValue
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException

helper = new CommonHelper(request, response, session)

action = helper.fetch('action')
classId = helper.fetch('classId')
courseId = helper.fetch('courseId')
lessonId = helper.fetch('lessonId')
userId = helper.fetch('userId')

sql = new Sql(helper.connection)

switch(action){
	case 'init':
		
		checkSql = """ SELECT GRADE FROM ST_GRADE WHERE CLASS_ID=? AND COURSE_ID=? AND LESSON_ID=? AND USER_ID=? """
		try {
			row = sql.firstRow(checkSql, [classId, courseId, lessonId, userId])
			if(!row.equals(null)){
				print row.grade
			} else {
				selectSql = """ SELECT GRADE_SET FROM GRADE_SETTING WHERE COURSE_ID=? AND LESSON_ID=? """
				gradeSet = sql.firstRow(selectSql, [courseId, lessonId]).grade_set
				
				JSONParser parser = new JSONParser()
				JSONObject _gradeSet = (JSONObject) parser.parse(gradeSet)
				
				JSONObject _return = new JSONObject();
				for(i = 0; i < _gradeSet.size(); i++){
					_return.put(i + 1, false)
				}
				
				insertSql = """ INSERT INTO ST_GRADE VALUES(?, ?, ?, ?, ?) """
				sql.executeInsert(insertSql, [classId, courseId, lessonId, userId, _return.toString()])
				
				print _return.toString()
			}
		} catch(e){

		} catch(ParseException e) {
			e.printStackTrace()
		}
		break;
	case 'saveGrade':
		updateSql = """ UPDATE ST_GRADE SET GRADE=? WHERE CLASS_ID=? AND COURSE_ID=? AND LESSON_ID=? AND USER_ID=? """
		grade = request.getParameter('grade')
		sql.executeUpdate(updateSql, [grade, classId, courseId, lessonId, userId])	
		break;
	case 'setGrade':
		utype = helper.sess('utype')
		if(!session || utype != 'T'){
			response.sendRedirect('/permission_denied.groovy')
			return;
		}
		gradeSet = request.getParameter('gradeSet')
		totalSet = request.getParameter('totalSet')
		updateSql = """ UPDATE GRADE_SETTING SET GRADE_SET=?, TOTAL_SET=? WHERE COURSE_ID=? AND LESSON_ID=? """
		sql.executeUpdate(updateSql, [gradeSet, totalSet, courseId, lessonId])
		break;
	case 'getMasterySet':
		checkSql = """ SELECT MASTERY_SETTING FROM MASTERY_SETTING WHERE COURSE_ID=? AND LESSON_ID=? """
		try{
			row = sql.firstRow(checkSql, [courseId, lessonId])
			print row.MASTERY_SETTING			
		} catch(e) {
		}
		
		
		break;
		
	case 'getStuMastery':
	
		allAvgTime = """ SELECT QUESTION_ID, AVG(TIME_USED) as AVG FROM ST_REPORTS WHERE COURSE_ID=? AND LESSON_ID=? GROUP BY QUESTION_ID """
        /* get last year */
        lastAvgTime = """ SELECT QUESTION_ID, AVG(TIME_USED) as AVG FROM ST_REPORTS, CLASS_COURSE WHERE ST_REPORTS.CLASS_ID=CLASS_COURSE.CLASS_ID AND ST_REPORTS.COURSE_ID=? AND ST_REPORTS.LESSON_ID=? AND CLASS_COURSE.BEGINDATE>? GROUP BY QUESTION_ID """

        try {
			Date tmp = new Date("1/1/" + String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 1))
            Calendar cal = Calendar.getInstance()
            cal.setTime(tmp);
            long millis = cal.getTimeInMillis()


            JSONObject _return = new JSONObject()
            JSONObject masteryTime = new JSONObject()
            JSONObject stuRecord = new JSONObject()

            lastAvgTimeRows = sql.rows(lastAvgTime, [courseId, lessonId, millis])
            allAvgTimeRows = sql.rows(allAvgTime, [courseId, lessonId])



            int x = 2
            if((lastAvgTimeRows.size() == 0 && allAvgTimeRows.size() > 0) || (lastAvgTimeRows.size() > 0 && allAvgTimeRows.size() == 0))
                x = 1
            else if(lastAvgTimeRows.size() == 0 && allAvgTimeRows.size() == 0)
                x = 0

            for(i = 0; i < allAvgTimeRows.size(); i++){

                if(lastAvgTimeRows.AVG[i] == null)
                    lastAvg = 0
                else
                    lastAvg = (int)lastAvgTimeRows.AVG[i]
                if(allAvgTimeRows.AVG[i] == null)
                    allAvg = 0
                else
                    allAvg = (int)allAvgTimeRows.AVG[i]

                masteryTime.put(allAvgTimeRows.QUESTION_ID[i], (int)((lastAvg + allAvg)/x/1000));
            }

            _return.put("MasteryTime", masteryTime)

            checkSql = """ SELECT MASTERY_GRADE FROM ST_MASTERY WHERE CLASS_ID=? AND COURSE_ID=? AND LESSON_ID=? AND USER_ID=? """
            row = sql.firstRow(checkSql, [classId, courseId, lessonId, userId])
            if(!row.equals(null)){
                _return.put("stuRecord", row.MASTERY_GRADE)
            } else {
                getMastery = """ SELECT MASTERY_SETTING FROM MASTERY_SETTING WHERE COURSE_ID=? AND LESSON_ID=? """
                row = sql.firstRow(getMastery, [courseId, lessonId])

                JSONParser parser = new JSONParser()
                JSONObject _masterySet = (JSONObject) parser.parse(row.MASTERY_SETTING)


                for(i = 1; i <= _masterySet.size(); i++){
                    tmpString = _masterySet.get(String.valueOf(i))
                    String[] tmpArray = tmpString.split(", ")
                    JSONObject content = new JSONObject()
                    for(j = 0; j < tmpArray.size(); j++){
                        content.put(tmpArray[j], false)
                    }
                    content.put("isPass", false)
                    stuRecord.put(i, new JSONObject(content))
                }
                _return.put("stuRecord", stuRecord)
                insertMasterySet = """ INSERT INTO ST_MASTERY(CLASS_ID, COURSE_ID, LESSON_ID, USER_ID, MASTERY_GRADE) VALUES(?, ?, ?, ?, ?) """
                sql.executeInsert(insertMasterySet, [classId, courseId, lessonId, userId, stuRecord.toString()])
            }

            print _return.toString()
            } catch(e){
			} catch(ParseException e){
            }
		break;
		
		
}
sql.close()