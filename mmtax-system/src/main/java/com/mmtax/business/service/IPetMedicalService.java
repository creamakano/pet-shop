package com.mmtax.business.service;

import com.mmtax.business.domain.PetInfo;
import com.mmtax.business.dto.AddPetAndMasterInfoDTO;
import com.mmtax.business.dto.PetInfoDTO;
import com.mmtax.business.dto.PetInfoQueryDTO;
import com.mmtax.common.core.domain.AjaxResult;
import com.mmtax.system.domain.SysUser;

import java.util.List;

/**
 * 宠物医疗 服务层
 */
public interface IPetMedicalService
{

    List<PetInfoDTO> selectPetInfoList(PetInfoQueryDTO queryDTO);

    int insertPetInfo(AddPetAndMasterInfoDTO dto);

    PetInfo selectPetInfoById(Integer id);

    int updatePetInfo(PetInfo petInfo);

    int deletePetInfoByIds(String ids);

    List<SysUser> getDoctorUser();

    AjaxResult getMedicalRecord(Integer id);

    AjaxResult treatmentFinished(Integer petInfoId);
}
