package com.imxss.web.controller.xss;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.coody.framework.context.annotation.LogHead;
import org.coody.framework.core.controller.BaseController;
import org.coody.framework.core.thread.XssThreadHandle;
import org.coody.framework.util.EncryptUtil;
import org.coody.framework.util.PrintException;
import org.coody.framework.util.RequestUtil;
import org.coody.framework.util.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.imxss.web.domain.EmailSendCensus;
import com.imxss.web.domain.LetterInfo;
import com.imxss.web.domain.LetterParas;
import com.imxss.web.domain.ModuleInfo;
import com.imxss.web.domain.ProjectInfo;
import com.imxss.web.domain.ProjectModuleMapping;
import com.imxss.web.domain.UserInfo;
import com.imxss.web.service.EmailService;
import com.imxss.web.service.IpService;
import com.imxss.web.service.LetterService;
import com.imxss.web.service.ModuleService;
import com.imxss.web.service.ProjectService;
import com.imxss.web.service.SuffixService;
import com.imxss.web.service.UserService;

@Controller
@RequestMapping("/s")
public class XssController extends BaseController {

	@Resource
	ProjectService projectService;
	@Resource
	ModuleService moduleService;
	@Resource
	SuffixService suffixService;
	@Resource
	LetterService letterService;
	@Resource
	IpService ipService;
	@Resource
	UserService userService;
	@Resource
	EmailService emailService;

	@RequestMapping(value = { "/{id:\\d+}" })
	public void xssContext(HttpServletRequest req, HttpServletResponse res, @PathVariable Integer id) {
		ProjectInfo project = projectService.loadProjectInfo(id);
		if (StringUtil.isNullOrEmpty(project) || StringUtil.isNullOrEmpty(project.getModuleId())) {
			return;
		}
		if(project.getIsOpen()!=1){
			logger.info("????????????????????????:" + id);
			return;
		}
		// ??????????????????
		Integer moduleId = project.getModuleId();
		String referer = req.getHeader("Referer");
		List<ProjectModuleMapping> mappings = projectService.loadProjectMappings(id);
		if (!StringUtil.isNullOrEmpty(mappings)) {
			String ip = RequestUtil.getIpAddr(req);
			for (ProjectModuleMapping mapping : mappings) {
				if (mapping.getType() == 1) {
					logger.debug("????????????:" + referer + ";??????URL:" + mapping.getMapping());
					if (StringUtil.isAntMatch(referer, mapping.getMapping())) {
						moduleId = mapping.getModuleId();
						logger.debug("????????????:" + moduleId);
						continue;
					}
					continue;
				}
				logger.debug("IP??????:" + referer + ";??????IP:" + mapping.getMapping());
				if (StringUtil.isAntMatch(ip, mapping.getMapping())) {
					moduleId = mapping.getModuleId();
					logger.debug("????????????:" + moduleId);
					continue;
				}

			}
		}
		setSessionPara("moduleId", moduleId);
		ModuleInfo module = moduleService.loadModuleInfo(moduleId);
		if (StringUtil.isNullOrEmpty(module)) {
			return;
		}
		String api = loadBasePath(req) + "s/" + "api_" + project.getId() + "."
				+ suffixService.loadSpringDefaultSuffix();
		String xmlCode= module.getContent();
		xmlCode = xmlCode.replace("{api}", api);
		xmlCode = xmlCode.replace("{basePath}", getAttribute("basePath").toString());
		xmlCode = xmlCode.replace("{projectId}", id.toString());
		xmlCode = xmlCode.replace("{moduleId}", moduleId.toString());
		xmlCode = xmlCode.replace("{defSuffix}",RequestUtil.loadBasePath(req));
		try {
			res.getWriter().write(xmlCode);
		} catch (Exception e) {
		}
	}
	

	@RequestMapping(value = { "auth_{projectId:\\d+}_{moduleId:\\d+}" })
	@LogHead("????????????")
	public String auth(HttpServletRequest req, HttpServletResponse res, @PathVariable Integer projectId, @PathVariable Integer moduleId) {
		String referer=(String)request.getSession().getAttribute("referer");
		if (StringUtil.isNullOrEmpty(referer)) {
			referer=StringUtil.isNullOrEmpty(req.getHeader("Referer"))?req.getHeader("referer"):req.getHeader("Referer");
			request.getSession().setAttribute("referer", referer);
		}
		setAttribute("moduleId", moduleId);
		setAttribute("projectId", moduleId);
		setAttribute("referer", referer);
		return "auth";
	}

