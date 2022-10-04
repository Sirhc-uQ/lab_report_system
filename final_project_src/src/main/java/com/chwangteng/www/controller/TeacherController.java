package com.chwangteng.www.controller;


import com.chwangteng.www.Utils.ConstVar;
import com.chwangteng.www.mapper.LaboratoryMapper;
import com.chwangteng.www.mapper.StudentMapper;
import com.chwangteng.www.mapper.TeacherMapper;
import com.chwangteng.www.param.*;
import com.chwangteng.www.pojo.*;
import com.chwangteng.www.service.TeacherService;
import com.chwangteng.www.Utils.DeadlineConvertor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private LaboratoryMapper laboratoryMapper;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentMapper studentMapper;

    //新增老师
    @Transactional
    @RequestMapping("/addTeacher.action")
    public ModelAndView addTeacher(@RequestBody AddTeacherRequestParam addTeacherRequestParam)   {
        Teacher teacher = new Teacher();
        teacher.setIsSupervisor(addTeacherRequestParam.getIs_supervisor());
        teacher.setAbout(addTeacherRequestParam.getAbout());
        teacher.setSex(addTeacherRequestParam.getSex());
        teacher.setTelephone(addTeacherRequestParam.getTelephone());
        teacher.setMail(addTeacherRequestParam.getMail());
        teacher.setName(addTeacherRequestParam.getName());
        teacher.setLabId(addTeacherRequestParam.getLab_id());
        //teacher.setDeadline(addTeacherRequestParam.getDeadline());
        teacher.setUsername(addTeacherRequestParam.getUsername());
        //teacher.setPassword(addTeacherRequestParam.getPassword());
        int rows = teacherMapper.insertSelective(teacher);
        if(rows==1) {
            int resetrows = teacherService.resetPassword(teacher.getUsername());

            if(resetrows!=1){
                Map<String,Object> map = new HashMap<String,Object>();
                map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_COMMON_);
                map.put(ConstVar._KEY_MESSAGE_, "为老师初始化密码时发生错误");
                return new ModelAndView(new MappingJackson2JsonView(),map);
            }

            //如果是主管老师,去更新同一个实验室以确保其他老师都不再是主管老师
            if(teacher.getIsSupervisor()==ConstVar._SUPER_YES_){

                TeacherExample teacherExample = new TeacherExample();
                teacherExample.createCriteria().andLabIdEqualTo(addTeacherRequestParam.getLab_id());

                List<Teacher> labteachers = teacherMapper.selectByExample(teacherExample);

                int meet = 0;
                int modify = 0;

                for (int index = 0;index<labteachers.size();index++){
                    Teacher currentteacher = labteachers.get(index);
                    if(currentteacher.getIsSupervisor()==1&&currentteacher.getId()!=teacher.getId()){
                        meet++;
                        currentteacher.setIsSupervisor(-1);
                        int modirows = teacherMapper.updateByPrimaryKeySelective(currentteacher);
                        if(modirows==1)
                            modify++;
                    }
                }

                if(meet==modify){
                    Map<String,Object> map = new HashMap<String,Object>();
                    map.put(ConstVar._KEY_MESSAGE_, "新增老师成功，并成为了责任老师");
                    return new ModelAndView(new MappingJackson2JsonView(),map);
                }else {
                    Map<String,Object> map = new HashMap<String,Object>();
                    map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_COMMON_);
                    map.put(ConstVar._KEY_MESSAGE_, "设置为责任老师时发生错误");
                    return new ModelAndView(new MappingJackson2JsonView(),map);
                }


/*                if(lab!=null){
                    lab.setTeacherId(teacher.getId());
                    int labrows = laboratoryMapper.updateByPrimaryKeySelective(lab);
                    if(labrows==1){
                        Map<String,Object> map = new HashMap<String,Object>();
                        map.put(ConstVar._KEY_MESSAGE_, "新增老师成功");
                        return new ModelAndView(new MappingJackson2JsonView(),map);
                    }else{
                        Map<String,Object> map = new HashMap<String,Object>();
                        map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_COMMON_);
                        map.put(ConstVar._KEY_MESSAGE_, "更新该老师主管的实验室失败");
                        return new ModelAndView(new MappingJackson2JsonView(),map);
                    }
                }else{
                    Map<String,Object> map = new HashMap<String,Object>();
                    map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_NOTFOUND);
                    map.put(ConstVar._KEY_MESSAGE_, "没有找到该老师主管的实验室");
                    return new ModelAndView(new MappingJackson2JsonView(),map);
                }*/

            }else{//不需要更新其他老师，直接成功
                Map<String,Object> map = new HashMap<String,Object>();
                map.put(ConstVar._KEY_MESSAGE_, "新增老师成功");
                return new ModelAndView(new MappingJackson2JsonView(),map);
            }
        }else{
            Map<String,Object> map = new HashMap<String,Object>();
            map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_COMMON_);
            map.put(ConstVar._KEY_MESSAGE_, "未知错误，新增老师失败");
            return new ModelAndView(new MappingJackson2JsonView(),map);
        }
        //如果老师的username重复，会直接抛出异常，已经用切片处理了该异常
    }

    //删除老师
    @RequestMapping("/deleteTeacher.action")
    public ModelAndView deleteTeacher(@RequestBody DeleteTeacherRequestParam deleteTeacherRequestParam)   {

        int rows = teacherMapper.deleteByPrimaryKey(deleteTeacherRequestParam.getId());
        Map<String,Object> map = new HashMap<String,Object>();
        if(rows==1){
            map.put(ConstVar._KEY_MESSAGE_, "删除老师成功");
        }else if(rows==0) {
            map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_NOTFOUND);
            map.put(ConstVar._KEY_MESSAGE_, "不存在该老师");
        }else {
            map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_COMMON_);
            map.put(ConstVar._KEY_MESSAGE_, "未知错误，删除老师失败");
        }
        return new ModelAndView(new MappingJackson2JsonView(),map);
    }

    //更新老师信息//还需要更新实验室表中的内容！！！！！！！！！！
    @RequestMapping("/updateTeacher.action")
    public ModelAndView updateTeacher(@RequestBody UpdateTeacherRequestParam updateTeacherRequestParam){

        Teacher teacher = new Teacher();
        teacher.setId(updateTeacherRequestParam.getId());
        teacher.setIsSupervisor(updateTeacherRequestParam.getIs_supervisor());
        teacher.setAbout(updateTeacherRequestParam.getAbout());
        teacher.setSex(updateTeacherRequestParam.getSex());
        teacher.setTelephone(updateTeacherRequestParam.getTelephone());
        teacher.setMail(updateTeacherRequestParam.getMail());
        teacher.setName(updateTeacherRequestParam.getName());
        //teacher.setLabId(updateTeacherRequestParam.getLab_id());不需要
        //teacher.setDeadline(updateTeacherRequestParam.getDeadline());
        teacher.setUsername(updateTeacherRequestParam.getUsername());
        //teacher.setPassword(updateTeacherRequestParam.getPassword());
        int rows = teacherMapper.updateByPrimaryKeySelective(teacher);

        if(rows==1){

            int resetrows = teacherService.resetPassword(teacher.getUsername());

            if(resetrows!=1){
                Map<String,Object> map = new HashMap<String,Object>();
                map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_COMMON_);
                map.put(ConstVar._KEY_MESSAGE_, "为老师更新密码时发生错误");
                return new ModelAndView(new MappingJackson2JsonView(),map);
            }

            //如果更新后是主管老师,去更新同一个实验室以确保其他老师都不再是主管老师
            if(teacher.getIsSupervisor()==ConstVar._SUPER_YES_){

                Teacher thisteacher = teacherMapper.selectByPrimaryKey(updateTeacherRequestParam.getId());

                TeacherExample teacherExample = new TeacherExample();
                teacherExample.createCriteria().andLabIdEqualTo(thisteacher.getLabId());

                List<Teacher> labteachers = teacherMapper.selectByExample(teacherExample);

                int meet = 0;
                int modify = 0;

                for (int index = 0;index<labteachers.size();index++){
                    Teacher currentteacher = labteachers.get(index);
                    if(currentteacher.getIsSupervisor()==1&&currentteacher.getId()!=teacher.getId()){
                        meet++;
                        currentteacher.setIsSupervisor(-1);
                        int modirows = teacherMapper.updateByPrimaryKeySelective(currentteacher);
                        if(modirows==1)
                            modify++;
                    }
                }

                if(meet==modify){
                    Map<String,Object> map = new HashMap<String,Object>();
                    map.put(ConstVar._KEY_MESSAGE_, "更新老师成功，并成为了责任老师");
                    return new ModelAndView(new MappingJackson2JsonView(),map);
                }else {
                    Map<String,Object> map = new HashMap<String,Object>();
                    map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_COMMON_);
                    map.put(ConstVar._KEY_MESSAGE_, "更新为责任老师时发生错误");
                    return new ModelAndView(new MappingJackson2JsonView(),map);
                }

            }else{//不需要更新其他老师，直接成功
                Map<String,Object> map = new HashMap<String,Object>();
                map.put(ConstVar._KEY_MESSAGE_, "更新老师成功");
                return new ModelAndView(new MappingJackson2JsonView(),map);
            }

        }else {
            Map<String,Object> map = new HashMap<String,Object>();
            map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_COMMON_);
            map.put(ConstVar._KEY_MESSAGE_, "未知错误，更新老师失败");
            return new ModelAndView(new MappingJackson2JsonView(),map);
        }

    }


    //老师修改自己的密码
    @RequestMapping("/updateMyPassword.action")
    public ModelAndView updateMyPassword(@RequestBody TeacherUpdateMyPasswordParam teacherUpdateMyPasswordParam, HttpSession session)   {

        int userid = Integer.parseInt(session.getAttribute(ConstVar._SESSION_USER_ID_).toString());
        int usertype = Integer.parseInt(session.getAttribute(ConstVar._SESSION_USER_TYPE_).toString());
        if(usertype==ConstVar._TEACHER_){
            int rows = teacherService.updateMyPassword(userid,
                    teacherUpdateMyPasswordParam.getPassword());
            if(rows==1){
                Map<String,Object> map = new HashMap<String,Object>();
                map.put(ConstVar._KEY_MESSAGE_, "密码已修改");
                return new ModelAndView(new MappingJackson2JsonView(),map);
            }else if(rows==ConstVar._ERROR_NOTFOUND){
                Map<String,Object> map = new HashMap<String,Object>();
                map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_NOTFOUND);
                map.put(ConstVar._KEY_MESSAGE_, "不存在该老师");
                return new ModelAndView(new MappingJackson2JsonView(),map);
            }else{
                Map<String,Object> map = new HashMap<String,Object>();
                map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_COMMON_);
                map.put(ConstVar._KEY_MESSAGE_, "未知错误，修改失败");
                return new ModelAndView(new MappingJackson2JsonView(),map);
            }
        }
        else{
            Map<String,Object> map = new HashMap<String,Object>();
            map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_COMMON_);
            map.put(ConstVar._KEY_MESSAGE_, "不是老师");
            return new ModelAndView(new MappingJackson2JsonView(),map);
        }
    }

    //老师设置周报截止日期
    @RequestMapping("/updateDeadline.action")
    public ModelAndView updateDeadline(@RequestBody UpdateDeadlineParam updateDeadlineParam, HttpSession session)   {
        int userid = Integer.parseInt(session.getAttribute(ConstVar._SESSION_USER_ID_).toString());
        int usertype = Integer.parseInt(session.getAttribute(ConstVar._SESSION_USER_TYPE_).toString());
        if(usertype==ConstVar._TEACHER_){
            Teacher teacher = teacherMapper.selectByPrimaryKey(userid);
            String day = updateDeadlineParam.getDay();//周一、周二等，转成Sun等
            Date time = updateDeadlineParam.getDate();//需要转成四位数,如2200
            //使用转换器对日期进行转换
            teacher.setDeadline(DeadlineConvertor.chinese2english(day)+DeadlineConvertor.Time2String(time));
            int rows = teacherMapper.updateByPrimaryKeySelective(teacher);
            if(rows==1){
                Map<String,Object> map = new HashMap<String,Object>();
                map.put(ConstVar._KEY_MESSAGE_, "截止日期已修改");
                return new ModelAndView(new MappingJackson2JsonView(),map);
            }else{
                Map<String,Object> map = new HashMap<String,Object>();
                map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_COMMON_);
                map.put(ConstVar._KEY_MESSAGE_, "未知错误，修改失败");
                return new ModelAndView(new MappingJackson2JsonView(),map);
            }
        }else{
            Map<String,Object> map = new HashMap<String,Object>();
            map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_COMMON_);
            map.put(ConstVar._KEY_MESSAGE_, "不是老师");
            return new ModelAndView(new MappingJackson2JsonView(),map);
        }
    }

    //查找老师信息
    @RequestMapping("/selectTeacher.action")
    public ModelAndView selectTeacher(@RequestBody  SelectTeacherRequestParam selectTeacherRequestParam){

        TeacherExample teacherExample = new TeacherExample();
        teacherExample.createCriteria();

        //需要返回resultList和itemsCount
        List records = teacherMapper.selectByExample(teacherExample);

        if(records!=null){
            ModelAndView mv = new ModelAndView();
            mv.addObject(ConstVar._KEY_DATA_,records);
            mv.setView(new MappingJackson2JsonView());
            return mv;
        }else{
            Map<String,Object> map = new HashMap<String,Object>();
            map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_NOTFOUND);
            map.put(ConstVar._KEY_MESSAGE_, "发生错误");
            return new ModelAndView(new MappingJackson2JsonView(),map);
        }
    }

    //查看自己的学生
    @RequestMapping("/selectMyStudents.action")
    public ModelAndView selectMyStudents(HttpSession session){
        int userid = Integer.parseInt(session.getAttribute(ConstVar._SESSION_USER_ID_).toString());
        int usertype = Integer.parseInt(session.getAttribute(ConstVar._SESSION_USER_TYPE_).toString());
        if(usertype==ConstVar._TEACHER_){

            StudentExample studentExample=new StudentExample();
            studentExample.createCriteria().andTeacherIdEqualTo(userid);

            List<Student> students = studentMapper.selectByExample(studentExample);
            ModelAndView mv = new ModelAndView();
            mv.addObject(ConstVar._KEY_DATA_,students);
            mv.setView(new MappingJackson2JsonView());
            return mv;
        }
        else{
            Map<String,Object> map = new HashMap<String,Object>();
            map.put(ConstVar._KEY_CODE_, ConstVar._ERROR_COMMON_);
            map.put(ConstVar._KEY_MESSAGE_, "不是老师");
            return new ModelAndView(new MappingJackson2JsonView(),map);
        }
    }
}
