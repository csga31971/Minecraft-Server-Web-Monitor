
package xyz.csga.controller;

import xyz.csga.config.SystemPropertiesConfig;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;

@Controller
@RequestMapping("/")
public class MainController {

	@Autowired
	private static SimpMessagingTemplate template;

	@Autowired
	private SystemPropertiesConfig systemPropertiesConfig;

	@RequestMapping("index")
	public String index() {
		return "index";
	}

	@RequestMapping("mb")
	public ModelAndView mb() {

		ModelAndView modelAndView = new ModelAndView("mb");
		OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		modelAndView.addObject("totalMemory", os.getTotalPhysicalMemorySize() / 1048576);
		modelAndView.addObject("freeMemory", os.getFreePhysicalMemorySize() / 1048576);
		modelAndView.addObject("cpuUsage", os.getSystemCpuLoad());

		File modsDir = new File(systemPropertiesConfig.getModLocation());
		if(modsDir.isDirectory()){
			List<File> modFiles = Arrays.asList(Objects.requireNonNull(modsDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.contains(".jar");
				}
			})));
			modelAndView.addObject("modFiles", modFiles);
		}

		File serverProperties = new File(systemPropertiesConfig.getMcLocation() + "/server.properties");
//		File serverProperties = new File(systemPropertiesConfig.getMcLocation() + "\\1.txt");
		try {
			FileInputStream inputStream = new FileInputStream(serverProperties);
			InputStreamReader reader = new InputStreamReader(inputStream);
			char[] chars = new char[inputStream.available()];
			reader.read(chars, 0, inputStream.available());
			String content = new String(chars);
			modelAndView.addObject("serverProperties", content);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return modelAndView;
	}

	@RequestMapping("mb/saveSP")
	@ResponseBody
	public String saveSP(@RequestParam("content") String content){
		System.out.println(content);
		File sp = new File(systemPropertiesConfig.getMcLocation() + "/server.properties");
//		File sp = new File(systemPropertiesConfig.getMcLocation() + "\\server.properties");
		String result = "";
		try {
			FileOutputStream outputStream = new FileOutputStream(sp);
			outputStream.write(content.getBytes("UTF-8"));
			outputStream.flush();
			outputStream.close();
			result = "alert('success11121!!');";
		} catch (FileNotFoundException e) {
			result = "alert('error11121!!');";
			e.printStackTrace();
		} catch (IOException e) {
			result = "alert('error11121!!');";
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(value = "mb/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void downloadMod(String fileName, HttpServletResponse response) {
		File file = new File(systemPropertiesConfig.getModLocation() + "/" + fileName);
		//File file = new File(systemPropertiesConfig.getModLocation() + "\\" + fileName);
		if (file.exists()) {
			response.setContentType("application/octet-stream");
			response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
			try {
				FileInputStream inputStream = new FileInputStream(file);
				byte[] bytes = new byte[inputStream.available()];
				inputStream.read(bytes);
				response.getOutputStream().write(bytes);
				response.getOutputStream().flush();
				response.getOutputStream().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@RequestMapping("mb/upload")
	@ResponseBody
	public String uploadMod(@RequestParam("files") MultipartFile[] files, HttpServletResponse response) {
		Arrays.stream(files).forEach(f->{
			System.out.println(f.getOriginalFilename());
			File transferred = new File(systemPropertiesConfig.getModLocation() + "/" + f.getOriginalFilename());
			//File transferred = new File(systemPropertiesConfig.getModLocation() + "\\" + f.getOriginalFilename());
			try {
				f.transferTo(transferred);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		return "alert('success');";
	}
}
