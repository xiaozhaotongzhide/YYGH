package com.example.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.yygh.hosp.repository.DepartmentRepository;
import com.example.yygh.hosp.service.DepartmentService;
import com.example.yygh.model.hosp.Department;
import com.example.yygh.vo.hosp.DepartmentQueryVo;
import com.example.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void save(Map<String, String[]> map) {
        //把map转化为department对象
        String departmentStr = JSONObject.toJSONString(map);
        Department department = JSONObject.parseObject(departmentStr, Department.class);

        //寻找这个科室编号,有的话作更新,没有做添加
        Department department1 = departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());

        //判断
        if (department1 != null) {

            department1.setUpdateTime(new Date());
            department1.setIsDeleted(0);
            departmentRepository.save(department1);
        }else {

            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }

    @Override
    public Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo, department);
        department.setIsDeleted(0);
        //创建一个pageable对象,设置当前页和每页记录数
        //0是第一页
        Pageable pageable = PageRequest.of(page - 1,limit);
        //创建example对象
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        Example<Department> example = Example.of(department,matcher);

        Page<Department> all = departmentRepository.findAll(example, pageable);
        return all;
    }

    //删除科室
    @Override
    public void removeDepartment(String hoscode, String descode) {
        //根据医院编号和科室编号
        Department departmentByHoscodeAndDepcode = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, descode);
        if (departmentByHoscodeAndDepcode != null) {
            //调用方法
            departmentRepository.deleteById(departmentByHoscodeAndDepcode.getId());
        }
    }

    //根据医院编号,查询医院所有科室列表
    @Override
    public List<DepartmentVo> getDeptTree(String hoscode) {
        //创建一个list集合,用于最终数据的封装
        List<DepartmentVo> result = new ArrayList<>();
        //根据医院编号,查询医院所有科室信息
        Department departmentQuery = new Department();
        departmentQuery.setHoscode(hoscode);
        Example example = Example.of(departmentQuery);
        //所有科室列表
        List<Department> all = departmentRepository.findAll(example);

        //根据大科室编号bigcode分组
        Map<String, List<Department>> listMap = all.stream().collect(Collectors.groupingBy(Department::getBigcode));

        for (Map.Entry<String,List<Department>> entry:listMap.entrySet()){
            //大科室编号
            String bigcode = entry.getKey();
            //大科室对应的全局数据
            List<Department> departmentList = entry.getValue();

            //封装大科室
            DepartmentVo departmentVo = new DepartmentVo();
            departmentVo.setDepcode(bigcode);
            departmentVo.setDepname(departmentList.get(0).getBigname());

            //封装小科室
            List<DepartmentVo> childrenList = new ArrayList<>();
            for(Department department:departmentList){
                DepartmentVo departmentQueryVo1 = new DepartmentVo();
                departmentQueryVo1.setDepname(department.getDepname());
                departmentQueryVo1.setDepcode(department.getDepcode());
                childrenList.add(departmentQueryVo1);
            }
            //把小科室的list放到大科室children里面
            departmentVo.setChildren(childrenList);
            //放到result里面
            result.add(departmentVo);
        }
        return result;
    }

    //根据医院编号和科室编号查询科室名称
    @Override
    public String getDepName(String hoscode, String depcode) {
        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode).getDepname();
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }
}
