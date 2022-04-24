package com.example.yygh.hosp.repository;

import com.example.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends MongoRepository<Department,String>{
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);
}
