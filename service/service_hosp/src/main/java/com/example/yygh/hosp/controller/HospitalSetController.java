package com.example.yygh.hosp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yygh.common.exception.YyghException;
import com.example.yygh.common.result.MD5;
import com.example.yygh.hosp.service.HospitalSetService;
import com.example.yygh.model.hosp.HospitalSet;
import com.example.yygh.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.yygh.common.result.Result;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Random;

@Api("医院设置管理")
@RequestMapping("/admin/hosp/hospitalSet")
@RestController
//@CrossOrigin
public class HospitalSetController {

    //出入service
    @Autowired
    HospitalSetService hospitalSetService;

    //url = http://localhost:8201/admin/hosp/hospitalSet/findAll

    //1.先做一个简单的测试查询医院设置表的所有信息
    @ApiOperation("获取所有医院设置")
    @GetMapping("findAll")
    public Result findHospitalSetList() {
        //调用service方法
        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);
    }

    //2.根据id删除
    @ApiOperation("逻辑删除医院设置")
    @DeleteMapping("{id}")
    public Result deleteHospitalSetById(@PathVariable("id") Long id) {
        boolean b = hospitalSetService.removeById(id);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //3.条件查询带分页
    @ApiOperation("条件查询医院设置")
    @PostMapping("findPageHospSet/{current}/{limit}")
    public Result findPageHostSet(@PathVariable("current") long current,
                                  @PathVariable("limit") long limit,
//                                这样方便前端只需要在前端写一个json后端就可以自动注入到实体类中
                                  @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo) {

        //创建一共page对象,传递当前页,每页记录数
        Page<HospitalSet> page = new Page<>(current, limit);
        //调用方法实现分页
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        String hoscode = hospitalSetQueryVo.getHoscode();
        String hosname = hospitalSetQueryVo.getHosname();
        //因为我们有的时候不会两个条件都有
        if (!StringUtils.isEmpty(hosname)) {
            queryWrapper.like("hosname", hospitalSetQueryVo.getHosname());
        }
        if (!StringUtils.isEmpty(hoscode)) {
            queryWrapper.eq("hoscode", hospitalSetQueryVo.getHoscode());
        }

        //调用方法实现分页查询
        Page<HospitalSet> pageHospitalSet = hospitalSetService.page(page, queryWrapper);

        //返回结果
        return Result.ok(pageHospitalSet);
    }

    //4.添加医院设置
    @ApiOperation("添加医院设置")
    @PostMapping("saveHospitalSet")
    public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet) {
        //设置状态 1.可以使用 0.不能使用
        hospitalSet.setStatus(1);
        //签名密钥
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + ""+random.nextInt(1000)));

        //调用service
        boolean save = hospitalSetService.save(hospitalSet);
        if (save) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //5.根据id获取医院设置
    @ApiOperation("根据id获取医院设置")
    @GetMapping("getHospitalSet/{id}")
    public Result getHospitalSet(@PathVariable Long id){
        HospitalSet byId = hospitalSetService.getById(id);
        /*try {
            int id1 = 10/0;
        }catch (Exception e) {
            throw new YyghException("0不能做除数",202);
        }*/

        return Result.ok(byId);
    }

    //6.修改医院设置
    @ApiOperation("修改医院设置")
    @PostMapping("updateHospitalSet")
    public Result updateHospitalSet(@RequestBody HospitalSet hospitalSet){
        boolean b = hospitalSetService.updateById(hospitalSet);
        if (b) {
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    //7.批量删除
    @ApiOperation("批量删除医院")
    @DeleteMapping("batchRemoveHospitalSet")
    public Result batchDeleteHospitalSet(@RequestBody List<Long> ids){
        boolean b = hospitalSetService.removeByIds(ids);
        if (b) {
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    //8.医院设置和解锁
    @PutMapping("lockHospitalSet/{id}/{staus}")
    public Result lockHospitalSet(@PathVariable Long id,
                                  @PathVariable Integer staus){
        //根据id查询设置信息
        HospitalSet byId = hospitalSetService.getById(id);
        //设置状态
        byId.setStatus(staus);
        //调用方法
        hospitalSetService.updateById(byId);

        return Result.ok();
    }

    //9.发送签名密钥
    @PutMapping("sendKey/{id}")
    public Result lockHospitalSet(@PathVariable Long id){
        //根据id查询设置信息
        HospitalSet byId = hospitalSetService.getById(id);
        String signKey = byId.getSignKey();
        String hoscode = byId.getHoscode();
        //TODD 发送短信
        return Result.ok();
    }

}