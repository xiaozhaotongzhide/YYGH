package com.example.yygh.oss.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.oss.Download;
import com.example.yygh.vo.download.DownloadCountQueryVo;

public interface DownloadService extends IService<Download> {
    void saveDownload(DownloadCountQueryVo downloadCountQueryVo);

    void startDownload();

    void upadteDownload(String fileUrl, Download download, Integer status);

    IPage<Download> selectPage(Page<Download> pageParams, DownloadCountQueryVo downloadCountQueryVo);
}
