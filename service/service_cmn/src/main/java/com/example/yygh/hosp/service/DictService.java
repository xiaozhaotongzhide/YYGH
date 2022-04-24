package com.example.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


public interface DictService extends IService<Dict> {
    List<Dict> findChildData(long id);

    void exportDictData(HttpServletResponse response);

    void importDict(MultipartFile file);

    String getDictName(String dictCode, String value);

    List<Dict> findByDictCode(String dictCode);
}
