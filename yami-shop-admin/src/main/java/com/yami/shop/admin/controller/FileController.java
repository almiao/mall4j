/*
 * Copyright (c) 2018-2999 广州亚米信息科技有限公司 All rights reserved.
 *
 * https://www.gz-yami.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.shop.admin.controller;

import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import jdk.jfr.events.ExceptionThrownEvent;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.yami.shop.bean.dto.TinymceEditorDto;
import com.yami.shop.common.bean.Qiniu;
import com.yami.shop.service.AttachFileService;

import javax.servlet.http.HttpServletResponse;

/**
 * 文件上传 controller
 * @author lgh
 *
 */
@RestController
@RequestMapping("/admin/file")
public class FileController {

	@Autowired
	private AttachFileService attachFileService;
	@Autowired
	private Qiniu qiniu;

	@PostMapping("/upload/element")
	public ResponseEntity<String> uploadElementFile(@RequestParam("file") MultipartFile file) throws IOException{
		if(file.isEmpty()){
            return ResponseEntity.noContent().build();
        }
		String fileName = attachFileService.uploadFile(file.getBytes(),file.getOriginalFilename());
        return ResponseEntity.ok(fileName);
	}

	@PostMapping("/upload/tinymceEditor")
	public ResponseEntity<String> uploadTinymceEditorImages(@RequestParam("editorFile") MultipartFile editorFile) throws IOException{
		String fileName =  attachFileService.uploadFile(editorFile.getBytes(),editorFile.getOriginalFilename());
        return ResponseEntity.ok(qiniu.getResourcesUrl() + fileName);
	}


	@PostMapping("/upload/image")
	@ResponseBody
	public String uploadFile(MultipartFile file) throws Exception {
		// 参数列表
		Map<String, Object> map = new HashMap<>();
		map.put("file", file);
		savePic(file.getInputStream(), file.getOriginalFilename());
		//请求接口
		return "www.tangle.ink:8085/download/image/"+file.getOriginalFilename();
	}


	@GetMapping("/download/image")
	@ResponseBody
	public void downloadFile(@RequestParam String fileName, HttpServletResponse response) throws IOException {

		String path = System.getProperty("user.dir")+ File.separator+fileName;
		File tempFile = new File(path);

		InputStream stream = new FileInputStream(tempFile);

		String encodedFilename = URLEncoder.encode(tempFile.getName(), "UTF-8");
		response.setHeader("Content-disposition", String.format("attachment;filename=%s;filename*=UTF-8''%s", encodedFilename, encodedFilename));
		response.setContentType("image/png");

		IOUtils.copy(stream, response.getOutputStream());

	}

	private void savePic(InputStream inputStream, String fileName) {
		OutputStream os = null;
		try {
			String path = System.getProperty("user.dir");
			// 2、保存到临时文件
			// 1K的数据缓冲
			byte[] bs = new byte[1024];
			// 读取到的数据长度
			int len;
			// 输出的文件流保存到本地文件

			File tempFile = new File(path);
			if (!tempFile.exists()) {
				boolean success = tempFile.mkdir();
				if(!success){
					throw new Exception("未创建文件");
				}
			}
			os = new FileOutputStream(tempFile+File.separator+fileName);
			// 开始读取
			while ((len = inputStream.read(bs)) != -1) {
				os.write(bs, 0, len);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 完毕，关闭所有链接
			try {
				os.close();
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}





}
