package com.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lottery.entity.UserDrawCount;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户抽奖次数Mapper
 */
@Mapper
public interface UserDrawCountMapper extends BaseMapper<UserDrawCount> {
}
