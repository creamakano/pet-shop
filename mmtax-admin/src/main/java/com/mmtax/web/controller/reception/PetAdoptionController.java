package com.mmtax.web.controller.reception;

import com.mmtax.business.domain.PetSaleRecord;
import com.mmtax.business.dto.PetInfoDTO;
import com.mmtax.business.dto.PetInfoQueryDTO;
import com.mmtax.business.mapper.PetInfoMapper;
import com.mmtax.business.service.IPetAdoptionService;
import com.mmtax.common.constant.ShiroConstants;
import com.mmtax.common.core.controller.BaseController;
import com.mmtax.common.core.domain.AjaxResult;
import com.mmtax.common.core.page.TableDataInfo;
import com.mmtax.framework.shiro.session.OnlineSession;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import java.util.List;

@Api(tags = " 前台宠物领养")
@Controller
@RequestMapping("reception/petAdoption")
public class PetAdoptionController extends BaseController {
    private String prefix = "reception/petAdoption";

    @Autowired
    private IPetAdoptionService adoptionService;
    @Autowired
    private PetInfoMapper petInfoMapper;

    @ApiOperation(value = "前台宠物领养列表页面")
    @GetMapping()
    public String petSaleRecord() {
        return prefix + "/index";
    }

    /**
     * 查询领养宠物记录列表
     */
    @ApiOperation(value = "查询领养宠物记录列表")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(PetInfoQueryDTO queryDTO) {
        startPage();
        List<PetInfoDTO> list = adoptionService.selectAdoptionList(queryDTO);
        return getDataTable(list);
    }

    /**
     * 修改前台宠物领养记销售录
     */
    @ApiOperation(value = "修改前台宠物领养记录页面")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, ModelMap mmap) {
        PetSaleRecord petSaleRecord = adoptionService.selectPetSaleRecordById(id);
        String petName = petInfoMapper.selectByPrimaryKey(petSaleRecord.getPetInfoId()).getPetName();
        mmap.put("petSaleRecord", petSaleRecord);
        mmap.put("petName", petName);
        return prefix + "/adoption";
    }

    /**
     * 修改保存前台宠物领养记录
     */
    @ApiOperation(value = "修改保存前台宠物领养记录")
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(PetSaleRecord petSaleRecord, String petName, ServletRequest request){
        OnlineSession session = (OnlineSession) request.getAttribute(ShiroConstants.ONLINE_SESSION);
        return toAjax(adoptionService.updatePetSaleRecord(petSaleRecord,session.getUserId(),petName));
    }

}
