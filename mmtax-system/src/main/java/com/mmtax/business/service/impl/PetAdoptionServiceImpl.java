package com.mmtax.business.service.impl;

import com.mmtax.business.domain.PetInfo;
import com.mmtax.business.domain.PetMasterInfo;
import com.mmtax.business.domain.PetSaleRecord;
import com.mmtax.business.dto.AddPetSaleDTO;
import com.mmtax.business.dto.PetInfoDTO;
import com.mmtax.business.dto.PetInfoQueryDTO;
import com.mmtax.business.mapper.PetAdoptionMapper;
import com.mmtax.business.mapper.PetInfoMapper;
import com.mmtax.business.mapper.PetMasterInfoMapper;
import com.mmtax.business.service.IPetAdoptionService;
import com.mmtax.common.enums.DelStatusEnum;
import com.mmtax.common.exception.BusinessException;
import com.mmtax.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 销售宠物记录 服务层实现
 * 
 * @author meimiao
 * @date 2021-04-27
 */
@Service
public class PetAdoptionServiceImpl implements IPetAdoptionService
{
    @Resource
    PetAdoptionMapper petAdoptionMapper;
    @Resource
    PetInfoMapper petInfoMapper;
    @Resource
    PetMasterInfoMapper petMasterInfoMapper;
    @Resource
    SysUserMapper sysUserMapper;

    @Override
    public List<PetInfoDTO> selectAdoptionList(PetInfoQueryDTO queryDTO) {
        List<PetInfoDTO> addPetSaleDTOS = petAdoptionMapper.selectAdoptionList(queryDTO);
        return addPetSaleDTOS;
    }

    @Override
    public int insertPetSaleRecord(AddPetSaleDTO dto) {
        if (null == dto) {
            throw new BusinessException("传入数据为空");
        }
        //添加宠物信息
        PetInfo petInfo = new PetInfo();
        petInfo.setPetName(dto.getPetName());
        petInfo.setPetSex(dto.getPetSex());
        petInfo.setPetAge(dto.getPetAge());
        petInfo.setPetType(dto.getPetType());
        petInfo.setPetInfoType(2);
        petInfo.setCreateTime(new Date());
        petInfo.setUpdateTime(new Date());
        petInfoMapper.insertSelective(petInfo);
        //添加宠物销售信息
        PetSaleRecord petSaleRecord = new PetSaleRecord();
        petSaleRecord.setPetInfoId(petInfo.getId());

        // petSaleRecord.setPrice(new BigDecimal(dto.getPrice()));
        // petSaleRecord.setCost(new BigDecimal(dto.getCost()));

        petSaleRecord.setRemake(dto.getRemake());
        petSaleRecord.setDelStatus(DelStatusEnum.NORMAL.getCode());
        petSaleRecord.setCreateTime(new Date());
        petSaleRecord.setUpdateTime(new Date());
        petAdoptionMapper.insertSelective(petSaleRecord);
        return 1;
    }

    @Override
    public PetSaleRecord selectPetSaleRecordById(Integer id) {
        PetSaleRecord petSaleRecord = petAdoptionMapper.selectByPrimaryKey(id);
        return petSaleRecord;
    }

    @Override
    public int updatePetSaleRecord(PetSaleRecord petSaleRecord, Long userId, String petName) {
        String phonenumber = sysUserMapper.selectUserById(userId).getPhonenumber();
        //查找宠物主人信息
        PetMasterInfo petMasterInfo = new PetMasterInfo();
        petMasterInfo.setPhonenumber(phonenumber);
        PetMasterInfo masterInfo = petMasterInfoMapper.selectOne(petMasterInfo);
        if (null == masterInfo) {
            throw new BusinessException("未找到主人信息,请注册或检查信息是否正确");
        }
        petSaleRecord.setUpdateTime(new Date());
        petSaleRecord.setStatus(1);
        petAdoptionMapper.updateByPrimaryKeySelective(petSaleRecord);
        PetSaleRecord selectRecord = new PetSaleRecord();
        selectRecord.setId(petSaleRecord.getId());
        PetSaleRecord record = petAdoptionMapper.selectOne(selectRecord);
        PetInfo petInfo = new PetInfo();
        petInfo.setPetName(petName);
        petInfo.setPetInfoType(6);
        petInfo.setId(record.getPetInfoId());
        petInfo.setMasterId(masterInfo.getId());
        petInfoMapper.updateByPrimaryKeySelective(petInfo);
        return 1;
    }

    @Override
    public int deletePetSaleRecordByIds(String ids) {
        List<PetSaleRecord> infos = petAdoptionMapper.selectByIds(ids);
        for (PetSaleRecord info : infos) {
            info.setDelStatus(DelStatusEnum.DELETE.getCode());
            info.setUpdateTime(new Date());
            petAdoptionMapper.updateByPrimaryKey(info);
        }
        return 1;
    }
}
