import javax.naming.InitialContext
import groovy.sql.Sql
import org.plweb.webapp.helper.CommonHelper

helper = new CommonHelper(request, response, session)

action = helper.fetch('action')
classId = helper.fetch('classId')
courseId = helper.fetch('courseId')
lessonId = helper.fetch('lessonId')
userId = helper.fetch('userId')

sql = new Sql(helper.connection)

switch(action){
	case 'init':
	
		checkSql = """ SELECT GRADE as cc FROM ST_GRADE WHERE CLASS_ID=? AND COURSE_ID=? AND LESSON_ID=? AND USER_ID=? """
		try {
			cc = sql.firstRow(checkSql, [classId, courseId, lessonId, userId]).cc
			if(cc != null){
				print cc
			} else {
				selectSql = """ SELECT GRADE_SET as set FROM GRADE_SETTING WHERE COURSE_ID=? AND LESSON_ID=? """
				gradeSet = sql.firstRow(selectSql, [courseId, lessonId]).set
				
				JSONParser parse = new JSONParser()
				JSONObject gradeSet = (JSONObject) parser.parse(gradeSet)
				
			}
		} catch(e){

		} catch(ParseException e) {
			e.printStackTrace()
		}
		break;
	
}