package com.mmtax.business.service.impl;

import com.mmtax.business.domain.PetInfo;
import com.mmtax.business.domain.PetMasterInfo;
import com.mmtax.business.domain.PetMedicalRecord;
import com.mmtax.business.dto.AddPetAndMasterInfoDTO;
import com.mmtax.business.dto.PetInfoDTO;
import com.mmtax.business.dto.PetInfoQueryDTO;
import com.mmtax.business.mapper.PetInfoMapper;
import com.mmtax.business.mapper.PetMasterInfoMapper;
import com.mmtax.business.mapper.PetMedicalMapper;
import com.mmtax.business.mapper.PetMedicalRecordMapper;
import com.mmtax.business.service.IPetMedicalService;
import com.mmtax.common.core.domain.AjaxResult;
import com.mmtax.common.enums.DelStatusEnum;
import com.mmtax.common.exception.BusinessException;
import com.mmtax.common.utils.StringUtils;
import com.mmtax.system.domain.SysUser;
import com.mmtax.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 宠物 服务层实现
 * 
 * @author meimiao
 * @date 2021-04-10
 */
@Service
public class PetMedicalServiceImpl implements IPetMedicalService
{
    @Resource
    PetMedicalMapper petMedicalMapper;
    @Resource
    PetMasterInfoMapper petMasterInfoMapper;
    @Resource
    SysUserMapper sysUserMapper;
    @Resource
    PetMedicalRecordMapper petMedicalRecordMapper;
    @Resource
    PetInfoMapper petInfoMapper;

    @Override
    public List<PetInfoDTO> selectPetInfoList(PetInfoQueryDTO queryDTO) {
        return petMedicalMapper.selectPetInfoList(queryDTO);
    }

    @Override
    public int insertPetInfo(AddPetAndMasterInfoDTO dto) {
        if (null == dto) {
            throw new BusinessException("传入数据为空");
        }
        checkPetInfo(dto);
        String phonenumber = sysUserMapper.selectUserById(dto.getUserId()).getPhonenumber();
        //查找宠物主人信息
        PetMasterInfo petMasterInfo = new PetMasterInfo();
        petMasterInfo.setPhonenumber(phonenumber);
        PetMasterInfo masterInfo = petMasterInfoMapper.selectOne(petMasterInfo);
        if (null == masterInfo) {
            throw new BusinessException("未找到主人信息,请注册或检查信息是否正确");
        }
        //添加宠物信息
        PetInfo petInfo = new PetInfo();
        petInfo.setMasterId(masterInfo.getId());
        petInfo.setPetName(dto.getPetName());
        petInfo.setPetSex(dto.getPetSex());
        petInfo.setPetAge(dto.getPetAge());
        petInfo.setPetType(dto.getPetType());
        petInfo.setPetInfoType(dto.getPetInfoType());
        petInfo.setRemake(dto.getRemake());
        petInfo.setCreateTime(new Date());
        petInfo.setUpdateTime(new Date());
        petMedicalMapper.insertSelective(petInfo);
        return 1;
    }


    // PetMedicalRecord petMedicalRecord = new PetMedicalRecord();
    //     petMedicalRecord.setPetInfoId(petInfo.getId());
    //     petMedicalRecord.setDisease("");
    //     petMedicalRecord.setMethod("");
    //     petMedicalRecord.setRemake("");
    //     petMedicalRecord.setDelStatus(DelStatusEnum.NORMAL.getCode());
    //     petMedicalRecord.setCreateTime(new Date());
    //     petMedicalRecord.setUpdateTime(new Date());
    //     petMedicalRecordMapper.insertSelective(petMedicalRecord);

    @Override
    public PetInfo selectPetInfoById(Integer id) {
        PetInfo petInfo = new PetInfo();
        petInfo.setId(id);
        PetInfo rPetInfo = petMedicalMapper.selectOne(petInfo);
        return rPetInfo;
    }

    @Override
    public int updatePetInfo(PetInfo petInfo) {
        petInfo.setUpdateTime(new Date());
        int i = petMedicalMapper.updateByPrimaryKeySelective(petInfo);
        return i;
    }

    @Override
    public int deletePetInfoByIds(String ids) {
        List<PetInfo> petInfos = petMedicalMapper.selectByIds(ids);
        for (PetInfo petInfo : petInfos) {
            petInfo.setDelStatus(DelStatusEnum.DELETE.getCode().toString());
            petInfo.setUpdateTime(new Date());
            petMedicalMapper.updateByPrimaryKeySelective(petInfo);
        }
        return 1;
    }

    @Override
    public List<SysUser> getDoctorUser() {
       return sysUserMapper.selectUserListByRoleKey("doctor");
    }

    @Override
    public AjaxResult getMedicalRecord(Integer id) {
        List<PetMedicalRecord> records = petMedicalRecordMapper.selectByPetInfoId(id);
        PetMedicalRecord petMedicalRecord = records.get(0);
        return AjaxResult.success(petMedicalRecord);
    }

    @Override
    public AjaxResult treatmentFinished(Integer petInfoId) {
        petInfoMapper.updatePetType(petInfoId,"6");
        return AjaxResult.success();
    }

    private void checkPetInfo(AddPetAndMasterInfoDTO dto){

        if (StringUtils.isEmpty(dto.getPetName())) {
            throw new BusinessException("请输入宠物名字");
        }
        if (StringUtils.isEmpty(dto.getPetType())) {
            throw new BusinessException("请输入宠物种类");
        }
        if (StringUtils.isEmpty(dto.getPetSex().toString())) {
            throw new BusinessException("请输入宠物性别");
        }
        if (StringUtils.isEmpty(dto.getPetAge().toString())) {
            throw new BusinessException("请输入年龄");
        }

    }

}
