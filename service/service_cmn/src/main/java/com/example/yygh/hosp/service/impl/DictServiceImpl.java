package com.example.yygh.hosp.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yygh.hosp.listener.DictListener;
import com.example.yygh.hosp.mapper.DictMapper;
import com.example.yygh.hosp.service.DictService;
import com.example.yygh.model.cmn.Dict;
import com.example.yygh.vo.cmn.DictEeVo;
import com.sun.deploy.net.URLEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {


    //根据数据id查询子数据列表
    @Cacheable(value = "dict",keyGenerator = "keyGenerator")
    @Override
    public List<Dict> findChildData(long id) {
        log.info("通过数据库查询");
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id);
        List<Dict> dicts = baseMapper.selectList(queryWrapper);
        for (Dict dict : dicts) {
            Long id1 = dict.getId();
            boolean children = this.isChildren(id1);
            dict.setHasChildren(children);
        }
        return dicts;
    }

    @Override
    public void exportDictData(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = null;
            fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            //查询数据库
            List<Dict> dictList = baseMapper.selectList(null);

            List<DictEeVo> dictVoList = new ArrayList<>();

            for (Dict dict : dictList) {
                DictEeVo dictEeVo = new DictEeVo();
                BeanUtils.copyProperties(dict, dictEeVo);
                dictVoList.add(dictEeVo);
            }
            //调用方法执行写操作
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("dict")
                    .doWrite(dictVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @CacheEvict(value = "dict",allEntries = true)
    @Override
    public void importDict(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(),DictEeVo.class,new DictListener(baseMapper))
                    .sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDictName(String dictCode, String value) {
        //如果dictCode为空,直接根据value查询
        if(StringUtils.isEmpty(dictCode)){
            //直接根据value查询
            QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("value",value);
            Dict dict = baseMapper.selectOne(queryWrapper);
            return dict.getName();
        }else {
            //根据dictCode不为空
            QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("dict_code",dictCode);
            Dict CodeDict = baseMapper.selectOne(queryWrapper);
            //根据parentid查询值和value查询
            Long parent_id = CodeDict.getId();
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>()
                    .eq("parent_id", parent_id)
                    .eq("value", value));
            return dict.getName();
        }
    }

    //
    public Dict getDictByDictCode(String dictCode){
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dict_code",dictCode);
        Dict dict = baseMapper.selectOne(queryWrapper);
        return dict;
    }


    //根据dictCode获取下级结点
    @Override
    public List<Dict> findByDictCode(String dictCode) {
        //根据dictcode获取id
        Dict dictByDictCode = this.getDictByDictCode(dictCode);
        //根据id获取子节点
        List<Dict> childData = this.findChildData(dictByDictCode.getId());
        return childData;
    }

    //判断id下面是否有子节点
    private boolean isChildren(Long id) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id);
        Integer integer = baseMapper.selectCount(queryWrapper);
        return integer > 0;
    }
}
