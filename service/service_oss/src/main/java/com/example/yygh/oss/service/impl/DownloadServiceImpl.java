package com.example.yygh.oss.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yygh.hosp.client.HospitalFeignClient;
import com.example.yygh.model.order.OrderInfo;
import com.example.yygh.model.oss.Download;
import com.example.yygh.order.client.OrderFeignClient;
import com.example.yygh.oss.mapper.DownloadMapper;
import com.example.yygh.oss.service.DownloadService;
import com.example.yygh.vo.download.DownloadCountQueryVo;
import com.example.yygh.vo.download.OrderVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class DownloadServiceImpl extends ServiceImpl<DownloadMapper, Download> implements DownloadService {

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private FileSerivceImpl fileSerivceImpl;

    @Override
    public void saveDownload(DownloadCountQueryVo downloadCountQueryVo) {
        try {
            Download download = new Download();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String hoscode = hospitalFeignClient.getHoscode(downloadCountQueryVo.getHosname());
            download.setDownloadDateBegin(sdf.parse(downloadCountQueryVo.getDownloadDateBegin()));
            download.setDownloadDateEnd(sdf.parse(downloadCountQueryVo.getDownloadDateEnd()));
            download.setHosname(downloadCountQueryVo.getHosname());
            download.setStatus(0);
            download.setHoscode(hoscode);
            download.setIsDeleted(0);
            download.setCreateTime(new Date());
            download.setUpdateTime(new Date());
            baseMapper.insert(download);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startDownload() {
        //???????????????states???????????????
        QueryWrapper<Download> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "0");
        List<Download> downloads = baseMapper.selectList(queryWrapper);
        for (Download download : downloads) {
            coreDownload(download);
        }
    }


    @Override
    public void upadteDownload(String fileUrl, Download download, Integer status) {
        download.setStatus(status);
        download.setFileUrl(fileUrl);
        download.setUpdateTime(new Date());
        baseMapper.updateById(download);
    }

    @Override
    public IPage<Download> selectPage(Page<Download> pageParams, DownloadCountQueryVo downloadCountQueryVo) {
        //???????????????
        String hosname = downloadCountQueryVo.getHosname();
        String downloadDateBegin = downloadCountQueryVo.getDownloadDateBegin();
        String downloadDateEnd = downloadCountQueryVo.getDownloadDateEnd();
        //????????????????????????
        QueryWrapper<Download> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(hosname)) {
            queryWrapper.eq("hosname", hosname);
        }
        if (!StringUtils.isEmpty(downloadDateBegin)) {
            queryWrapper.ge("downloadDateBegin", downloadDateBegin);
        }
        if (!StringUtils.isEmpty(downloadDateEnd)) {
            queryWrapper.le("downloadDateEnd", downloadDateEnd);
        }
        //??????mapper??????
        Page<Download> downloadPage = baseMapper.selectPage(pageParams, queryWrapper);
        return downloadPage;
    }


    public void coreDownload(Download download) {
        List<OrderInfo> orderList = orderFeignClient.getDownload(download);
        //????????????????????????????????????????????????
        List<OrderVo> orderVoList = orderList.stream().map(data -> {
            OrderVo orderVo = new OrderVo();
            BeanUtils.copyProperties(data, orderVo);
            return orderVo;
        }).collect(Collectors.toList());
        //???????????????
        String fileName = "D:\\BaiduNetdiskDownload\\" + download.getHoscode() + "_" + download.getId() + ".xlsx";
        String file = download.getHoscode() + "_" + download.getId() + ".xlsx";

        log.info(fileName);
        EasyExcel.write(fileName, OrderVo.class).sheet().doWrite(orderVoList);
        String zipFileName = "D:\\BaiduNetdiskDownload\\" + download.getHoscode() + "_" + download.getId() + ".zip";
        String zipName = download.getHoscode() + "_" + download.getId() + ".zip";
        try {
            zip(fileName, zipFileName, file);
            String fileUrl = fileSerivceImpl.uplodad(new File(zipFileName), zipName);
            upadteDownload(fileUrl, download, 1);
        } catch (Exception e) {
            upadteDownload(null, download, 2);
        }
    }


    /**
     * ??????
     */
    public void zip(String input, String output, String name) throws Exception {
        //????????????????????????
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(output));
        //?????????????????????????????????
        String[] paths = input.split("\\|");
        File[] files = new File[paths.length];
        byte[] buffer = new byte[1024];
        for (int i = 0; i < paths.length; i++) {
            files[i] = new File(paths[i]);
        }
        for (int i = 0; i < files.length; i++) {
            FileInputStream fis = new FileInputStream(files[i]);
            if (files.length == 1 && name != null) {
                out.putNextEntry(new ZipEntry(name));
            } else {
                out.putNextEntry(new ZipEntry(files[i].getName()));
            }
            int len;
            // ????????????????????????????????????????????????zip??????
            while ((len = fis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.closeEntry();
            fis.close();
        }
        out.close();
    }
}
