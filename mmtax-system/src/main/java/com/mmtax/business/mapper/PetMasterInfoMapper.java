package com.mmtax.business.mapper;

import com.mmtax.business.domain.PetMasterInfo;
import com.mmtax.common.utils.MyMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 宠物主人 数据层
 * 
 * @author meimiao
 * @date 2021-04-10
 */
public interface PetMasterInfoMapper extends MyMapper<PetMasterInfo>
{
    void updateInfo(@Param("phoneNumber") String phoneNumber, @Param("info") PetMasterInfo masterInfo);
}