package com.example.yygh.hosp.controller.ApiController;

import com.example.yygh.common.result.Result;
import com.example.yygh.hosp.service.DepartmentService;
import com.example.yygh.hosp.service.HospitalService;
import com.example.yygh.model.hosp.Hospital;
import com.example.yygh.vo.hosp.DepartmentVo;
import com.example.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp/hospital")
public class HospApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation(value = "查询医院列表")
    @GetMapping("/findHospList/{page}/{limit}")
    public Result findHospList(@PathVariable Integer page,
                               @PathVariable Integer limit,
                               HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> hospitals = hospitalService.selectHospPage(page, limit, hospitalQueryVo);
        int totalPages = hospitals.getTotalPages();
        return Result.ok(hospitals);
    }

    @ApiOperation(value = "根据医院名称进行查询")
    @GetMapping("findByHospName/{hosname}")
    public Result findHospName(@PathVariable(value = "hosname") String hosname){
        List<Hospital> list = hospitalService.findByHosname(hosname);
        return Result.ok(list);
    }

    @ApiOperation(value = "根据医院编号获取科室")
    @GetMapping("department/{hoscode}")
    public Result index(@PathVariable(value = "hoscode") String hoscode){
        List<DepartmentVo> list = departmentService.getDeptTree(hoscode);
        return Result.ok(list);
    }

    @ApiOperation(value = "根据医院编号获取医院详情")
    @GetMapping("findHospDetail/{hoscode}")
    public Result item(@PathVariable(value = "hoscode") String hoscode){
        Map<String,Object> map = new HashMap<>();
        map= hospitalService.item(hoscode);
        return Result.ok(map);
    }
}
