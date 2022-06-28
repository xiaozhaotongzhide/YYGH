package com.example.yygh.hosp.service;

import com.example.yygh.model.hosp.Schedule;
import com.example.yygh.vo.hosp.ScheduleOrderVo;
import com.example.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    void save(Map<String, Object> stringObjectMap);

    Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    void remove(String hoscode, String hosScheduleId);

    Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode);

    List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate);

    Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);

    Schedule getSchedule(String scheduleId);

    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    //更新排班数据
    void update(Schedule schedule);
}
