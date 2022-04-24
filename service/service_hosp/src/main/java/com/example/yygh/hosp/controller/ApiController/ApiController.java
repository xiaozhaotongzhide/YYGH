package com.example.yygh.hosp.controller.ApiController;


import com.example.yygh.common.exception.YyghException;
import com.example.yygh.common.helper.HttpRequestHelper;
import com.example.yygh.common.result.MD5;
import com.example.yygh.common.result.Result;
import com.example.yygh.common.result.ResultCodeEnum;
import com.example.yygh.hosp.service.DepartmentService;
import com.example.yygh.hosp.service.HospitalService;
import com.example.yygh.hosp.service.HospitalSetService;
import com.example.yygh.hosp.service.ScheduleService;
import com.example.yygh.model.hosp.Department;
import com.example.yygh.model.hosp.Hospital;
import com.example.yygh.model.hosp.Schedule;
import com.example.yygh.vo.hosp.DepartmentQueryVo;
import com.example.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp")
public class ApiController {

    @Autowired
    HospitalService hospitalService;

    @Autowired
    HospitalSetService hospitalSetService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    ScheduleService scheduleService;

    //删除排班
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request){
        //获取到传递过来的科室信息
        Map<String, String[]> map = request.getParameterMap();
        //把Stirng[]转化为Object
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(map);

        //获取医院编号和排班编号
        String hoscode = (String) stringObjectMap.get("hoscode");
        String hosScheduleId = (String) stringObjectMap.get("hosScheduleId");

        //签名校验
        scheduleService.remove(hoscode,hosScheduleId);

        return Result.ok();
    }

    //查询排班
    @PostMapping("schedule/list")
    public Result findSchedule(HttpServletRequest request){
        //获取到传递过来的科室信息
        Map<String, String[]> map = request.getParameterMap();
        //把Stirng[]转化为Object
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(map);

        //获取医院的编号
        String hoscode = (String) stringObjectMap.get("hoscode");

        //科室编号
        String depcode = (String) stringObjectMap.get("depcode");

        //默认是1
        //获取当前页
        int page = StringUtils.isEmpty(stringObjectMap.get("page")) ? 1 : Integer.parseInt((String) stringObjectMap.get("page"));
        int limit = StringUtils.isEmpty(stringObjectMap.get("limit")) ? 1 : Integer.parseInt((String) stringObjectMap.get("limit"));
        //TODO 签名校验
        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        //调用service
        Page<Schedule> schedulePage = scheduleService.findPageSchedule(page, limit, scheduleQueryVo);

        return Result.ok(schedulePage);
    }


    //上传排班
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        //获取到传递过来的科室信息
        Map<String, String[]> map = request.getParameterMap();
        //把Stirng[]转化为Object
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(map);

        scheduleService.save(stringObjectMap);
        return Result.ok();
    }


    //删除科室
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request){
        //获取到传递过来的科室信息
        Map<String, String[]> map = request.getParameterMap();
        //把Stirng[]转化为Object
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(map);

        //医院编号和科室编号
        String hoscode = (String) stringObjectMap.get("hoscode");
        String descode = (String) stringObjectMap.get("depcode");

        departmentService.removeDepartment(hoscode,descode);
        return Result.ok();
    }

    //查询科室
    @PostMapping("/department/list")
    public Result findDepartment(HttpServletRequest request){
        //获取到传递过来的科室信息
        Map<String, String[]> map = request.getParameterMap();
        //把Stirng[]转化为Object
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(map);

        //获取医院的编号
        String hoscode = (String) stringObjectMap.get("hoscode");

        //默认是1
        //获取当前页
        int page = StringUtils.isEmpty(stringObjectMap.get("page")) ? 1 : Integer.parseInt((String) stringObjectMap.get("page"));
        int limit = StringUtils.isEmpty(stringObjectMap.get("limit")) ? 1 : Integer.parseInt((String) stringObjectMap.get("limit"));
        //TODO 签名校验
        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);

        //调用service
        Page<Department> pageDepartment = departmentService.findPageDepartment(page, limit, departmentQueryVo);

        return Result.ok(pageDepartment);
    }

    //上传科室接口
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        //获取到传递过来的科室信息
        Map<String, String[]> map = request.getParameterMap();
        //把Stirng[]转化为Object
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(map);

        //获取医院编号
        //1.获取医院系统传递过来的签名,签名是MD加密的
        String hospSign = (String) stringObjectMap.get("sign");

        //2.根据医院编码,查询数据库查询签名
        String hoscode = (String) stringObjectMap.get("hoscode");

        //3.根据医院编码查询签名
        String singKey = hospitalSetService.getSingKey(hoscode);

        //4.判断签名是否一致
        if (!hospSign.equals(singKey)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        //调用service方法
        departmentService.save(map);
        return Result.ok();
    }

    //
    @PostMapping("/hospital/show")
    public Result getHospital(HttpServletRequest request){
        //获取到传递过来的医院信息
        Map<String, String[]> map = request.getParameterMap();
        //把Stirng[]转化为Object
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(map);

        //获取医院编号
        //1.获取医院系统传递过来的签名,签名是MD加密的
        String hospSign = (String) stringObjectMap.get("sign");

        //2.根据医院编码,查询数据库查询签名
        String hoscode = (String) stringObjectMap.get("hoscode");

        //3.根据医院编码查询签名
        String singKey = hospitalSetService.getSingKey(hoscode);

        //4.判断签名是否一致
        if (!hospSign.equals(singKey)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        //调用service方法实现根据医院编号查询
        Hospital hospital = hospitalService.getByHoscode(hoscode);

        return Result.ok(hospital);
    }

    //上传医院接口
    @PostMapping("saveHospital")
    public Result saveHosp(HttpServletRequest request){

        //获取到传递过来的医院信息
        Map<String, String[]> map = request.getParameterMap();
        //把Stirng[]转化为Object
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(map);
        
        //1.获取医院系统传递过来的签名,签名是MD加密的
        String hospSign = (String) stringObjectMap.get("sign");

        //2.根据医院编码,查询数据库查询签名
        String hoscode = (String) stringObjectMap.get("hoscode");

        //3.根据医院编码查询签名
        String singKey = hospitalSetService.getSingKey(hoscode);

        //4.判断签名是否一致
        if (!hospSign.equals(singKey)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        //显示图片,base64
        String logoData = (String) stringObjectMap.get("logoData");
        logoData = logoData.replaceAll(" ","+");
        stringObjectMap.put("logoData",logoData);

        //调用service的方法
        hospitalService.save(stringObjectMap);
        return Result.ok();
    }

}
