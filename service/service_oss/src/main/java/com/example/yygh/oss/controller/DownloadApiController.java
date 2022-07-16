package com.example.yygh.oss.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yygh.common.result.Result;
import com.example.yygh.model.oss.Download;
import com.example.yygh.oss.service.DownloadService;
import com.example.yygh.vo.download.DownloadCountQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/oss")
@Slf4j
public class DownloadApiController {

    @Autowired
    private DownloadService downloadService;


    /**
     * 开启下载任务
     *
     * @param downloadCountQueryVo
     */
    @PostMapping("postDownload")
    public void postDownload(DownloadCountQueryVo downloadCountQueryVo) {
        downloadService.saveDownload(downloadCountQueryVo);
    }

    /**
     * 获取下载中心数据
     *
     * @param
     */
    @GetMapping("{page}/{limit}")
    public Result getDownload(@PathVariable Long page,
                              @PathVariable Long limit,
                              DownloadCountQueryVo downloadCountQueryVo) {
        Page<Download> pageParams = new Page<>(page, limit);
        IPage<Download> pageModel = downloadService.selectPage(pageParams, downloadCountQueryVo);
        return Result.ok(pageModel);
    }

    /**
     * 前端给一个测试接口
     */
    @GetMapping("getTest")
    public void getTest() {
        log.info("下载测试接口");
        downloadService.startDownload();
    }
}
