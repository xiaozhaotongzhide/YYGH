package com.example.yygh.hosp.controller;

import com.example.yygh.common.result.Result;
import com.example.yygh.hosp.service.DepartmentService;
import com.example.yygh.model.hosp.Department;
import com.example.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hosp/department")
//@CrossOrigin
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    //根据医院的编号查询所有科室列表
    @ApiOperation(value = "查询医院所有科室列表")
    @GetMapping("getDeptList/{hoscode}")
    public Result getDeptList(@PathVariable String hoscode) {
        List<DepartmentVo> departmentVoList = departmentService.getDeptTree(hoscode);
        return Result.ok(departmentVoList);
    }
}
