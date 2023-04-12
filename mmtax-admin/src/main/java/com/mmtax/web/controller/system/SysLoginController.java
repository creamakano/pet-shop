package com.mmtax.web.controller.system;

import com.mmtax.business.domain.PetMasterInfo;
import com.mmtax.business.mapper.PetMasterInfoMapper;
import com.mmtax.business.util.ShiroUtilsLess;
import com.mmtax.common.constant.Constants;
import com.mmtax.common.constant.RequestContans;
import com.mmtax.common.context.ApiContext;
import com.mmtax.common.core.controller.BaseController;
import com.mmtax.common.core.domain.AjaxResult;
import com.mmtax.common.enums.DelStatusEnum;
import com.mmtax.common.exception.BusinessException;
import com.mmtax.common.utils.IpUtils;
import com.mmtax.common.utils.ServletUtils;
import com.mmtax.common.utils.StringUtils;
import com.mmtax.framework.filter.ParamFilter;
import com.mmtax.system.domain.SysUser;
import com.mmtax.system.service.ISysConfigService;
import com.mmtax.system.service.ISysUserService;
import com.mmtax.web.controller.monitor.SysOperlogController;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.util.Date;
import java.util.List;

/**
 * 登录验证
 * 
 * @author mmtax
 */
@Controller
public class SysLoginController extends BaseController
{
    private static final Logger logger = LoggerFactory.getLogger(SysLoginController.class);
    @Autowired
    private ISysConfigService sysConfigService;
    @Autowired
    private ApiContext apiContext;
    @Autowired
    private ISysUserService userService;
    @Resource
    PetMasterInfoMapper petMasterInfoMapper;
    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response)
    {

        String serverName = request.getServerName();
        // 如果是Ajax请求，返回Json字符串。
        if (ServletUtils.isAjaxRequest(request))
        {
            return ServletUtils.renderString(response, "{\"code\":\"1\",\"msg\":\"未登录或登录超时。请重新登录\"}");
        }

        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public AjaxResult ajaxLogin(String username, String password, Boolean rememberMe)
    {
        UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);
        Subject subject = SecurityUtils.getSubject();
        try
        {
            subject.login(token);
            return success();
        }
        catch (AuthenticationException e)
        {
            String msg = "用户或密码错误";
            if (StringUtils.isNotEmpty(e.getMessage()))
            {
                msg = e.getMessage();
            }
            return error(msg);
        }
    }

    @GetMapping("/unauth")
    public String unauth()
    {
        return "error/unauth";
    }
    @GetMapping("/registry")
    public String receptionLogin(){
        System.out.println("asdasdad");
        return "registry";
    }
    @PostMapping ("/registry")
    public String receptionLogin(String phoneNumber, String password){
        PetMasterInfo info = new PetMasterInfo();
        info.setPhonenumber(phoneNumber);
        info.setDelStatus(DelStatusEnum.NORMAL.getCode());
        List<PetMasterInfo> infos = petMasterInfoMapper.select(info);
        if (infos.size() > 0) {
            throw new BusinessException("手机号已被注册");
        }
        PetMasterInfo petMasterInfo = new PetMasterInfo();
        petMasterInfo.setPhonenumber(phoneNumber);
        petMasterInfo.setDelStatus(DelStatusEnum.NORMAL.getCode());
        petMasterInfo.setCreateTime(new Date());
        petMasterInfo.setUpdateTime(new Date());
        petMasterInfo.setName(phoneNumber);
        petMasterInfo.setSex("2");
        petMasterInfoMapper.insertSelective(petMasterInfo);

        SysUser user = new SysUser();
        user.setDeptId(109L);
        user.setUserName(petMasterInfo.getName());
        user.setPhonenumber(petMasterInfo.getPhonenumber());
        user.setEmail(null);
        user.setPassword(password);
        user.setSex(petMasterInfo.getSex());
        user.setLoginName(petMasterInfo.getPhonenumber());
        user.setRoleId(4L);
        user.setStatus("0");
        Long[] roleIds = new Long[]{4L};
        Long[] postIds = new Long[]{5L};
        user.setRoleIds(roleIds);
        user.setPostIds(postIds);

        user.setProviderId(apiContext.getCurrentProviderId());
        user.setSalt(ShiroUtilsLess.randomSalt());
        user.setPassword(encryptPassword(user.getLoginName(), user.getPassword(), user.getSalt()));
        user.setCreateBy(user.getLoginName());
        userService.insertUser(user);


        return "login";
    }
    public String encryptPassword(String username, String password, String salt)
    {
        return new Md5Hash(username + password + salt).toHex().toString();
    }
}
