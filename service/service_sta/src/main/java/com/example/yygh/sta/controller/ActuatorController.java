package com.example.yygh.sta.controller;

import com.example.yygh.common.result.Result;
import com.example.yygh.sta.service.cpuService;
import com.example.yygh.sta.service.ramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/admin/sta")
public class ActuatorController {

    @Autowired
    private cpuService cpuService;

    @Autowired
    private ramService ramService;

    @GetMapping("/armTest")
    public void armTest() {
        ramService.save();
    }

    @GetMapping("/cpuTest")
    public void cpuTest() {
        cpuService.save();
    }

    @GetMapping("getCpu")
    public Result showCpu() {
        return Result.ok(cpuService.show());
    }

    @GetMapping("getRam")
    public Result showRam() {
        return Result.ok(ramService.show());
    }
}
