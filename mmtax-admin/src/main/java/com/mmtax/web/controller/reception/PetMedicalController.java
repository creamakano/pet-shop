package com.mmtax.web.controller.reception;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmtax.business.domain.PetInfo;
import com.mmtax.business.domain.PetMedicalRecord;
import com.mmtax.business.dto.AddPetAndMasterInfoDTO;
import com.mmtax.business.dto.PetInfoDTO;
import com.mmtax.business.dto.PetInfoQueryDTO;
import com.mmtax.business.mapper.PetInfoMapper;
import com.mmtax.business.mapper.PetMedicalRecordMapper;
import com.mmtax.business.service.IPetMedicalService;
import com.mmtax.business.service.IPetSaleRecordService;
import com.mmtax.common.constant.ShiroConstants;
import com.mmtax.common.core.controller.BaseController;
import com.mmtax.common.core.domain.AjaxResult;
import com.mmtax.common.core.page.PageDomain;
import com.mmtax.common.core.page.TableDataInfo;
import com.mmtax.common.core.page.TableSupport;
import com.mmtax.common.enums.DelStatusEnum;
import com.mmtax.common.exception.BusinessException;
import com.mmtax.common.utils.StringUtils;
import com.mmtax.common.utils.sql.SqlUtil;
import com.mmtax.framework.shiro.session.OnlineSession;
import com.mmtax.system.domain.SysDictType;
import com.mmtax.system.domain.SysUser;
import com.mmtax.web.config.AlipayConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Api(tags = " 前台宠物医疗")
@Controller
@RequestMapping("/reception/petMedical")
public class PetMedicalController extends BaseController{

    protected final Logger logger = LoggerFactory.getLogger(BaseController.class);
    private String prefix = "/reception/petMedical";

    @Autowired
    private IPetMedicalService petMedicalService;

    @Autowired
    private PetInfoMapper petInfoMapper;

    @Autowired
    private PetMedicalRecordMapper medicalRecordMapper;

    @ApiOperation(value = "销售宠物记录列表页面")
    @GetMapping()
    public String petSaleRecord(ModelMap modelMap)
    {
        List<SysUser> doctor = petMedicalService.getDoctorUser();
        modelMap.put("doctor",doctor);
        return prefix + "/index";
    }


    /**
     * 查询宠物列表
     */
    @ApiOperation(value = "查询宠物列表")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(PetInfoQueryDTO queryDTO,ServletRequest request)
    {
        OnlineSession session = (OnlineSession) request.getAttribute(ShiroConstants.ONLINE_SESSION);
        Long userId = session.getUserId();
        queryDTO.setUserId(userId);
        startPage();
        List<PetInfoDTO> list = petMedicalService.selectPetInfoList(queryDTO);
        return getDataTable(list);
    }

    /**
     * 新增保存宠物
     */
    @ApiOperation(value = "新增保存宠物页面")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }


    /**
     * 新增保存宠物
     */
    @ApiOperation(value = "新增保存宠物")
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(AddPetAndMasterInfoDTO dto,ServletRequest request)
    {
        OnlineSession session = (OnlineSession) request.getAttribute(ShiroConstants.ONLINE_SESSION);
        Long userId = session.getUserId();
        dto.setUserId(userId);
        try {
            petMedicalService.insertPetInfo(dto);
        } catch (BusinessException e) {
            logger.info("reception/petMedical/add", e);
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            logger.error("reception/petMedical/add", e);
            return AjaxResult.error("新增宠物信息异常");
        }
        return AjaxResult.success();
    }

    /**
     * 宠物挂号
     */
    @ApiOperation(value = "宠物挂号")
    @PostMapping("/registry")
    @ResponseBody
    public String registry(Integer id,Integer userId,ServletRequest request) throws AlipayApiException {
        petInfoMapper.updatePetType(id,"1");
        //添加宠物病历信息
        PetMedicalRecord petMedicalRecord = new PetMedicalRecord();
        petMedicalRecord.setPetInfoId(id);
        petMedicalRecord.setDisease("");
        petMedicalRecord.setMethod("");
        petMedicalRecord.setRemake("");
        petMedicalRecord.setUserId(userId);
        petMedicalRecord.setDelStatus(DelStatusEnum.NORMAL.getCode());
        petMedicalRecord.setCreateTime(new Date());
        petMedicalRecord.setUpdateTime(new Date());
        medicalRecordMapper.insertSelective(petMedicalRecord);

        AlipayClient alipayClient = new DefaultAlipayClient(
                AlipayConfig.gatewayUrl,
                AlipayConfig.app_id,
                AlipayConfig.merchant_private_key, "json",
                AlipayConfig.charset,
                AlipayConfig.alipay_public_key,
                AlipayConfig.sign_type);
        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        //这里设置支付后跳转的地址
        alipayRequest.setReturnUrl(AlipayConfig.return_url);
        String out_trade_no = UUID.randomUUID().toString().replace("-","");
        String total_amount = String.valueOf("10");
        String subject = "宠物挂号收费";
        String body =null;
        String timeout_express = "5m";
        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+ timeout_express +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        String form = alipayClient.pageExecute(alipayRequest).getBody();
        form = form.replace("document.forms[0].submit();","document.forms['punchout_form'].submit()");
        return form;
    }

    /**
     *
     */
    @ApiOperation(value = "")
    @PostMapping("/getMedicalRecord")
    @ResponseBody
    public AjaxResult getMedicalRecord(Integer id)
    {
        return petMedicalService.getMedicalRecord(id);
    }
    /**
     * 诊疗结束
     */
    @ApiOperation(value = "诊疗结束")
    @PostMapping("/treatmentFinished")
    @ResponseBody
    public AjaxResult treatmentFinished(Integer petInfoId)
    {
        return petMedicalService.treatmentFinished(petInfoId);
    }


    /**
     * 修改宠物
     */
    @ApiOperation(value = "跳转修改宠物页面")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, ModelMap mmap)
    {
        PetInfo petInfo = petMedicalService.selectPetInfoById(id);
        mmap.put("petInfo", petInfo);
        return prefix + "/edit";
    }
    /**
     * 修改保存宠物
     */
    @ApiOperation(value = "修改保存宠物")
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(PetInfo petInfo)
    {
        return toAjax(petMedicalService.updatePetInfo(petInfo));
    }

    /**
     * 删除宠物
     */
    @ApiOperation(value = "删除宠物")
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(petMedicalService.deletePetInfoByIds(ids));
    }


    /**
     * 设置请求分页数据
     */
    protected void startPage()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        if (StringUtils.isNotNull(pageNum) && StringUtils.isNotNull(pageSize))
        {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.startPage(pageNum, pageSize, orderBy);
        }
    }

    /**
     * 响应请求分页数据
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected TableDataInfo getDataTable(List<?> list)
    {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(0);
        rspData.setRows(list);
        rspData.setTotal(new PageInfo(list).getTotal());
        return rspData;
    }

}
