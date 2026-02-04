package com.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lottery.entity.Prize;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 奖品Mapper
 */
@Mapper
public interface PrizeMapper extends BaseMapper<Prize> {

    /**
     * 扣减奖品库存（乐观锁）
     */
    @Update("UPDATE prize SET remaining_count = remaining_count - 1 " +
            "WHERE id = #{prizeId} AND remaining_count > 0")
    int decreaseStock(@Param("prizeId") Long prizeId);
}
