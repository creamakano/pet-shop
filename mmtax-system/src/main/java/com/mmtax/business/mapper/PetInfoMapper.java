package com.mmtax.business.mapper;

import com.mmtax.business.domain.PetInfo;
import com.mmtax.business.dto.PetInfoDTO;
import com.mmtax.business.dto.PetInfoQueryDTO;
import com.mmtax.common.utils.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 宠物 数据层
 * 
 * @author meimiao
 * @date 2021-04-10
 */
public interface PetInfoMapper extends MyMapper<PetInfo>
{
    /**
     * 查询宠物列表
     * @param queryDTO
     * @return
     */
    List<PetInfoDTO> selectPetInfoList(PetInfoQueryDTO queryDTO);

    /**
     * 修改宠物类型
     */
    Integer updatePetType(@Param("id") Integer id,@Param("type") String type);

}