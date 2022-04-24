package com.example.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.yygh.hosp.repository.ScheduleRepository;
import com.example.yygh.hosp.service.DepartmentService;
import com.example.yygh.hosp.service.HospitalService;
import com.example.yygh.hosp.service.ScheduleService;
import com.example.yygh.model.hosp.Schedule;
import com.example.yygh.vo.hosp.BookingScheduleRuleVo;
import com.example.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleServiceImpl implements ScheduleService {


    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;
    //上传排版接口
    @Override
    public void save(Map<String, Object> stringObjectMap) {
        //查一下数据库有就更新没有就上传
        //把map转化为department对象
        String ScheduleStr = JSONObject.toJSONString(stringObjectMap);
        Schedule schedule = JSONObject.parseObject(ScheduleStr, Schedule.class);

        //寻找这个科室编号,有的话作更新,没有做添加
        Schedule schedule1 = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());

        //判断
        if (schedule1 != null) {
            schedule1.setUpdateTime(new Date());
            schedule1.setIsDeleted(0);
            schedule1.setStatus(1);
            scheduleRepository.save(schedule1);
        }else {
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        }
    }

    @Override
    public Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo, schedule);
        schedule.setIsDeleted(0);
        schedule.setStatus(1);
        //创建一个pageable对象,设置当前页和每页记录数
        //0是第一页
        Pageable pageable = PageRequest.of(page - 1,limit);
        //创建example对象
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        Example<Schedule> example = Example.of(schedule,matcher);

        Page<Schedule> all = scheduleRepository.findAll(example, pageable);
        return all;
    }

    //删除排班
    @Override
    public void remove(String hoscode, String hosScheduleId) {
        //根据我们的医院编号和排班编号
        Schedule scheduleByHoscodeAndHosScheduleId = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (scheduleByHoscodeAndHosScheduleId != null) {
            scheduleRepository.deleteById(scheduleByHoscodeAndHosScheduleId.getId());
        }
    }

    @Override
    public Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode) {
        // 1.根据医院编号和科室编号查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        // 2.根据工作日workData进行分组
        // 构造条件
        // 用于做聚合操作
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),//匹配条件
                Aggregation.group("workDate")//分组字段
                        .first("workDate").as("workDate")
                        // 3.统计号源数量
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.DESC,"workDate"),
                // 4.实现分页
                Aggregation.skip((page - 1)*limit),
                Aggregation.limit(limit)
        );

        //查询
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class).getMappedResults();

        //分组查询的总记录数
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );

        AggregationResults<BookingScheduleRuleVo> totalAggResults = mongoTemplate.aggregate(totalAgg, Schedule.class,BookingScheduleRuleVo.class);
        int total = totalAggResults.getMappedResults().size();

        //把日期对应的星期获取
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }

        //设置最终的数据
        HashMap<String, Object> Result = new HashMap<>();
        Result.put("bookingScheduleRuleVoList",bookingScheduleRuleVoList);
        Result.put("total",total);

        //获取医院的名称
        String hosName = hospitalService.getHospName(hoscode);
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname",hosName);
        Result.put("baseMap",baseMap);

        return Result;
    }

    //根据医院编号,科室编号,工作日期,查询排班详细信息
    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        List<Schedule> scheduleList = scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workDate).toDate());
        //遍历list集合,向设置其他值,医院名称,科室名称,日期对应星期
        scheduleList.stream().forEach(item -> {
            this.packageSchedule(item);
        });

        return scheduleList;
    }

    private void packageSchedule(Schedule item) {
        //设置医院的名称
        item.getParam().put("hosname",hospitalService.getHospName(item.getHoscode()));
        //设置科室名称
        item.getParam().put("depname",departmentService.getDepName(item.getHoscode(),item.getDepcode()));
        //设置日期对应星期
        item.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(item.getWorkDate())));
    }

    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }
}
