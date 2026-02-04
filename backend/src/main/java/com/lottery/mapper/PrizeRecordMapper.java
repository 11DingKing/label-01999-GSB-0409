package com.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lottery.entity.PrizeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 中奖记录Mapper
 */
@Mapper
public interface PrizeRecordMapper extends BaseMapper<PrizeRecord> {
}
