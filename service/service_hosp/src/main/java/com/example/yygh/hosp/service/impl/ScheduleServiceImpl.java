package com.example.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yygh.common.exception.YyghException;
import com.example.yygh.common.result.ResultCodeEnum;
import com.example.yygh.hosp.repository.ScheduleRepository;
import com.example.yygh.hosp.service.DepartmentService;
import com.example.yygh.hosp.service.HospitalService;
import com.example.yygh.hosp.service.ScheduleService;
import com.example.yygh.model.hosp.BookingRule;
import com.example.yygh.model.hosp.Department;
import com.example.yygh.model.hosp.Hospital;
import com.example.yygh.model.hosp.Schedule;
import com.example.yygh.vo.hosp.BookingScheduleRuleVo;
import com.example.yygh.vo.hosp.ScheduleOrderVo;
import com.example.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {


    @Autowired
    private ScheduleRepository scheduleRepository;

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
        Schedule schedule1 = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());

        //判断
        if (schedule1 != null) {
            schedule1.setUpdateTime(new Date());
            schedule1.setIsDeleted(0);
            schedule1.setStatus(1);
            scheduleRepository.save(schedule1);
        } else {
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
        Pageable pageable = PageRequest.of(page - 1, limit);
        //创建example对象
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        Example<Schedule> example = Example.of(schedule, matcher);

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
                Aggregation.sort(Sort.Direction.DESC, "workDate"),
                // 4.实现分页
                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)
        );

        //查询
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class).getMappedResults();

        //分组查询的总记录数
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );

        AggregationResults<BookingScheduleRuleVo> totalAggResults = mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);
        int total = totalAggResults.getMappedResults().size();

        //把日期对应的星期获取
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }

        //设置最终的数据
        HashMap<String, Object> Result = new HashMap<>();
        Result.put("bookingScheduleRuleVoList", bookingScheduleRuleVoList);
        Result.put("total", total);

        //获取医院的名称
        String hosName = hospitalService.getHospName(hoscode);
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname", hosName);
        Result.put("baseMap", baseMap);

        return Result;
    }

    //根据医院编号,科室编号,工作日期,查询排班详细信息
    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        List<Schedule> scheduleList = scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate).toDate());
        //遍历list集合,向设置其他值,医院名称,科室名称,日期对应星期
        scheduleList.stream().forEach(item -> {
            this.packageSchedule(item);
        });

        return scheduleList;
    }
    //获取可预约日期的数据(分页)
    private IPage getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        //当天放号时间
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //预约周期
        Integer cycle = bookingRule.getCycle();
        //如果当天放号时间已过，则预约周期后一天为即将放号时间，周期加1
        if (releaseTime.isAfterNow()) {
            cycle += 1;
        }
        //可预约所有日期，最后一天显示即将放号倒计时
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            //计算当前预约日期
            DateTime curDateTime = new DateTime().plusDays(i);
            String dateString = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateString).toDate());
        }
        //因为周期不同,每页显示的日期最多7天时间,超过七天分页
        List<Date> pageDateList = new ArrayList<>();
        int start = (page - 1) * limit;
        int end = (page - 1) * limit + limit;
        //如果可以显示的数据小于7,直接显示
        if (end > dateList.size()) {
            end = dateList.size();
        }
        for (int i = start; i < end; i++) {
            pageDateList.add(dateList.get(i));
        }
        //如果大于7,进行分页
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, 7, dateList.size());
        iPage.setRecords(pageDateList);
        return iPage;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        HashMap<String, Object> result = new HashMap<>();
        //获取预约规则
        //根据医院编号获取预约规则
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if (ObjectUtils.isEmpty(hospital)) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        //获取可预约日期的数据(分页)
        IPage iPage = this.getListDate(page, limit, bookingRule);
        //获取当前可预约的时间
        List<Date> dateList = iPage.getRecords();

        //获取可预约日期里面的科室剩余预约数
        List<BookingScheduleRuleVo> scheduleVoList = getBookingScheduleRuleVos(hoscode, depcode, dateList);

        //合并数据 map集合 key日期 value预约规则和剩余数量
        HashMap<Date, BookingScheduleRuleVo> scheduleRuleVoMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(scheduleVoList)) {
            scheduleRuleVoMap = (HashMap<Date, BookingScheduleRuleVo>) scheduleVoList.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate,
                    BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }

        //获取可预约的排班规则
        ArrayList<BookingScheduleRuleVo> bookingScheduleRuleVoList = getScheduleRuleVos(page, bookingRule, iPage, dateList, scheduleRuleVoMap);

        //可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        //科室
        Department department = departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;

    }

    //获取可预约的排班规则
    private ArrayList<BookingScheduleRuleVo> getScheduleRuleVos(Integer page, BookingRule bookingRule, IPage iPage, List<Date> dateList, HashMap<Date, BookingScheduleRuleVo> scheduleRuleVoMap) {
        ArrayList<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i = 0, len = dateList.size(); i < len; i++) {
            Date date = dateList.get(i);

            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleRuleVoMap.get(toDate(date));
            if (null == bookingScheduleRuleVo) { // 说明当天没有排班医生
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                //科室剩余预约数  -1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //计算当前预约日期为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            //最后一页最后一条记录为即将预约   状态 0：正常 1：即将放号 -1：当天已停止挂号
            if (i == len - 1 && page == iPage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //当天预约如果过了停号时间， 不能预约
            if (i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if (stopTime.isBeforeNow()) {
                    //停止预约
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }
        return bookingScheduleRuleVoList;
    }

    private List<BookingScheduleRuleVo> getBookingScheduleRuleVos(String hoscode, String depcode, List<Date> dateList) {
        //构建条件
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> bookingScheduleRuleVos = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleVoList = bookingScheduleRuleVos.getMappedResults();
        return scheduleVoList;
    }

    @Override
    public Schedule getSchedule(String scheduleId) {
        Schedule byId = scheduleRepository.findById(scheduleId).get();
        Schedule schedule = packageSchedule(byId);
        return schedule;
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        //获取排班信息
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        if (ObjectUtils.isEmpty(schedule)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //获取预约挂号的规则
        Hospital hoscode = hospitalService.getByHoscode(schedule.getHoscode());
        if (ObjectUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        BookingRule bookingRule = hoscode.getBookingRule();
        if (ObjectUtils.isEmpty(bookingRule)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //把获取的数据设置到scheduleOrderVo
        BeanUtils.copyProperties(schedule, scheduleOrderVo);
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setHosname(hospitalService.getHospName(schedule.getHoscode()));
        scheduleOrderVo.setDepname(departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime startTime = getDateTime(scheduleOrderVo, schedule, bookingRule, quitDay);
        scheduleOrderVo.setStartTime(startTime.toDate());
        return scheduleOrderVo;
    }

    //更新排班信息,用于mq
    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }

    private DateTime getDateTime(ScheduleOrderVo scheduleOrderVo, Schedule schedule, BookingRule bookingRule, int quitDay) {
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        return startTime;
    }

    //把一个时间带时分秒转换为不带时分秒的
    private Date toDate(Date date) {
        return new DateTime(date).toDate();
    }



    private Schedule packageSchedule(Schedule item) {
        //设置医院的名称
        item.getParam().put("hosname", hospitalService.getHospName(item.getHoscode()));
        //设置科室名称
        item.getParam().put("depname", departmentService.getDepName(item.getHoscode(), item.getDepcode()));
        //设置日期对应星期
        item.getParam().put("dayOfWeek", this.getDayOfWeek(new DateTime(item.getWorkDate())));
        //返回科室
        return item;
    }

    /**
     * 根据日期获取周几数据
     *
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
