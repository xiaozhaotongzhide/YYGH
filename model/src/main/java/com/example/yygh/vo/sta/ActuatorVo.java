package com.example.yygh.vo.sta;

import lombok.Data;

import java.util.List;

@Data
public class ActuatorVo {

    private String name;

    private String description;

    private String baseUnit;

    private List<Measurements> measurements ;

    private List<AvailableTags> availableTags ;

}
