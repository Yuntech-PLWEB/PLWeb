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
}
sql.close()