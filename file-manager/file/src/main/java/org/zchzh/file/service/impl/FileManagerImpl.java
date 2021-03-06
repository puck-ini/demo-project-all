package org.zchzh.file.service.impl;

import org.zchzh.file.entity.BaseFile;
import org.zchzh.file.entity.VirtualFile;
import org.zchzh.file.model.Range;
import org.zchzh.file.repository.BaseFileRepo;
import org.zchzh.file.service.FileManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zchzh.file.service.FileService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * @author zengchzh
 * @date 2021/7/27
 */
@Slf4j
@Service
public class FileManagerImpl implements FileManager {

    @Resource
    private Map<Class<? extends BaseFile>, FileService<? super BaseFile>> fileServiceMap;

    @Autowired
    private BaseFileRepo fileRepo;

    @Override
    public BaseFile get(Long id) {
        BaseFile baseFile = fileRepo.findById(id).orElse(BaseFile.getNull());
        if (baseFile.isNull()) {
            return baseFile;
        }
        return fileServiceMap.get(baseFile.getClass()).get(id);
    }

    @Override
    public BaseFile create(BaseFile file) {
        return fileServiceMap.get(file.getClass()).create(file);
    }

    @Override
    public BaseFile upload(BaseFile file) {
        return fileServiceMap.get(file.getClass()).upload(file);
    }

    @Override
    public void download(Long id) {
        BaseFile file = get(id);
        if (!file.isNull()) {
            fileServiceMap.get(file.getClass()).download(id);
        }
    }

    @Override
    public void remove(Long id) {
        BaseFile file = get(id);
        if (!file.isNull()) {
            fileServiceMap.get(file.getClass()).remove(id);
        }
    }

    @Override
    public List<BaseFile> list() {
        return fileRepo.findAll();
    }

    @Override
    public void uploadChunk(HttpServletRequest request, HttpServletResponse response, Long folderId) {

    }

    @Override
    public void downloadChunk(HttpServletRequest request, HttpServletResponse response, Long id) {
        Range range = Range.getRange(request);
        BaseFile baseFile = get(id);
        if (baseFile instanceof VirtualFile) {
            VirtualFile file = (VirtualFile) baseFile;
            Long len = Objects.nonNull(range.getLen()) ? range.getLen() : file.getSize();
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setContentType(file.getContentType());
            //??????????????? ???????????????????????? ???????????? ???????????????
            response.addHeader("Content-Range",
                    String.format("bytes %d-%d/%d", range.getBegin(), len, file.getSize())
            );
            //??????????????? ???????????????????????????
            response.addHeader("Content-Length", String.valueOf(len));
            //??????????????? ???????????????????????????????????????????????????
            response.addHeader("Accept-Ranges", "bytes");
            //private ??????????????????????????????????????????????????????????????????????????????????????????????????????intermediate???????????????????????????
            response.addHeader("Cache-control", "private");
            //??????????????? ???????????????????????????????????????????????????
            response.addHeader("Content-Disposition", "filename=" + file.getOriginName());
            try (InputStream is = file.getInputStream();
                 OutputStream os = response.getOutputStream();) {
                if (Objects.isNull(is)) {
                    throw new IllegalArgumentException("?????????????????????");
                }
                is.skip(range.getBegin());
                copy(is, os, len);
            } catch (IOException e) {
                log.error("??????????????????", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    private void copy(InputStream is, OutputStream os, Long len) throws IOException {
        byte[] buf = new byte[1024];
        while (len > 0) {
            is.read(buf);
            long l = len > 1024 ? 1024 : len;
            os.write(buf, 0, (int) l);
            os.flush();
            len -= l;
        }
    }

}
