package com.example.yygh.user.api;

import com.example.yygh.common.result.Result;
import com.example.yygh.common.utils.AuthContextHolder;
import com.example.yygh.model.user.Patient;
import com.example.yygh.user.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/user/patient")
public class PatientApiController {

    @Autowired
    private PatientService patientService;

    //获取就诊人列表
    @GetMapping("/auth/findAll")
    public Result findAll(HttpServletRequest request) {
        //获取当前登录用户的id值
        List<Patient> patients = patientService.findAllByUserId(AuthContextHolder.getUserId(request));
        return Result.ok(patients);
    }

    //添加就诊人
    @PostMapping("/auth/save")
    public Result savePatient(@RequestBody Patient patient, HttpServletRequest request) {
        //获取当前登录用户id
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return Result.ok();
    }

    //根据id获取就诊人信息
    @GetMapping("/auth/get/{id}")
    public Result findByid(@PathVariable("id") Long id){
        Patient patientId = patientService.getPatientId(id);
        return Result.ok(patientId);
    }

    //修改就诊人
    @PostMapping("/auth/upadte")
    public Result updatePatient(Patient patient){
        patientService.updateById(patient);
        return Result.ok();
    }

    //删除就诊人
    @DeleteMapping("/auth/delete/{id}")
    public Result deletePatient(@PathVariable("id") Long id){
        patientService.removeById(id);
        return Result.ok();
    }

    //通过feign根据id获取就诊人信息
    @GetMapping("/inner/get/{id}")
    public Patient innerFindByid(@PathVariable("id") Long id){
        return patientService.getPatientId(id);
    }
}
