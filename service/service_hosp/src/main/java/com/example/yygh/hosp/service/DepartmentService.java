package com.example.yygh.hosp.service;

import com.example.yygh.model.hosp.Department;
import com.example.yygh.vo.hosp.DepartmentQueryVo;
import com.example.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    //保存科室接口
    void save(Map<String, String[]> map);

    //查询科室接口
    Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo);

    //删除科室接口
    void removeDepartment(String hoscode, String descode);

    List<DepartmentVo> getDeptTree(String hoscode);

    String getDepName(String hoscode, String depcode);

    Department getDepartment(String hoscode, String depcode);
}
