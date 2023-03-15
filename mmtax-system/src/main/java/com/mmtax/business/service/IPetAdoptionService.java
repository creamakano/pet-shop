package com.mmtax.business.service;

import com.mmtax.business.domain.PetSaleRecord;
import com.mmtax.business.dto.AddPetSaleDTO;
import com.mmtax.business.dto.PetInfoDTO;
import com.mmtax.business.dto.PetInfoQueryDTO;

import java.util.List;

/**
 * 销售宠物记录 服务层
 * 
 * @author meimiao
 * @date 2021-04-27
 */
public interface IPetAdoptionService
{
    List<PetInfoDTO> selectAdoptionList(PetInfoQueryDTO queryDTO);

    int insertPetSaleRecord(AddPetSaleDTO dto);

    PetSaleRecord selectPetSaleRecordById(Integer id);

    int updatePetSaleRecord(PetSaleRecord petSaleRecord, Long userId, String phonenumber);

    int deletePetSaleRecordByIds(String ids);
}