	@RequestMapping(value = { "api_{id:\\d+}" })
	@LogHead("????????????")
	public void api(HttpServletRequest req, HttpServletResponse res, @PathVariable Integer id) {
		try {
			ProjectInfo project = projectService.loadProjectInfo(id);
			if(project.getIsOpen()!=1){
				logger.error("????????????????????????:" + id);
				return;
			}
			Map<String, String> paraMap = getParas();
			if (StringUtil.isNullOrEmpty(paraMap)) {
				logger.error("?????????????????????:" + id);
				return;
			}
			String referer = StringUtil.isNullOrEmpty(req.getHeader("Referer"))?req.getHeader("referer"):req.getHeader("Referer");
			String basePath = RequestUtil.loadBasePath(req);
			String ip = RequestUtil.getIpAddr(req);
			Integer moduleId = getSessionPara("moduleId");
			XssThreadHandle.xssThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					doApi(project, referer, paraMap, basePath, ip, moduleId);
				}
			});
		} catch (Exception e) {
			PrintException.printException(logger, e);
		} finally {
			res.setStatus(404);
		}
	}

	public void doApi(Integer projectId, String referer, Map<String, String> paraMap, String basePath, String ip,
			Integer moduleId) {
		ProjectInfo project = projectService.loadProjectInfo(projectId);
		if(project.getIsOpen()!=1){
			logger.error("????????????????????????:" + projectId);
			return;
		}
		if (StringUtil.isNullOrEmpty(paraMap)) {
			logger.error("?????????????????????:" + projectId);
			return;
		}
		doApi(project, referer, paraMap, basePath, ip, moduleId);
	}
	public void doApi(ProjectInfo project, String referer, Map<String, String> paraMap, String basePath, String ip,
			Integer moduleId) {

		try {
			// ??????????????????
			if (!StringUtil.isNullOrEmpty(project.getIgnoreRef())) {
				String[] pattens = project.getIgnoreRef().split(" ");
				for (String patten : pattens) {
					if (!StringUtil.isAntMatch(referer, patten)) {
						continue;
					}
					logger.error("?????????????????????:" + referer + ";" + patten);
					return;
				}
			}
			// ??????IP??????
			if (!StringUtil.isNullOrEmpty(project.getIgnoreIp())) {
				String[] pattens = project.getIgnoreIp().split(" ");
				for (String patten : pattens) {
					if (!StringUtil.isAntMatch(ip, patten)) {
						continue;
					}
					logger.error("IP???????????????:" + ip + ";" + patten);
					return;
				}
			}
			// ??????????????????
			String unionId = EncryptUtil.md5Code(paraMap.toString());
			LetterInfo letter = letterService.loadLetterInfo(unionId);
			if (letter != null) {
				logger.error("???????????????:" + referer + ";" + project.getId() + ";" + unionId);
				return;
			}
			letter = new LetterInfo();
			letter.setProjectId(project.getId());
			letter.setRefUrl(referer);
			letter.setUpdateTime(new Date());
			letter.setIp(ip);
			letter.setIsReaded(0);
			letter.setRefUrl(referer);
			letter.setUnionId(unionId);
			letter.setUserId(project.getUserId());
			letter.setModuleId(moduleId);
			Integer letterId = letterService.writeLetterInfo(letter).intValue();
			if (letterId < 1) {
				logger.error("??????????????????:" + letter);
				return;
			}
			for (String key : paraMap.keySet()) {
				try {
					LetterParas para = new LetterParas();
					para.setParaName(key);
					para.setParaValue(paraMap.get(key));
					para.setUpdateTime(new Date());
					para.setLetterId(letterId);
					letterService.writeLetterParas(para);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			// ?????????IP????????????
			ipService.loadIpInfo(ip);
			// ????????????
			if (project.getOpenEmail() == null || project.getOpenEmail() != 1) {
				return;
			}
			UserInfo userInfo = userService.loadUserInfo(project.getUserId());
			if (userInfo == null) {
				return;
			}
			EmailSendCensus census= emailService.getSendCensus(project.getUserId());
			if(census!=null) {
				if(census.getNum()>100) {
					logger.error("???????????????????????????????????????ID???"+project.getUserId());
					return;
				}
			}
			emailService.pushSendNum(project.getUserId());
			String context = MessageFormat.format("????????????:{0}\r\n????????????:{1}\r\n\r\n??????????????????????????????,?????????http:{2} ??????", referer, ip,
					basePath);
			emailService.sendEmailAuto("ImXSS", context, userInfo.getEmail());
			return;
		} catch (Exception e) {
			PrintException.printException(logger, e);
		}
	}
}
